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
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.seats.SeatsResults;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlTramoSeatsGrowthRates extends AbstractHtmlElement implements IHtmlElement {

    private static final String TITLE = "Rates of growth";
    private static final String COMMENT1 = "The rate-of-growth of series Z(t) over the period (t1,t2) is expressed in percent points as [ (Z(t2) / Z(t1)) -1] * 100";
    private static final String TITLE_ADD = "Variations";
    private static final String COMMENT1_ADD = "The changes of series Z(t) over the period (t1,t2) is defined by [ (Z(t2) - Z(t1))]";
    private static final String COMMENT2 = "All standard errors reported for the rates-of growth in the following tables are computed using linear approximation to the rates. When period-to-period changes are large, these standard errors should be interpreted as broad approximations, that will tend to UNDERESTIMATE the true values.";
    private static final String COMMENT3 = "The error variances are based on the estimation error of the stochastic Trend and Sa series, and the errors in the parameter estimates are not considered.";

    private final PreprocessingModel preprocessing_;
    private final SeatsResults decomposition_;
    private final ISeriesDecomposition finalDecomposition_;
    private final int np;
    private final boolean mul;

    public HtmlTramoSeatsGrowthRates(CompositeResults results) {
        preprocessing_ = GenericSaResults.getPreprocessingModel(results);
        decomposition_ = GenericSaResults.getDecomposition(results, SeatsResults.class);
        finalDecomposition_ = GenericSaResults.getFinalDecomposition(results);
        np = finalDecomposition_.getSeries(ComponentType.Series, ComponentInformation.Value).getFrequency().intValue() * 2 + 1;
        mul = finalDecomposition_.getMode() != DecompositionMode.Additive;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeHeader(stream);
        writePeriodToPeriod(stream);
    }

    private void writeHeader(HtmlStream stream) throws IOException {
        if (mul) {
            stream.write(HtmlTag.HEADER1, TITLE).newLine();
            stream.write(HtmlTag.HEADER4, COMMENT1).newLine();
            stream.write(HtmlTag.HEADER4, COMMENT2).newLine();
        } else {
            stream.write(HtmlTag.HEADER1, TITLE_ADD).newLine();
            stream.write(HtmlTag.HEADER4, COMMENT1_ADD).newLine();
        }
        stream.write(HtmlTag.HEADER4, COMMENT3).newLine();
    }

    private void writePeriodToPeriod(HtmlStream stream) throws IOException {
        if (mul) {
            stream.write(HtmlTag.HEADER2, "Period to period growth rates").newLine();
        } else {
            stream.write(HtmlTag.HEADER2, "Period to period variations").newLine();
        }

        double[] re = new double[np];
        double[] te = new double[np];
        double ser = decomposition_.getModel().getSer();
        if (!decomposition_.getUcarimaModel().getComponent(1).isNull()) {
            TsData sa = finalDecomposition_.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            for (int i = 0; i < re.length; ++i) {
                re[i] = ser * Math.sqrt(decomposition_.getWienerKolmogorovEstimators().variationPrecision(1, i, 1, false));
                te[i] = ser * Math.sqrt(decomposition_.getWienerKolmogorovEstimators().variationPrecision(1, i, 1, true));
            }
            stream.write(HtmlTag.HEADER2, "Seasonally adjusted series").newLine();
            writePeriodToPeriod(stream, sa, re, te);
        }
        if (!decomposition_.getUcarimaModel().getComponent(0).isNull()) {
            TsData t = finalDecomposition_.getSeries(ComponentType.Trend, ComponentInformation.Value);
            for (int i = 0; i < re.length; ++i) {
                re[i] = ser * Math.sqrt(decomposition_.getWienerKolmogorovEstimators().variationPrecision(0, i, 1, false));
                te[i] = ser * Math.sqrt(decomposition_.getWienerKolmogorovEstimators().variationPrecision(0, i, 1, true));
            }
            stream.write(HtmlTag.HEADER2, "Trend").newLine();
            writePeriodToPeriod(stream, t, re, te);
        }
    }

    private void writePeriodToPeriod(HtmlStream stream, TsData s, double[] re, double[] te) throws IOException {
        TsData gs = mul ? s.pctVariation(1) : s.delta(1);
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Period").withWidth(100));
        if (mul) {
            stream.write(new HtmlTableCell("Growth").withWidth(100));
        } else {
            stream.write(new HtmlTableCell("Changes").withWidth(100));
        }
        stream.write(new HtmlTableCell("Std error (revisions)").withWidth(100));
        stream.write(new HtmlTableCell("Std error (total error)").withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        for (int i = gs.getLength() - 1, j = 0; i >= gs.getLength() - np; --i, ++j) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(gs.getDomain().get(i).toString()).withWidth(100));
            if (mul) {
                stream.write(new HtmlTableCell(pc2.format(gs.get(i) * .01)).withWidth(100));
                stream.write(new HtmlTableCell(pc2.format(Math.exp(re[j]) - 1)).withWidth(100));
                stream.write(new HtmlTableCell(pc2.format(Math.exp(te[j]) - 1)).withWidth(100));
            } else {
                stream.write(new HtmlTableCell(dg6.format(gs.get(i))).withWidth(100));
                stream.write(new HtmlTableCell(dg6.format(re[j])).withWidth(100));
                stream.write(new HtmlTableCell(dg6.format(te[j])).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
    }
}
