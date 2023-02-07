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
package jdplus.sa.diagnostics;

import demetra.data.AggregationType;
import nbbrd.design.Development;
import demetra.processing.ProcQuality;
import demetra.sa.DecompositionMode;
import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import java.util.Collections;
import java.util.List;
import jdplus.stats.DescriptiveStatistics;
import demetra.information.Explorable;
import demetra.processing.Diagnostics;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.RegressionDictionaries;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public final class CoherenceDiagnostics implements Diagnostics {

    @lombok.Value
    public static class Input {

        /**
         *
         */
        DecompositionMode mode;
        /**
         * Results that can be consulted through a dictionary
         */
        Explorable result;
    }

    private final CoherenceDiagnosticsConfiguration config;
    private double maxAnnualDifference, maxDefinitionDifference;
    private boolean multiplicative;
    private boolean shortSeries;
    private double scale;
    private final static String SHORTSERIES = "Short series";

    public static CoherenceDiagnostics of(CoherenceDiagnosticsConfiguration config, Input data) {
        try {
            if (data == null) {
                return null;
            } else {
                return new CoherenceDiagnostics(config, data.result, data.getMode());
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private CoherenceDiagnostics(CoherenceDiagnosticsConfiguration config, Explorable rslts, DecompositionMode mode) {
        // set the boundaries...
        this.config = config;
        test(rslts, mode);
    }

    private String decompositionItem(String key) {
        return Dictionary.concatenate(SaDictionaries.DECOMPOSITION, key);
    }

    private void test(Explorable rslts, DecompositionMode mode) {
        TsData cy = rslts.getData(decompositionItem(SaDictionaries.Y_CMP), TsData.class);
        if (cy != null && cy.length() < config.getShortSeriesLimit() * cy.getAnnualFrequency()) {
            shortSeries = true;
        }
        multiplicative = mode != DecompositionMode.Additive;
        TsData y = rslts.getData(RegressionDictionaries.YC, TsData.class);
        if (y == null) {
            y = rslts.getData(RegressionDictionaries.Y, TsData.class);
        }
        DescriptiveStatistics ds = DescriptiveStatistics.of(y.getValues());
        scale = ds.getRmse();
        TsData yc = y;
        TsData sa = rslts.getData(SaDictionaries.SA, TsData.class);
        TsData s = rslts.getData(SaDictionaries.S, TsData.class);
        TsData t = rslts.getData(SaDictionaries.T, TsData.class);
        TsData i = rslts.getData(SaDictionaries.I, TsData.class);
        if (mode == DecompositionMode.PseudoAdditive) {
            // finals
            TsData df0 = sub(y, TsData.multiply(t, TsData.add(s, i).subtract(1)));
            TsData df1 = sub(sa, TsData.multiply(t, i));
            check(df0);
            check(df1);
        } else {
            TsData regy = rslts.getData(SaDictionaries.REG_Y, TsData.class);
            TsData regsa = rslts.getData(SaDictionaries.REG_SA, TsData.class);
            TsData ct = rslts.getData(decompositionItem(SaDictionaries.T_CMP), TsData.class);
            TsData cs = rslts.getData(decompositionItem(SaDictionaries.S_CMP), TsData.class);
            TsData ci = rslts.getData(decompositionItem(SaDictionaries.I_CMP), TsData.class);
            TsData csa = rslts.getData(decompositionItem(SaDictionaries.SA_CMP), TsData.class);
            TsData ly = rslts.getData(decompositionItem(SaDictionaries.Y_LIN), TsData.class);
            TsData lt = rslts.getData(decompositionItem(SaDictionaries.T_LIN), TsData.class);
            TsData ls = rslts.getData(decompositionItem(SaDictionaries.S_LIN), TsData.class);
            TsData li = rslts.getData(decompositionItem(SaDictionaries.I_LIN), TsData.class);
            TsData lsa = rslts.getData(decompositionItem(SaDictionaries.SA_LIN), TsData.class);
            TsData tde = rslts.getData(RegressionDictionaries.TDE, TsData.class);
            TsData ee = rslts.getData(RegressionDictionaries.EE, TsData.class);
            TsData omhe = rslts.getData(RegressionDictionaries.OMHE, TsData.class);
            TsData cal = rslts.getData(RegressionDictionaries.CAL, TsData.class);
            TsData outs = rslts.getData(SaDictionaries.OUT_S, TsData.class);
            TsData regs = rslts.getData(SaDictionaries.REG_S, TsData.class);
            TsData outt = rslts.getData(SaDictionaries.OUT_T, TsData.class);
            TsData regt = rslts.getData(SaDictionaries.REG_T, TsData.class);
            TsData outi = rslts.getData(SaDictionaries.OUT_I, TsData.class);
            TsData regi = rslts.getData(SaDictionaries.REG_I, TsData.class);

            // main constraints
            yc = inv_op(y, regy);
            // finals
            TsData df0 = sub(yc, op(t, s, i, regsa));
            TsData df1 = sub(sa, op(t, i, regsa));
            //TsData df2 = sub(inv_op(y, regy), op(sa, s)); REDUNDANT
            TsData df3 = sub(s, op(cs, cal, regs, outs));
            TsData df4 = sub(t, op(ct, regt, outt));
            TsData df5 = sub(i, op(ci, regi, outi));
            TsData dcal = sub(cal, op(tde, ee, omhe));
            // components
            TsData dc0 = sub(cy, op(ct, cs, ci));
            TsData dc1 = sub(csa, op(ct, ci));
            //TsData dc2 = sub(cy, op(csa, cs)); REDUNDANT

            maxDefinitionDifference = Double.NaN;
            check(df0);
            check(df1);
            //check(df2);
            check(df3);
            check(df4);
            check(df5);
            check(dcal);
            check(dc0);
            check(dc1);
            //check(dc2);
            // lin
            if (lsa != null) {
                TsData dl0 = sub(ly, add(lt, ls, li));
                TsData dl1 = sub(lsa, add(lt, li));
                //TsData dl2 = sub(ly, add(lsa, ls)); REDUNDANT
                check(dl0);
                check(dl1);
                //check(dl2);
            }
        }
        // annual totals
        TsData yca = yc.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        TsData saa = sa.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        maxAnnualDifference = 0;
        for (int k = 0; k < yca.length(); ++k) {
            double dcur = Math.abs(yca.getValue(k) - saa.getValue(k));
            if (dcur > maxAnnualDifference) {
                maxAnnualDifference = dcur;
            }
        }
        maxAnnualDifference /= y.getAnnualFrequency() * scale;
    }

    private TsData op(TsData l, TsData r) {
        if (multiplicative) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData op(TsData a, TsData b, TsData c) {
        if (multiplicative) {
            return TsData.multiply(a, TsData.multiply(b, c));
        } else {
            return TsData.add(a, TsData.add(b, c));
        }
    }

    private TsData add(TsData l, TsData r) {
        return TsData.add(l, r);
    }

    private TsData add(TsData a, TsData b, TsData c) {
        return TsData.add(a, TsData.add(b, c));
    }

    private TsData op(TsData a, TsData b, TsData c, TsData d) {
        return op(op(a, b), op(c, d));
    }

    private TsData inv_op(TsData l, TsData r) {
        if (multiplicative) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    private TsData sub(TsData l, TsData r) {
        return TsData.subtract(l, r);
    }

    @Override
    public String getName() {
        return CoherenceDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return CoherenceDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        if (test.equals(CoherenceDiagnosticsFactory.ALL.get(0))) {
            if (Double.isNaN(maxDefinitionDifference)) {
                return ProcQuality.Error;
            }
            return maxDefinitionDifference < config.getTolerance() ? ProcQuality.Good : ProcQuality.Error;
        } else {
            if (Double.isNaN(maxAnnualDifference) || maxAnnualDifference > config.getErrorThreshold()) {
                return ProcQuality.Error;
            } else if (maxAnnualDifference > config.getSevereThreshold()) {
                return ProcQuality.Severe;
            } else if (maxAnnualDifference > config.getBadThreshold()) {
                return ProcQuality.Bad;
            } else if (maxAnnualDifference > config.getUncertainThreshold()) {
                return ProcQuality.Uncertain;
            } else {
                return ProcQuality.Good;
            }
        }
    }

    @Override
    public double getValue(String test) {
        double val;
        if (test.equals(CoherenceDiagnosticsFactory.ALL.get(0))) {
            val = maxDefinitionDifference;
        } else {
            val = maxAnnualDifference;
        }
        return val;
    }

    @Override
    public List<String> getWarnings() {
        if (shortSeries) {
            return Collections.singletonList(SHORTSERIES);
        } else {
            return Collections.emptyList();
        }
    }

    public double getLowerBound(ProcQuality quality) {
        switch (quality) {
            case Error:
                return config.getErrorThreshold();
            case Severe:
                return config.getSevereThreshold();
            case Bad:
                return config.getBadThreshold();
            case Uncertain:
                return config.getUncertainThreshold();
            default:
                return 0;
        }
    }

    private void check(TsData d) {
        if (d == null || scale == 0) {
            return;
        }
        DescriptiveStatistics stats = DescriptiveStatistics.of(d.getValues());
        double dmax = Math.max(Math.abs(stats.getMax()), Math.abs(stats.getMin())) / scale;
        if (Double.isNaN(maxDefinitionDifference) || dmax > maxDefinitionDifference) {
            maxDefinitionDifference = dmax;
        }
    }
}
