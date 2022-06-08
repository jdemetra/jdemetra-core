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
package demetra.tramoseats.io.html;

import demetra.data.DoubleSeq;
import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlClass;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTableHeader;
import demetra.html.HtmlTag;
import demetra.stats.AutoCovariances;
import java.io.IOException;
import java.util.function.IntToDoubleFunction;
import jdplus.arima.ArimaModel;
import jdplus.arima.BartlettApproximation;
import jdplus.arima.LinearProcess;
import jdplus.dstats.Normal;
import jdplus.ucarima.WienerKolmogorovDiagnostics;

/**
 *
 * @author Jean Palate
 */
public class HtmlWienerKolmogorovDiagnostics extends AbstractHtmlElement implements HtmlElement {

    private final double m_badthreshold = 0.005;
    private final double m_goodthresohold = 0.05;
    private final double m_ccbadthreshold = 0.5;
    private final double m_ccgoodthresohold = 0.25;
    private final WienerKolmogorovDiagnostics diags_;
    private final int freq_;
    private final String[] desc_;
    private final boolean[] signals_;

    private HtmlClass valueStyle(double val) {
        if (val < m_badthreshold) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val < m_goodthresohold) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }

    private HtmlClass ccStyle(double val) {
        double aval = Math.abs(val);
        if (aval >= m_ccbadthreshold) {
            return Bootstrap4.TEXT_DANGER;
        } else if (aval < m_ccgoodthresohold) {
            return Bootstrap4.TEXT_SUCCESS;
        } else {
            return Bootstrap4.TEXT_WARNING;
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
        stream.write(HtmlTag.HEADER1,
                "Distribution of component, theoretical estimator and empirical estimate (stationary transformation)").newLine();
    }

    private void writeVariance(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Variance");
        stream.newLine();
        stream.open(new HtmlTable().withWidth(500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("").withWidth(100));
        stream.write(new HtmlTableHeader("Component").withWidth(100));
        stream.write(new HtmlTableHeader("Estimator").withWidth(100));
        stream.write(new HtmlTableHeader("Estimate").withWidth(100));
        stream.write(new HtmlTableHeader("P-Value").withWidth(100));
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
                        df4.format(pval)).withClass(valueStyle(pval)));

                stream.close(HtmlTag.TABLEROW);
            }
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeAutoCorrelations(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, "Autocorrelation");
        Normal dist = new Normal();
        for (int i = 0; i < desc_.length; ++i) {
            ArimaModel cmodel = diags_.getStationaryComponentModel(i);
            LinearProcess emodel = diags_.getStationaryEstimatorModel(i);
            if (cmodel != null && emodel != null) {
                stream.newLine();
                stream.write(HtmlTag.HEADER3, desc_[i]);
                stream.open(new HtmlTable().withWidth(500));
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableHeader("Lag").withWidth(100));
                stream.write(new HtmlTableHeader("Component").withWidth(100));
                stream.write(new HtmlTableHeader("Estimator").withWidth(100));
                stream.write(new HtmlTableHeader("Estimate").withWidth(100));
                stream.write(new HtmlTableHeader("P-Value").withWidth(100));
                stream.close(HtmlTag.TABLEROW);

                double tvar = cmodel.getAutoCovarianceFunction().get(0);
                DoubleSeq data = diags_.getStationaryEstimate(i);
                BartlettApproximation.AutoCorrelation bac=new BartlettApproximation.AutoCorrelation(emodel);
                IntToDoubleFunction ac = AutoCovariances.autoCorrelationFunction(data, 0);
                for (int l = 1; l <= freq_; ++l) {
                    stream.open(HtmlTag.TABLEROW);
                    double tcor = cmodel.getAutoCovarianceFunction().get(l) / tvar;
                    double ecor = bac.get(l);
                    double cor = ac.applyAsDouble(l);
                    double sd = bac.standardDeviation(l, data.length());
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
                            df4.format(pval)).withClass(valueStyle(pval)));
                    stream.close(HtmlTag.TABLEROW);
                }
                stream.close(HtmlTag.TABLE);
            }
        }
    }

    private void writeCrossCorrelations(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, "Cross-correlation");
        stream.open(new HtmlTable().withWidth(500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("").withWidth(200));
        stream.write(new HtmlTableHeader("Estimator").withWidth(100));
        stream.write(new HtmlTableHeader("Estimate").withWidth(100));
        stream.write(new HtmlTableHeader("P-Value").withWidth(100));
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
                                            df4.format(cc)).withClass(ccStyle(cc)));
                                    stream.write(new HtmlTableCell(
                                            df4.format(diags_.getEstimateCrossCorrelation(i, j))));
                                    double pval = diags_.getPValue(i, j);
                                    stream.write(new HtmlTableCell(
                                            df4.format(pval)).withClass(valueStyle(pval)));
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
