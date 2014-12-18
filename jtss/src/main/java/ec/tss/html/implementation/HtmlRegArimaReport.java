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
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.RegArimaReport;
import ec.tstoolkit.sarima.SarimaSpecification;
import java.io.IOException;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlRegArimaReport extends AbstractHtmlElement implements IHtmlElement {
    private RegArimaReport report_;
    private String title_;

    public HtmlRegArimaReport(String title, RegArimaReport report) {
        title_ = title;
        report_ = report;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (title_ != null)
            writeSummary(stream);

        writeTransform(stream);
        writeArima(stream);
        writeOutliers(stream);
        writeDetails(stream);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, title_).newLine();
        stream.write("number of series: ").write(report_.Total).newLines(2);
    }

    private void writeTransform(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Transformation").newLine();

        double tot = report_.Total;
        int n = report_.LogCount;
        stream.write("Log transformations: ").write(n).write(" [")
                .write(df2.format(n * 100 / tot)).write(" %]").newLines(2);
    }

    private void writeArima(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Arima models").newLine();

        double tot = report_.Total;
        SarimaSpecification[] specs = report_.getModels();

        for (int i = 0; i < specs.length; ++i) {
            int n = report_.getModelCount(specs[i]);
            stream.write(specs[i].toString()).write(": ").write(n).write(" [")
                    .write(df2.format(n * 100 / tot)).write(" %]").newLine();
        }

        int m = report_.MeanCount;
        stream.newLine().write("Mean correction: ").write(m).write(" [")
                .write(df2.format(m * 100 / tot)).write(" %]").newLines(2);
    }

    private void writeOutliers(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Outliers").newLine();

        double tot = report_.Total;
        int n = report_.AoCount + report_.LsCount + report_.TcCount + report_.SoCount;
        if (n > 0) {
            stream.write("All outliers: ").write(n).write(" [ average: ").write(df2.format(n / tot)).write(" ]").newLines(2);
            if (report_.AoCount > 0)
                stream.write("Additive outliers: ").write(report_.AoCount).write(" [ average: ").write(df2.format(report_.AoCount / tot)).write(" ]").newLine();
            if (report_.LsCount > 0)
                stream.write("Level shifts: ").write(report_.LsCount).write(" [ average: ").write(df2.format(report_.LsCount / tot)).write(" ]").newLine();
            if (report_.TcCount > 0)
                stream.write("Transitory changes: ").write(report_.TcCount).write(" [ average: ").write(df2.format(report_.TcCount / tot)).write(" ]").newLine();
            if (report_.SoCount > 0)
                stream.write("Seasonal outliers: ").write(report_.SoCount).write(" [ average: ").write(df2.format(report_.SoCount / tot)).write(" ]").newLine();
        }
    }

    private void writeDetails(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Calendar effects").newLine();

        double tot = report_.Total;
        int n = report_.TdCount;
        stream.write("Trading days corrections: ").write(n).write(" [").write(df2.format(n * 100 / tot)).write(" %]").newLine();
        n = report_.LpCount;
        stream.write("Leap year corrections: ").write(n).write(" [").write(df2.format(n * 100 / tot)).write(" %]").newLines(2);
        n = report_.EasterCount;
        stream.write("Easter corrections: ").write(n).write(" [").write(df2.format(n * 100 / tot)).write(" %]").newLine();
    }
}
