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
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.analysis.DiagnosticInfo;
import ec.tstoolkit.timeseries.analysis.SlidingSpans;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.YearIterator;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSlidingSpanDocument extends AbstractHtmlElement implements IHtmlElement {

    private SlidingSpans slidingspans_;
    private String name_;
    private DiagnosticInfo info_;
    private double threshold_ = 0.03;

    public HtmlSlidingSpanDocument(SlidingSpans slidingspans, String name, DiagnosticInfo info) {
        slidingspans_ = slidingspans;
        name_ = name;
        info_ = info;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        TsData s = slidingspans_.Statistics(name_, info_);
        if (s == null || s.getMissingValuesCount() == s.getLength())
            return;

        DescriptiveStatistics stats = new DescriptiveStatistics(new DataBlock(s.internalStorage()));
        if (stats.getMax() == stats.getMin() || stats.getMax() == 0)
            return;

        stream.write("Abnormal values : ");

        NumberFormat format = new DecimalFormat("0.0");
        int nabnormal = stats.countBetween(threshold_, Double.MAX_VALUE);
        double mabnormal = stats.getAverage();
        double p = nabnormal * 100.0 / stats.getObservationsCount();
        stream.write(format.format(p) + "%").newLines(2);

        if (nabnormal != 0) {
            String title = "";
            if (info_ == DiagnosticInfo.AbsoluteDifference || info_ == DiagnosticInfo.PeriodToPeriodDifference)
                title = "Breakdowns of unstable factors and Average Maximum Differences across spans";
            else
                title = "Breakdowns of unstable factors and Average Maximum Percent Differences across spans";
            stream.write(title, HtmlStyle.Bold, HtmlStyle.Underline, HtmlStyle.Info).newLines(2);

            PeriodIterator iter = new PeriodIterator(s);
            int freq = s.getFrequency().intValue();

            stream.open(new HtmlTable(0, 300));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Period", HtmlStyle.Italic));
            stream.write(new HtmlTableCell("Breakdowns", HtmlStyle.Italic));
            stream.write(new HtmlTableCell("Average", HtmlStyle.Italic));
            stream.close(HtmlTag.TABLEROW);

            double nbold = 2 * nabnormal;
            nbold /= freq;

            while(iter.hasMoreElements()) {
                TsDataBlock block = iter.nextElement();
                DescriptiveStatistics desc = new DescriptiveStatistics(block.data);
                int n = desc.countBetween(threshold_, Double.MAX_VALUE);
                double m = desc.getAverage();
                boolean bold = n > nbold | m > 2 * mabnormal;
                HtmlStyle[] styles = (bold ? new HtmlStyle[] { HtmlStyle.Bold } : new HtmlStyle[] {});

                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(block.start.getPeriodString(), styles));
                stream.write(new HtmlTableCell(n + "", styles));
                if (info_ == DiagnosticInfo.AbsoluteDifference || info_ == DiagnosticInfo.PeriodToPeriodDifference)
                    stream.write(new HtmlTableCell(format.format(m), styles));
                else
                    stream.write(new HtmlTableCell(format.format(100 * m), styles));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE).newLines(2);

            int y0 = s.getDomain().getStart().getYear();
            int y1 = s.getDomain().getLast().getYear();
            stream.open(new HtmlTable(0, 300));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Year", HtmlStyle.Italic));
            stream.write(new HtmlTableCell("Breakdowns", HtmlStyle.Italic));
            stream.write(new HtmlTableCell("Average", HtmlStyle.Italic));
            stream.close(HtmlTag.TABLEROW);

            nbold = 2 * nabnormal;
            nbold /= (y1-y0+1);

            YearIterator yiter = new YearIterator(s);
            while(yiter.hasMoreElements()) {
                TsDataBlock block = yiter.nextElement();
                DescriptiveStatistics desc = new DescriptiveStatistics(block.data);
                int n = desc.countBetween(threshold_, Double.MAX_VALUE);
                double m = desc.getAverage();
                boolean bold = n > nbold | m > 2 * mabnormal;
                HtmlStyle[] styles = (bold ? new HtmlStyle[] { HtmlStyle.Bold } : new HtmlStyle[] {});

                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(block.start.getYear() + "", styles));
                stream.write(new HtmlTableCell(n + "", styles));
                if (info_ == DiagnosticInfo.AbsoluteDifference || info_ == DiagnosticInfo.PeriodToPeriodDifference)
                    stream.write(new HtmlTableCell(format.format(m), styles));
                else
                    stream.write(new HtmlTableCell(format.format(100 * m), styles));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
        }
    }

    public double getThreshold() {
        return threshold_;
    }
    public void setThreshold(double threshold) {
        threshold_ = threshold;
    }
}
