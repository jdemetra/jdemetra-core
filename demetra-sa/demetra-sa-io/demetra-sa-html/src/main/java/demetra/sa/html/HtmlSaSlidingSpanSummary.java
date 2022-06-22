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

package demetra.sa.html;

import demetra.html.AbstractHtmlElement;
import static demetra.html.Bootstrap4.FONT_ITALIC;
import static demetra.html.Bootstrap4.TEXT_DANGER;
import static demetra.html.Bootstrap4.TEXT_SUCCESS;
import static demetra.html.Bootstrap4.TEXT_WARNING;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import demetra.sa.DecompositionMode;
import demetra.sa.SaDictionaries;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import jdplus.sa.tests.CombinedSeasonality;
import jdplus.timeseries.simplets.PeriodIterator;
import jdplus.timeseries.simplets.TsDataView;
import jdplus.timeseries.simplets.analysis.SlidingSpans;
import jdplus.stats.tests.TestsUtility;
/**
 *
 * @author Kristof Bayens
 */
public class HtmlSaSlidingSpanSummary extends AbstractHtmlElement implements HtmlElement {

    private final SlidingSpans slidingspans_;
    private final String sname_ , siname_ ;
    private final boolean multiplicative;

    public HtmlSaSlidingSpanSummary(SlidingSpans slidingspans, String sname, String siname, boolean mul) {
        slidingspans_ = slidingspans;
        sname_ = sname;
        siname_ = siname;
        this.multiplicative=mul;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        int ncols = slidingspans_.getSpanCount();
        if (ncols <= 1) {
            return;
        }
        int freq = slidingspans_.getReferenceDomain().getAnnualFrequency();

        stream.write(HtmlTag.HEADER1, "Sliding spans summary");

        stream.write(HtmlTag.HEADER2, "Time spans");

//        TsPeriod p = new TsPeriod(freq);
        for (int i = 0; i < slidingspans_.getSpanCount(); ++i) {
            TsDomain domain = slidingspans_.getDomain(i);
            stream.write("Span " + (i + 1) + ": from " + domain.getStartPeriod().display() + " to " + domain.getLastPeriod().display()).newLine();
        }        

        CombinedSeasonality[] tests = new CombinedSeasonality[ncols];
        PeriodIterator[] siter = new PeriodIterator[ncols];
        TsData[] s = new TsData[ncols];
        for (int i = 0; i < ncols; ++i) {
            //s[i] = slidingspans_.info(i).getData("decomposition.seas", null)
            s[i] = slidingspans_.info(i).getData(sname_, TsData.class);
            if (s[i] != null) {
                siter[i] = new PeriodIterator(s[i]);
            }
            TsData si = slidingspans_.info(i).getData(siname_, TsData.class);
            if (si != null) {
                tests[i] = CombinedSeasonality.of(si, multiplicative ? 1 : 0);
            }
        }

        stream.newLine().write(HtmlTag.HEADER2, "Tests for seasonality");
        
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(""));
        for (int i = 1; i <= ncols; ++i)
            stream.write(new HtmlTableCell("Span " + i).withClass(FONT_ITALIC));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Stable seas.").withClass(FONT_ITALIC));
        for (int i = 0; i < ncols; ++i)
            stream.write(SeasonalityCell(TestsUtility.ofAnova(tests[i].getStableSeasonalityTest())));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kruskal-Wallis").withClass(FONT_ITALIC));
        for (int i = 0; i < ncols; ++i)
            stream.write(SeasonalityCell(tests[i].getNonParametricTestForStableSeasonality().build()));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Moving seas.").withClass(FONT_ITALIC));
        for (int i = 0; i < ncols; ++i)
            stream.write(MovingSeasonalityCell(TestsUtility.ofAnova(tests[i].getEvolutiveSeasonalityTest())));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Identifiable seas.").withClass(FONT_ITALIC));
        for (int i = 0; i < ncols; ++i)
            stream.write(IdentifiableCell(tests[i]));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);

        DecompositionMode mode = slidingspans_.getReferenceInfo().getData(SaDictionaries.MODE, DecompositionMode.class);
        boolean mul = mode == DecompositionMode.Multiplicative;

        stream.newLines(2).write(HtmlTag.HEADER2, "Means of seasonal factors");
        
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(""));
        for (int i = 1; i <= ncols; ++i)
            stream.write(new HtmlTableCell("Span " + i).withClass(FONT_ITALIC));
        stream.close(HtmlTag.TABLEROW);
         for (int i = 0; i < freq; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i+1)));
            for (int j = 0; j < ncols; ++j) {
                if (siter[j] != null) {
                    TsDataView block = siter[j].nextElement();
                    double v = block.getData().average();
                    if (mul)
                        stream.write(new HtmlTableCell(df4.format(v)));
                    else
                        stream.write(new HtmlTableCell(dg6.format(v)));
                }
                else
                    stream.write(new HtmlTableCell(""));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
    }

    private HtmlTableCell SeasonalityCell(StatisticalTest test) {
        NumberFormat format = new DecimalFormat("0.0");
        String val = format.format(test.getValue());
        if (test.getPvalue() > .05)
            return new HtmlTableCell(val).withClass(TEXT_DANGER);
        else if (test.getPvalue() > .01)
            return new HtmlTableCell(val).withClass(TEXT_WARNING);
        else
            return new HtmlTableCell(val).withClass(TEXT_SUCCESS);
    }

    private HtmlTableCell MovingSeasonalityCell(StatisticalTest test) {
        NumberFormat format = new DecimalFormat("0.0");
        String val = format.format(test.getValue());
        if (test.getPvalue() < .05)
            return new HtmlTableCell(val).withClass(TEXT_DANGER);
        else if (test.getPvalue() < .2)
            return new HtmlTableCell(val).withClass(TEXT_WARNING);
        else
            return new HtmlTableCell(val).withClass(TEXT_SUCCESS);
    }

    private HtmlTableCell IdentifiableCell(CombinedSeasonality test) {
        switch(test.getSummary()) {
            case None -> {
                return new HtmlTableCell("NO").withClass(TEXT_DANGER);
            }
            case ProbablyNone -> {
                return new HtmlTableCell("???").withClass(TEXT_WARNING);
            }
            case Present -> {
                return new HtmlTableCell("YES").withClass(TEXT_SUCCESS);
            }
        }
        return null;
    }
}
