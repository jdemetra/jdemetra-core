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

import ec.tss.html.*;
import ec.tss.sa.diagnostics.OutOfSampleDiagnosticsConfiguration;
import ec.tstoolkit.modelling.arima.diagnostics.IOneStepAheadForecastingTest;
import ec.tstoolkit.stats.MeanTest;
import ec.tstoolkit.stats.StatisticalTest;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlOneStepAheadForecastingTest extends AbstractHtmlElement implements IHtmlElement {

    private final IOneStepAheadForecastingTest test_;
    private double badthreshold_ = OutOfSampleDiagnosticsConfiguration.BAD;
    private double goodthreshold_ = OutOfSampleDiagnosticsConfiguration.UNC;

    public HtmlOneStepAheadForecastingTest(IOneStepAheadForecastingTest test) {
        test_ = test;
    }

    private void writeHeader(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Out of sample test").newLine();
        int nin = test_.getInSampleLength(), nout = test_.getOutOfSampleLength();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Model re-estimated on Linearized series for first ").write(nin);
        stream.write(HtmlTag.EMPHASIZED_TEXT, " observations and ").write(nout);
        stream.write(HtmlTag.EMPHASIZED_TEXT, " One-Period-Ahead Forecasts computed with model fixed.").newLines(2);
    }

    private void writeMeanTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Mean").newLine();
        int nout = test_.getOutOfSampleLength();
        MeanTest tin = test_.inSampleMeanTest(), tout = test_.outOfSampleMeanTest();

        stream.write(HtmlTag.EMPHASIZED_TEXT, "Comparison between forecast errors (last ").write(nout).write(" observations)");
        stream.write(HtmlTag.EMPHASIZED_TEXT, " and residuals (in-sample)").newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "In sample standard eror of the residuals is ").write(HtmlTag.EMPHASIZED_TEXT, df4.format(Math.sqrt(test_.getInSampleMSE()))).newLines(2);
        stream.open(new HtmlTable().withWidth(300));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("Mean"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("In sample").withWidth(100));
        stream.write(new HtmlTableCell(df4.format(tin.getMean())).withWidth(100));
        stream.write(new HtmlTableCell(df4.format(tin.getPValue())).withWidth(100).withClass(getPValueClass(tin.getPValue())));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Out of sample").withWidth(100));
        stream.write(new HtmlTableCell(df4.format(tout.getMean())).withWidth(100));
        stream.write(new HtmlTableCell(df4.format(tout.getPValue())).withWidth(100).withClass(getPValueClass(tout.getPValue())));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();
        if (tout.getPValue() < badthreshold_) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Mean of forecast errors cannot be assumed zero", Bootstrap4.TEXT_DANGER);
        } else {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Mean of forecast errors can be assumed zero", Bootstrap4.TEXT_SUCCESS);
        }
        stream.newLines(2);
    }

    private void writeMSETest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "MSE").newLine();
        int nout = test_.getOutOfSampleLength();
        StatisticalTest test = test_.mseTest();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Comparison between mean squared of forecast errors (last ").write(nout).write(" observations)");
        stream.write(HtmlTag.EMPHASIZED_TEXT, " and mean squared of residuals (in-sample)").newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "The test is strongly sensitive to the possible non-normality of the residuals.").newLines(2);
        stream.open(new HtmlTable().withWidth(200));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("MSE"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("In sample").withWidth(100));
        stream.write(new HtmlTableCell(df4.format(test_.getInSampleMSE())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Out of sample").withWidth(100));
        stream.write(new HtmlTableCell(df4.format(test_.getOutOfSampleMSE())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();

        stream.write("Test for equality of MSE = " + df4.format(test.getValue())).newLine();
        stream.write("Distribution: " + test.getDistribution().toString()).newLine();
        stream.write("P-Value: ").write(df4.format(test.getPValue()), getPValueClass(test.getPValue())).newLines(2);

        if (test.getPValue() < badthreshold_) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Mean Squared of forecast errors cannot be assumed close to the Mean Squared of in sample residuals.", Bootstrap4.TEXT_DANGER);
        } else {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Mean Squared of forecast errors can be assumed close to the Mean Squared of in sample residuals.", Bootstrap4.TEXT_SUCCESS);
        }
        stream.newLines(2);
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeHeader(stream);
        writeMeanTest(stream);
        writeMSETest(stream);
    }

    private HtmlClass getPValueClass(double val) {
        if (val < badthreshold_) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val < goodthreshold_) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }
}
