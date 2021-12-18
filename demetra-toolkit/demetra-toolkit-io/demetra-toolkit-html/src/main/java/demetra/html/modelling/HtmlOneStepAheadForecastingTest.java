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
package demetra.html.modelling;

import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlClass;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTableHeader;
import demetra.html.HtmlTag;
import demetra.stats.StatisticalTest;
import java.io.IOException;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.tests.OneStepAheadForecastingTest;

/**
 *
 * @author Jean Palate
 */
public class HtmlOneStepAheadForecastingTest extends AbstractHtmlElement implements HtmlElement {

    private final OneStepAheadForecastingTest test;
    private final double badthreshold_ = OutOfSampleDiagnosticsConfiguration.BAD;
    private final double goodthreshold_ = OutOfSampleDiagnosticsConfiguration.UNC;

    public HtmlOneStepAheadForecastingTest(OneStepAheadForecastingTest test) {
        this.test = test;
    }

    private void writeHeader(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Out of sample test").newLine();
        int nin = test.getInSampleLength(), nout = test.getOutOfSampleLength();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Model re-estimated on Linearized series for first ").write(nin);
        stream.write(HtmlTag.EMPHASIZED_TEXT, " observations and ").write(nout);
        stream.write(HtmlTag.EMPHASIZED_TEXT, " One-Period-Ahead Forecasts computed with model fixed.").newLines(2);
    }

    private void writeMeanTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Mean").newLine();
        int nout = test.getOutOfSampleLength();
        StatisticalTest tin = test.inSampleMeanTest(), tout = test.outOfSampleMeanTest();

        stream.write(HtmlTag.EMPHASIZED_TEXT, "Comparison between forecast errors (last ").write(nout).write(" observations)");
        stream.write(HtmlTag.EMPHASIZED_TEXT, " and residuals (in-sample)").newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "In sample standard eror of the residuals is ").write(HtmlTag.EMPHASIZED_TEXT, df4.format(Math.sqrt(test.getInSampleMeanSquaredError()))).newLines(2);
        stream.open(new HtmlTable().withWidth(300));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("Mean"));
        stream.write(new HtmlTableHeader("P-Value"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("In sample").withWidth(100));
        stream.write(new HtmlTableCell(df4.format(test.getInSampleMean())).withWidth(100));
        stream.write(new HtmlTableCell(df4.format(tin.getPvalue())).withWidth(100).withClass(getPvalueClass(tin.getPvalue())));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Out of sample").withWidth(100));
        stream.write(new HtmlTableCell(df4.format(test.getOutOfSampleMean())).withWidth(100));
        stream.write(new HtmlTableCell(df4.format(tout.getPvalue())).withWidth(100).withClass(getPvalueClass(tout.getPvalue())));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();
        if (tout.getPvalue() < badthreshold_) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Mean of forecast errors cannot be assumed zero", Bootstrap4.TEXT_DANGER);
        } else {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Mean of forecast errors can be assumed zero", Bootstrap4.TEXT_SUCCESS);
        }
        stream.newLines(2);
    }

    private void writeMSETest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "MSE").newLine();
        int nout = test.getOutOfSampleLength();
        StatisticalTest test = this.test.sameVarianceTest();
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
        stream.write(new HtmlTableCell(df4.format(this.test.getInSampleMeanSquaredError())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Out of sample").withWidth(100));
        stream.write(new HtmlTableCell(df4.format(this.test.getOutOfSampleMeanSquaredError())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();

        stream.write("Test for equality of MSE = " + df4.format(test.getValue())).newLine();
        stream.write("Distribution: " + test.getDescription()).newLine();
        stream.write("P-Value: ").write(df4.format(test.getPvalue()), getPvalueClass(test.getPvalue())).newLines(2);

        if (test.getPvalue() < badthreshold_) {
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

    private HtmlClass getPvalueClass(double val) {
        if (val < badthreshold_) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val < goodthreshold_) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }
}
