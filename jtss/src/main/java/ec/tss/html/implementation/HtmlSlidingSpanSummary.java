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

import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.tss.html.AbstractHtmlElement;
import static ec.tss.html.Bootstrap4.FONT_ITALIC;
import static ec.tss.html.Bootstrap4.FONT_WEIGHT_BOLD;
import static ec.tss.html.Bootstrap4.TEXT_DANGER;
import static ec.tss.html.Bootstrap4.TEXT_SUCCESS;
import static ec.tss.html.Bootstrap4.TEXT_WARNING;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.analysis.SlidingSpans;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSlidingSpanSummary extends AbstractHtmlElement implements IHtmlElement {

    private final SlidingSpans slidingspans_;
    private String sname_ = ModellingDictionary.S_CMP, siname_ = ModellingDictionary.SI_LIN;

    /**
     *
     * @param model
     */
    public HtmlSlidingSpanSummary(SlidingSpans slidingspans) {
        slidingspans_ = slidingspans;
    }

    public HtmlSlidingSpanSummary(SlidingSpans slidingspans, String sname, String siname) {
        slidingspans_ = slidingspans;
        sname_ = sname;
        siname_ = siname;
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
        TsFrequency freq = slidingspans_.getReferenceDomain().getFrequency();

        stream.write(HtmlTag.HEADER1, "Sliding spans summary");

        stream.write(HtmlTag.HEADER2, "Time spans");

        TsPeriod p = new TsPeriod(freq);
        for (int i = 0; i < slidingspans_.getSpanCount(); ++i) {
            TsDomain domain = slidingspans_.getDomain(i);
            stream.write("Span " + (i + 1) + ": from " + domain.getStart() + " to " + domain.getLast()).newLine();
        }        

        CombinedSeasonalityTest[] tests = new CombinedSeasonalityTest[ncols];
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
                DecompositionMode mode = slidingspans_.info(i).getData(ModellingDictionary.MODE, DecompositionMode.class);
                tests[i] = new CombinedSeasonalityTest(si, mode != null ? mode != DecompositionMode.Additive : false);
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
            stream.write(SeasonalityCell(tests[i].getStableSeasonality()));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Kruskal-Wallis").withClass(FONT_ITALIC));
        for (int i = 0; i < ncols; ++i)
            stream.write(SeasonalityCell(tests[i].getNonParametricTestForStableSeasonality()));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Moving seas.").withClass(FONT_ITALIC));
        for (int i = 0; i < ncols; ++i)
            stream.write(MovingSeasonalityCell(tests[i].getEvolutiveSeasonality()));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Identifiable seas.").withClass(FONT_ITALIC));
        for (int i = 0; i < ncols; ++i)
            stream.write(IdentifiableCell(tests[i]));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);

        DecompositionMode mode = slidingspans_.getReferenceInfo().getData(ModellingDictionary.MODE, DecompositionMode.class);
        boolean mul = mode == DecompositionMode.Multiplicative;

        stream.newLines(2).write(HtmlTag.HEADER2, "Means of seasonal factors");
        
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(""));
        for (int i = 1; i <= ncols; ++i)
            stream.write(new HtmlTableCell("Span " + i).withClass(FONT_ITALIC));
        stream.close(HtmlTag.TABLEROW);
        TsPeriod dummy = new TsPeriod(freq);
        for (int i = 0; i < freq.intValue(); ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(dummy.getPeriodString()));
            for (int j = 0; j < ncols; ++j) {
                if (siter[j] != null) {
                    TsDataBlock block = siter[j].nextElement();
                    double v = block.data.sum() / block.data.getLength();
                    if (mul)
                        stream.write(new HtmlTableCell(df4.format(v)));
                    else
                        stream.write(new HtmlTableCell(dg6.format(v)));
                }
                else
                    stream.write(new HtmlTableCell(""));
            }
            stream.close(HtmlTag.TABLEROW);
            dummy.move(1);
        }
        stream.close(HtmlTag.TABLE);
    }

    private void writeTimeSpans(HtmlStream stream) throws IOException{
        
    }
    
    private HtmlTableCell SeasonalityCell(StatisticalTest test) {
        NumberFormat format = new DecimalFormat("0.0");
        String val = format.format(test.getValue());
        if (test.getPValue() > .05)
            return new HtmlTableCell(val).withClass(TEXT_DANGER);
        else if (test.getPValue() > .01)
            return new HtmlTableCell(val).withClass(TEXT_WARNING);
        else
            return new HtmlTableCell(val).withClass(TEXT_SUCCESS);
    }

    private HtmlTableCell MovingSeasonalityCell(StatisticalTest test) {
        NumberFormat format = new DecimalFormat("0.0");
        String val = format.format(test.getValue());
        if (test.getPValue() < .05)
            return new HtmlTableCell(val).withClass(TEXT_DANGER);
        else if (test.getPValue() < .2)
            return new HtmlTableCell(val).withClass(TEXT_WARNING);
        else
            return new HtmlTableCell(val).withClass(TEXT_SUCCESS);
    }

    private HtmlTableCell IdentifiableCell(CombinedSeasonalityTest test) {
        switch(test.getSummary()) {
            case None:
                return new HtmlTableCell("NO").withClass(TEXT_DANGER);
            case ProbablyNone:
                return new HtmlTableCell("???").withClass(TEXT_WARNING);
            case Present:
                return new HtmlTableCell("YES").withClass(TEXT_SUCCESS);
        }
        return null;
    }
}
