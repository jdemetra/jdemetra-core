/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tss.html.implementation;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.BartlettApproximation;
import ec.tstoolkit.arima.LinearModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.stats.AutoCorrelations;
import ec.tstoolkit.ucarima.WienerKolmogorovDiagnostics;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlWienerKolmogorovDiagnostics extends AbstractHtmlElement implements IHtmlElement {

    private double m_badthreshold = 0.005;
    private double m_goodthresohold = 0.05;
    private double m_ccbadthreshold = 0.5;
    private double m_ccgoodthresohold = 0.25;
    private WienerKolmogorovDiagnostics diags_;
    private int freq_;
    private String[] desc_;
    private boolean[] signals_;

    private HtmlStyle valueStyle(double val) {
        if (val < m_badthreshold) {
            return HtmlStyle.Danger;
        } else if (val < m_goodthresohold) {
            return HtmlStyle.Warning;
        } else {
            return HtmlStyle.Success;
        }
    }

    private HtmlStyle ccStyle(double val) {
        double aval = Math.abs(val);
        if (aval >= m_ccbadthreshold) {
            return HtmlStyle.Danger;
        } else if (aval < m_ccgoodthresohold) {
            return HtmlStyle.Success;
        } else {
            return HtmlStyle.Warning;
        }
    }

    public HtmlWienerKolmogorovDiagnostics(WienerKolmogorovDiagnostics diags, String[] desc, boolean[] signals, int freq) {
        diags_ = diags;
        freq_ = freq;
        desc_ = desc;
        signals_ = signals;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeHeader(stream);
        writeVariance(stream);
        writeAutoCorrelations(stream);
        writeCrossCorrelations(stream);
    }

    private void writeHeader(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1,
                "Distribution of component, theoretical estimator and empirical estimate (stationary transformation)").newLine();
    }

    private void writeVariance(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Variance");
        stream.newLine();
        stream.open(new HtmlTable(0, 500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("", 100));
        stream.write(new HtmlTableHeader("Component", 100));
        stream.write(new HtmlTableHeader("Estimator", 100));
        stream.write(new HtmlTableHeader("Estimate", 100));
        stream.write(new HtmlTableHeader("P-Value", 100));
        stream.close(HtmlTag.TABLEROW);

        for (int i = 0; i < desc_.length; ++i) {
            ArimaModel stmodel = diags_.getStationaryComponentModel(i);
            if (stmodel != null) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(desc_[i]));
                stream.write(new HtmlTableCell(
                        df4.format(stmodel.getAutoCovarianceFunction().get(0))));
                stream.write(new HtmlTableCell(
                        df4.format(diags_.getEstimatorVariance(i))));
                stream.write(new HtmlTableCell(
                        df4.format(diags_.getEstimateVariance(i))));
                double pval = diags_.getPValue(i);
                stream.write(new HtmlTableCell(
                        df4.format(pval), valueStyle(pval)));

                stream.close(HtmlTag.TABLEROW);
            }
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeAutoCorrelations(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, h2, "Autocorrelation");
        Normal dist = new Normal();
        for (int i = 0; i < desc_.length; ++i) {
            ArimaModel cmodel = diags_.getStationaryComponentModel(i);
            LinearModel emodel = diags_.getStationaryEstimatorModel(i);
            if (cmodel != null && emodel != null) {
                stream.newLine();
                stream.write(HtmlTag.HEADER3, h3, desc_[i]);
                stream.open(new HtmlTable(0, 500));
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableHeader("Lag", 100));
                stream.write(new HtmlTableHeader("Component", 100));
                stream.write(new HtmlTableHeader("Estimator", 100));
                stream.write(new HtmlTableHeader("Estimate", 100));
                stream.write(new HtmlTableHeader("P-Value", 100));
                stream.close(HtmlTag.TABLEROW);

                double tvar = cmodel.getAutoCovarianceFunction().get(0);
                double evar = emodel.getAutoCovarianceFunction().get(0);
                BartlettApproximation bartlett = new BartlettApproximation();
                bartlett.setX(emodel);
                IReadDataBlock data = diags_.getStationaryEstimate(i);
                AutoCorrelations ac = new AutoCorrelations(data);
                for (int l = 1; l <= freq_; ++l) {
                    stream.open(HtmlTag.TABLEROW);
                    double tcor = cmodel.getAutoCovarianceFunction().get(l) / tvar;
                    double ecor = emodel.getAutoCovarianceFunction().get(l) / evar;
                    double cor = ac.autoCorrelation(l);
                    double sd = bartlett.SDAutoCorrelation(data.getLength(), l);
                    double z = Math.abs(ecor - cor) / sd;
                    double pval = 1 - dist.getProbabilityForInterval(-z, z);
                    stream.write(new HtmlTableCell(Integer.toString(l)));
                    stream.write(new HtmlTableCell(
                            df4.format(tcor)));
                    stream.write(new HtmlTableCell(
                            df4.format(ecor)));
                    stream.write(new HtmlTableCell(
                            df4.format(cor)));
                    stream.write(new HtmlTableCell(
                            df4.format(pval), valueStyle(pval)));
                    stream.close(HtmlTag.TABLEROW);
                }
                stream.close(HtmlTag.TABLE);
            }
        }
    }

    private void writeCrossCorrelations(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, h2, "Cross-correlation");
        stream.open(new HtmlTable(0, 500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("", 200));
        stream.write(new HtmlTableHeader("Estimator", 100));
        stream.write(new HtmlTableHeader("Estimate", 100));
        stream.write(new HtmlTableHeader("P-Value", 100));
        stream.close(HtmlTag.TABLEROW);

        for (int i = 1; i < desc_.length; ++i) {
            if (diags_.getStationaryEstimate(i) != null) {
                if (signals_[i]) {
                    for (int j = 0; j < i; ++j) {
                        if (diags_.getStationaryEstimate(j) != null) {
                            if (signals_[j]) {
                                if (diags_.getStationaryComponentModel(i) != null
                                        && diags_.getStationaryComponentModel(j) != null) {
                                    stream.open(HtmlTag.TABLEROW);
                                    StringBuilder builder = new StringBuilder();
                                    builder.append(desc_[j]).append("/").append(desc_[i]);
                                    stream.write(new HtmlTableCell(builder.toString()));
                                    double cc = diags_.getEstimatorCrossCorrelation(i, j);
                                    stream.write(new HtmlTableCell(
                                            df4.format(cc), ccStyle(cc)));
                                    stream.write(new HtmlTableCell(
                                            df4.format(diags_.getEstimateCrossCorrelation(i, j))));
                                    double pval = diags_.getPValue(i, j);
                                    stream.write(new HtmlTableCell(
                                            df4.format(pval), valueStyle(pval)));
                                    stream.close(HtmlTag.TABLEROW);
                                }
                            }
                        }
                    }
                }
            }
        }
        stream.close(HtmlTag.TABLE);

    }
}
