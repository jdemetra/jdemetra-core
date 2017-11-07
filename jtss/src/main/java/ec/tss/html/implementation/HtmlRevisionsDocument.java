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
import ec.tss.html.Bootstrap4;
import static ec.tss.html.Bootstrap4.FONT_WEIGHT_BOLD;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.analysis.DiagnosticInfo;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlRevisionsDocument extends AbstractHtmlElement implements IHtmlElement {

    private final TsData revisions_;
    private final DiagnosticInfo info_;
    private int threshold_ = 2;

    public HtmlRevisionsDocument(TsData revisions, DiagnosticInfo info) {
        revisions_ = revisions;
        info_ = info;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {

        stream.write(HtmlTag.HEADER1, info_.toString());

        int y0 = revisions_.getDomain().getStart().getYear();
        int y1 = revisions_.getDomain().getLast().getYear();

        DescriptiveStatistics stats = new DescriptiveStatistics(revisions_);

        double mean = stats.getAverage();
        double rmse = stats.getRmse();

        NumberFormat format = new DecimalFormat("0.0000");
        stream.write(HtmlTag.EMPHASIZED_TEXT, "mean = " + format.format(mean)).newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "rmse = " + format.format(rmse)).newLines(2);

        int columnscount = 2 + y1 - y0;
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(""));
        for (int i = y0; i <= y1; ++i) {
            stream.write(new HtmlTableHeader("" + i));
        }
        stream.close(HtmlTag.TABLEROW);

        format = new DecimalFormat("0.000");
        TsPeriod x = new TsPeriod(revisions_.getFrequency(), y0, 0);
        for (int i = 0; i < revisions_.getFrequency().intValue(); ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(x.getPeriodString()).withClass(FONT_WEIGHT_BOLD));
            for (int j = 1; j < columnscount; ++j) {
                String txt = "";
                boolean danger = false;
                int k = revisions_.getDomain().search(x.plus((j - 1) * revisions_.getFrequency().intValue()));
                if (k >= 0) {
                    double d = revisions_.get(k);
                    if (Math.abs(d) > threshold_ * rmse) {
                        danger = true;
                    }
                    txt = format.format(revisions_.get(k));
                }
                if (danger) {
                    stream.write(new HtmlTableCell(txt).withClass(Bootstrap4.TEXT_DANGER));
                } else {
                    stream.write(new HtmlTableCell(txt));
                }
            }
            stream.close(HtmlTag.TABLEROW);
            x = x.plus(1);
        }
        stream.close(HtmlTag.TABLE);
    }

    public int getThreshold() {
        return threshold_;
    }

    public void setThreshold(int threshold) {
        threshold_ = threshold;
    }
}
