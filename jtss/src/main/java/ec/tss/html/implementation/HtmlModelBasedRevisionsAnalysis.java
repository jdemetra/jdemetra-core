/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tss.html.implementation;

import ec.satoolkit.ComponentDescriptor;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.arima.ArimaException;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.LinearModel;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlModelBasedRevisionsAnalysis extends AbstractHtmlElement {

    private final WienerKolmogorovEstimators estimators_;
    private final ComponentDescriptor[] cmps_;
    private final int freq_;

    public HtmlModelBasedRevisionsAnalysis(int freq, WienerKolmogorovEstimators estimators, ComponentDescriptor[] cmps) {
        freq_ = freq;
        estimators_ = estimators;
        cmps_ = cmps;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        errorAutocorrelations(stream);
        revisionErrorVariance(stream);
    }

    private void revisionErrorVariance(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Revision errors").newLine();
        stream.write("Percentage reduction in the standard error of the revision after additional years (comparison with concurrent estimators)",
                HtmlStyle.Italic).newLine();
        stream.open(new HtmlTable(0, 600));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("After..."));
        for (int i = 1; i <= 5; ++i) {
            stream.write(new HtmlTableHeader(Integer.toString(i) + (i == 1 ? " year" : " years")));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 0; i < cmps_.length; ++i) {
            ComponentDescriptor desc = cmps_[i];
            if (desc.lowFrequency && !estimators_.getUcarimaModel().getComponent(desc.cmp).isNull()) {
                try {
                    double[] v = estimators_.revisionVariance(desc.cmp, desc.signal, 0, freq_ * 5 + 1);

                    stream.open(HtmlTag.TABLEROW);
                    stream.write(new HtmlTableCell(desc.name, 100));

                    for (int j = 1; j <= 5; ++j) {
                        stream.write(new HtmlTableCell(pc2.format(1 - Math.sqrt(v[freq_ * j] / v[0])), 100));
                    }
                    stream.close(HtmlTag.TABLEROW);
                } catch (ArimaException | MatrixException | IOException err) {
                }
            }
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
        stream.write(HtmlTag.LINEBREAK);
    }

    private void errorAutocorrelations(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Auto-correlations of the errors").newLine();
        for (int i = 0; i < cmps_.length; ++i) {
            if (cmps_[i].lowFrequency) {
                if (!estimators_.getUcarimaModel().getComponent(cmps_[i].cmp).isNull()) {
                    try {
                        stream.write(HtmlTag.HEADER2, h2, cmps_[i].name).newLine();
                        ArimaModel finalError = estimators_.finalErrorModel(cmps_[i].cmp);
                        LinearModel revisionModel = estimators_.revisionModel(cmps_[i].cmp, 0);
                        stream.open(new HtmlTable(0, 500));
                        stream.open(HtmlTag.TABLEROW);
                        stream.write(new HtmlTableHeader("Lag"));
                        stream.write(new HtmlTableHeader("Final error"));
                        stream.write(new HtmlTableHeader("Revision error (concurrent estimator)"));
                        stream.write(new HtmlTableHeader("Total error (concurrent estimator)"));
                        stream.close(HtmlTag.TABLEROW);
                        double vf = finalError.getAutoCovarianceFunction().get(0);
                        double vr = revisionModel.getAutoCovarianceFunction().get(0);
                        stream.open(HtmlTag.TABLEROW);
                        stream.write(new HtmlTableCell("Variance", 50));
                        stream.write(new HtmlTableCell(df4.format(vf), 150));
                        stream.write(new HtmlTableCell(df4.format(vr), 150));
                        stream.write(new HtmlTableCell(df4.format(vf + vr), 150));
                        stream.close(HtmlTag.TABLEROW);
                        for (int j = 1; j <= freq_; ++j) {
                            stream.open(HtmlTag.TABLEROW);
                            stream.write(new HtmlTableCell(Integer.toString(j), 50));
                            double f = finalError.getAutoCovarianceFunction().get(j);
                            double r = revisionModel.getAutoCovarianceFunction().get(j);
                            stream.write(new HtmlTableCell(df4.format(f / vf), 150));
                            stream.write(new HtmlTableCell(df4.format(r / vr), 150));
                            stream.write(new HtmlTableCell(df4.format((f + r) / (vf + vr)), 150));
                            stream.close(HtmlTag.TABLEROW);
                        }
                    } catch (IOException | ArimaException | MatrixException err) {
                    }
                }
                stream.close(HtmlTag.TABLE);
                stream.newLine();
            }
        }
        stream.write(HtmlTag.LINEBREAK);
    }
}
