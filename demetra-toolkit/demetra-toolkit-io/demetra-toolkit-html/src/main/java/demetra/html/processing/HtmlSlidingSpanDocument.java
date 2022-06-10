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


package demetra.html.processing;

import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import static demetra.html.Bootstrap4.FONT_ITALIC;
import demetra.html.HtmlClass;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import jdplus.stats.DescriptiveStatistics;
import jdplus.timeseries.simplets.PeriodIterator;
import jdplus.timeseries.simplets.TsDataView;
import jdplus.timeseries.simplets.YearIterator;
import jdplus.timeseries.simplets.analysis.DiagnosticInfo;
import jdplus.timeseries.simplets.analysis.SlidingSpans;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSlidingSpanDocument extends AbstractHtmlElement implements HtmlElement {

    private final SlidingSpans slidingspans_;
    private final String name_;
    private final DiagnosticInfo info_;
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
        if (s == null || s.getValues().count(x->Double.isNaN(x))== s.length())
            return;

        DescriptiveStatistics stats = DescriptiveStatistics.of(s.getValues());
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
            stream.write(HtmlTag.IMPORTANT_TEXT, title, Bootstrap4.TEXT_INFO).newLines(2);

            PeriodIterator iter = new PeriodIterator(s);
            int freq = s.getAnnualFrequency();

            stream.open(new HtmlTable().withWidth(300));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Period").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Breakdowns").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Average").withClass(FONT_ITALIC));
            stream.close(HtmlTag.TABLEROW);

            double nbold = 2 * nabnormal;
            nbold /= freq;

            while(iter.hasMoreElements()) {
                TsDataView block = iter.nextElement();
                DescriptiveStatistics desc = DescriptiveStatistics.of(block.getData());
                int n = desc.countBetween(threshold_, Double.MAX_VALUE);
                double m = desc.getAverage();
                boolean bold = n > nbold | m > 2 * mabnormal;
                HtmlClass style = bold ? Bootstrap4.FONT_WEIGHT_BOLD : HtmlClass.NO_CLASS;

                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(Integer.toString(block.getStart().annualPosition()+1)).withClass(style));
                stream.write(new HtmlTableCell(n + "").withClass(style));
                if (info_ == DiagnosticInfo.AbsoluteDifference || info_ == DiagnosticInfo.PeriodToPeriodDifference)
                    stream.write(new HtmlTableCell(format.format(m)).withClass(style));
                else
                    stream.write(new HtmlTableCell(format.format(100 * m)).withClass(style));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE).newLines(2);

            int y0 = s.getDomain().getStartPeriod().year();
            int y1 = s.getDomain().getLastPeriod().year();
            stream.open(new HtmlTable().withWidth(300));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Year").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Breakdowns").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Average").withClass(FONT_ITALIC));
            stream.close(HtmlTag.TABLEROW);

            nbold = 2 * nabnormal;
            nbold /= (y1-y0+1);

            TsPeriod start=s.getStart();
            int period=start.annualFrequency();
            
            YearIterator yiter = new YearIterator(s);
            while(yiter.hasMoreElements()) {
                TsDataView block = yiter.nextElement();
                DescriptiveStatistics desc = DescriptiveStatistics.of(block.getData());
                int n = desc.countBetween(threshold_, Double.MAX_VALUE);
                double m = desc.getAverage();
                boolean bold = n > nbold | m > 2 * mabnormal;
                HtmlClass style = bold ? Bootstrap4.FONT_WEIGHT_BOLD : HtmlClass.NO_CLASS;

                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(block.getStart().year() + "").withClass(style));
                stream.write(new HtmlTableCell(n + "").withClass(style));
                if (info_ == DiagnosticInfo.AbsoluteDifference || info_ == DiagnosticInfo.PeriodToPeriodDifference)
                    stream.write(new HtmlTableCell(format.format(m)).withClass(style));
                else
                    stream.write(new HtmlTableCell(format.format(100 * m)).withClass(style));
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
