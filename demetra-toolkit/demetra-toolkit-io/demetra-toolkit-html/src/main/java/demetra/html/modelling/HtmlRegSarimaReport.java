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

import demetra.arima.SarimaOrders;
import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import java.io.IOException;
import jdplus.regsarima.regular.RegSarimaReport;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlRegSarimaReport extends AbstractHtmlElement implements HtmlElement {

    private final RegSarimaReport report;
    private final String title;

    public HtmlRegSarimaReport(String title, RegSarimaReport report) {
        this.title = title;
        this.report = report;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (title != null) {
            writeSummary(stream);
        }

        writeTransform(stream);
        writeArima(stream);
        writeOutliers(stream);
        writeDetails(stream);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, title).newLine();
        stream.write("number of series: ").write(report.getTotal()).newLines(2);
    }

    private void writeTransform(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Transformation").newLine();

        double tot = report.getTotal();
        int n = report.getLogCount();
        stream.write("Log transformations: ").write(n).write(" [")
                .write(df2.format(n * 100 / tot)).write(" %]").newLines(2);
    }

    private void writeArima(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Arima models").newLine();

        double tot = report.getTotal();
        SarimaOrders[] specs = report.getModels();

        for (int i = 0; i < specs.length; ++i) {
            int n = report.getModelCount(specs[i]);
            stream.write(specs[i].toString()).write(": ").write(n).write(" [")
                    .write(df2.format(n * 100 / tot)).write(" %]").newLine();
        }

        int m = report.getMeanCount();
        stream.newLine().write("Mean correction: ").write(m).write(" [")
                .write(df2.format(m * 100 / tot)).write(" %]").newLines(2);
    }

    private void writeOutliers(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Outliers").newLine();

        double tot = report.getTotal();
        int ao = report.getAoCount(), ls = report.getLsCount(), tc = report.getTcCount(), so = report.getSoCount();
        int n = ao + ls + tc + so;
        stream.write("All outliers: ").write(n).write(" [ average: ").write(df2.format(n / tot)).write(" ]").newLines(2);
        if (n > 0) {
            if (ao > 0) {
                stream.write("Additive outliers: ").write(ao).write(" [ average: ").write(df2.format(ao / tot)).write(" ]").newLine();
            }
            if (ls > 0) {
                stream.write("Level shifts: ").write(ls).write(" [ average: ").write(df2.format(ls / tot)).write(" ]").newLine();
            }
            if (tc > 0) {
                stream.write("Transitory changes: ").write(tc).write(" [ average: ").write(df2.format(tc / tot)).write(" ]").newLine();
            }
            if (so > 0) {
                stream.write("Seasonal outliers: ").write(so).write(" [ average: ").write(df2.format(so / tot)).write(" ]").newLine();
            }
            stream.newLine();
        }
    }

    private void writeDetails(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Calendar effects").newLine();

        double tot = report.getTotal();
        int n = report.getTdCount();
        stream.write("Trading days corrections: ").write(n).write(" [").write(df2.format(n * 100 / tot)).write(" %]").newLine();
        n = report.getLpCount();
        stream.write("Leap year corrections: ").write(n).write(" [").write(df2.format(n * 100 / tot)).write(" %]").newLines(2);
        n = report.getEasterCount();
        stream.write("Easter corrections: ").write(n).write(" [").write(df2.format(n * 100 / tot)).write(" %]").newLine();
    }
}
