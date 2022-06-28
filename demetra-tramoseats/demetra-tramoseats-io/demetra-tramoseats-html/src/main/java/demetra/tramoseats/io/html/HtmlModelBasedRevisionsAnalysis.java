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
package demetra.tramoseats.io.html;

import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTableHeader;
import demetra.html.HtmlTag;
import demetra.sa.ComponentDescriptor;
import java.io.IOException;
import jdplus.arima.ArimaException;
import jdplus.arima.ArimaModel;
import jdplus.arima.LinearProcess;
import jdplus.math.matrices.MatrixException;
import jdplus.ucarima.WienerKolmogorovEstimators;

/**
 *
 * @author Jean Palate
 */
public class HtmlModelBasedRevisionsAnalysis extends AbstractHtmlElement {

    private final WienerKolmogorovEstimators estimators;
    private final ComponentDescriptor[] components;
    private final int periodicity;

    public HtmlModelBasedRevisionsAnalysis(int freq, WienerKolmogorovEstimators estimators, ComponentDescriptor[] cmps) {
        periodicity = freq;
        this.estimators = estimators;
        components = cmps;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        errorAutocorrelations(stream);
        revisionErrorVariance(stream);
    }

    private void revisionErrorVariance(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Revision errors").newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Percentage reduction in the standard error of the revision after additional years (comparison with concurrent estimators)").newLine();
        stream.open(new HtmlTable().withWidth(600));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("After..."));
        for (int i = 1; i <= 5; ++i) {
            stream.write(new HtmlTableHeader(Integer.toString(i) + (i == 1 ? " year" : " years")));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 0; i < components.length; ++i) {
            ComponentDescriptor desc = components[i];
            if (desc.isLowFrequency() && !estimators.getUcarimaModel().getComponent(desc.getComponent()).isNull()) {
                try {
                    double[] v = estimators.revisionVariance(desc.getComponent(), desc.isSignal(), 0, periodicity * 5 + 1);

                    stream.open(HtmlTag.TABLEROW);
                    stream.write(new HtmlTableCell(desc.getName()).withWidth(100));

                    for (int j = 1; j <= 5; ++j) {
                        stream.write(new HtmlTableCell(pc2.format(1 - Math.sqrt(v[periodicity * j] / v[0]))).withWidth(100));
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
        stream.write(HtmlTag.HEADER1, "Auto-correlations of the errors").newLine();
        for (int i = 0; i < components.length; ++i) {
            if (components[i].isLowFrequency()) {
                if (!estimators.getUcarimaModel().getComponent(components[i].getComponent()).isNull()) {
                    try {
                        stream.write(HtmlTag.HEADER2, components[i].getName()).newLine();
                        ArimaModel finalError = estimators.finalErrorModel(components[i].getComponent());
                        LinearProcess revisionModel = estimators.revisionModel(components[i].getComponent(), 0);
                        stream.open(new HtmlTable().withWidth(500));
                        stream.open(HtmlTag.TABLEROW);
                        stream.write(new HtmlTableHeader("Lag"));
                        stream.write(new HtmlTableHeader("Final error"));
                        stream.write(new HtmlTableHeader("Revision error (concurrent estimator)"));
                        stream.write(new HtmlTableHeader("Total error (concurrent estimator)"));
                        stream.close(HtmlTag.TABLEROW);
                        double vf = finalError.getAutoCovarianceFunction().get(0);
                        double vr = revisionModel.getAutoCovarianceFunction().get(0);
                        stream.open(HtmlTag.TABLEROW);
                        stream.write(new HtmlTableCell("Variance").withWidth(50));
                        stream.write(new HtmlTableCell(df4.format(vf)).withWidth(150));
                        stream.write(new HtmlTableCell(df4.format(vr)).withWidth(150));
                        stream.write(new HtmlTableCell(df4.format(vf + vr)).withWidth(150));
                        stream.close(HtmlTag.TABLEROW);
                        for (int j = 1; j <= periodicity; ++j) {
                            stream.open(HtmlTag.TABLEROW);
                            stream.write(new HtmlTableCell(Integer.toString(j)).withWidth(50));
                            double f = finalError.getAutoCovarianceFunction().get(j);
                            double r = revisionModel.getAutoCovarianceFunction().get(j);
                            stream.write(new HtmlTableCell(df4.format(f / vf)).withWidth(150));
                            stream.write(new HtmlTableCell(df4.format(r / vr)).withWidth(150));
                            stream.write(new HtmlTableCell(df4.format((f + r) / (vf + vr))).withWidth(150));
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
