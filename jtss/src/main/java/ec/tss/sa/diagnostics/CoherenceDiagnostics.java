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
package ec.tss.sa.diagnostics;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.GenericSaProcessingFactory;
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.YearIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public final class CoherenceDiagnostics implements IDiagnostics {

    private double maxDA_, maxDDef_;
    private double eb_ = .5;
    private double ub_ = .01, bb_ = 0.05, sb_ = .1;
    private double lDef_ = 1e-6;
    private int yshort_ = 7;
    private boolean mul_;
    private boolean short_;
    private static String SHORTSERIES = "Short series";

    static CoherenceDiagnostics create(CoherenceDiagnosticsConfiguration config, CompositeResults rslts) {
        try {
            if (rslts == null || GenericSaResults.getDecomposition(rslts, ISaResults.class) == null) {
                return null;
            } else {
                return new CoherenceDiagnostics(config, rslts);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private CoherenceDiagnostics(CoherenceDiagnosticsConfiguration config, CompositeResults rslts) {
        // set the boundaries...
        eb_ = config.getError();
        sb_ = config.getSevere();
        bb_ = config.getBad();
        ub_ = config.getUncertain();
        lDef_ = config.getTolerance();
        yshort_ = config.getShortSeries();
        test(rslts);
    }

    private void test(CompositeResults rslts) {
        TsData yl = rslts.getData(ModellingDictionary.Y_LIN, TsData.class);
        if (yl != null && yl.getLength() < yshort_ * yl.getFrequency().intValue()) {
            short_ = true;
        }

        TsData y = rslts.getData(ModellingDictionary.YC, TsData.class);
        TsData regy = rslts.getData(ModellingDictionary.REG_Y, TsData.class);
        TsData regsa = rslts.getData(ModellingDictionary.REG_SA, TsData.class);
        TsData sa = rslts.getData(ModellingDictionary.SA, TsData.class);
        TsData s = rslts.getData(ModellingDictionary.S, TsData.class);
        TsData t = rslts.getData(ModellingDictionary.T, TsData.class);
        TsData i = rslts.getData(ModellingDictionary.I, TsData.class);
        TsData cy = rslts.getData(ModellingDictionary.Y_CMP, TsData.class);
        TsData ct = rslts.getData(ModellingDictionary.T_CMP, TsData.class);
        TsData cs = rslts.getData(ModellingDictionary.S_CMP, TsData.class);
        TsData ci = rslts.getData(ModellingDictionary.I_CMP, TsData.class);
        TsData csa = rslts.getData(ModellingDictionary.SA_CMP, TsData.class);
        TsData ly = rslts.getData(ModellingDictionary.Y_LIN, TsData.class);
        TsData lt = rslts.getData(ModellingDictionary.T_LIN, TsData.class);
        TsData ls = rslts.getData(ModellingDictionary.S_LIN, TsData.class);
        TsData li = rslts.getData(ModellingDictionary.I_LIN, TsData.class);
        TsData lsa = rslts.getData(ModellingDictionary.SA_LIN, TsData.class);
        TsData tde = rslts.getData(ModellingDictionary.TDE, TsData.class);
        TsData ee = rslts.getData(ModellingDictionary.EE, TsData.class);
        TsData omhe = rslts.getData(ModellingDictionary.OMHE, TsData.class);
        TsData cal = rslts.getData(ModellingDictionary.CAL, TsData.class);
        TsData outs = rslts.getData(ModellingDictionary.OUT_S, TsData.class);
        TsData regs = rslts.getData(ModellingDictionary.REG_S, TsData.class);
        TsData outt = rslts.getData(ModellingDictionary.OUT_T, TsData.class);
        TsData regt = rslts.getData(ModellingDictionary.REG_T, TsData.class);
        TsData outi = rslts.getData(ModellingDictionary.OUT_I, TsData.class);
        TsData regi = rslts.getData(ModellingDictionary.REG_I, TsData.class);

        ISeriesDecomposition decomposition = GenericSaResults.getFinalDecomposition(rslts);
        mul_ = decomposition.getMode() != DecompositionMode.Additive;
        // main constraints
        TsData yc=inv_op(y, regy);
        // finals
        TsData df0 = sub(yc, op(t, s, i,regsa));
        TsData df1 = sub(sa, op(t, i, regsa));
        TsData df2 = sub(inv_op(y, regy), op(sa, s));
        TsData df3 = sub(s, op(cs, cal, regs, outs));
        TsData df4 = sub(t, op(ct, regt, outt));
        TsData df5 = sub(i, op(ci, regi, outi));
        TsData dcal = sub(cal, op(tde, ee, omhe));
        // components
        TsData dc0 = sub(cy, op(ct, cs, ci));
        TsData dc1 = sub(csa, op(ct, ci));
        TsData dc2 = sub(cy, op(csa, cs));

        maxDDef_ = Double.NaN;
        check(df0);
        check(df1);
        check(df2);
        check(df3);
        check(df4);
        check(df5);
        check(dcal);
        check(dc0);
        check(dc1);
        check(dc2);
        // lin
        if (lsa != null) {
            TsData dl0 = sub(ly, add(lt, ls, li));
            TsData dl1 = sub(lsa, add(lt, li));
            TsData dl2 = sub(ly, add(lsa, ls));
            check(dl0);
            check(dl1);
            check(dl2);
        }
        // annual totals
        YearIterator yiter = YearIterator.fullYears(yc);
        YearIterator saiter = YearIterator.fullYears(sa);

        maxDA_ = 0;
        while (yiter.hasMoreElements() && saiter.hasMoreElements()) {
            TsDataBlock ydb = yiter.nextElement();
            TsDataBlock sadb = saiter.nextElement();
            double dcur = Math.abs(ydb.data.sum() - sadb.data.sum());
            if (dcur > maxDA_) {
                maxDA_ = dcur;
            }
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(y);
        double q = stats.getRmse();
        maxDA_ /= y.getFrequency().intValue() * q;
    }

    private TsData op(TsData l, TsData r) {
        if (mul_) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData op(TsData a, TsData b, TsData c) {
        if (mul_) {
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
        if (mul_) {
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
            if (Double.isNaN(maxDDef_)) {
                return ProcQuality.Error;
            }
            return maxDDef_ < lDef_ ? ProcQuality.Good : ProcQuality.Error;
        } else {
            if (Double.isNaN(maxDA_) || maxDA_ > eb_) {
                return ProcQuality.Error;
            } else if (maxDA_ > sb_) {
                return ProcQuality.Severe;
            } else if (maxDA_ > bb_) {
                return ProcQuality.Bad;
            } else if (maxDA_ > ub_) {
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
            val = maxDDef_;
        } else {
            val = maxDA_;
        }
        return val;
    }

    @Override
    public List<String> getWarnings() {
        if (short_) {
            return Collections.singletonList(SHORTSERIES);
        } else {
            return null;
        }
    }

    public double getDefinitionTolerance() {
        return lDef_;
    }

    public int getShortSeries() {
        return yshort_;
    }

    public double getLowerBound(ProcQuality quality) {
        switch (quality) {
            case Error:
                return eb_;
            case Severe:
                return sb_;
            case Bad:
                return bb_;
            case Uncertain:
                return ub_;
            default:
                return 0;
        }
    }

    private void check(TsData d) {
        if (d == null) {
            return;
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(d);
        double dmax = Math.max(Math.abs(stats.getMax()), Math.abs(stats.getMin()));
        if (Double.isNaN(maxDDef_) || dmax > maxDDef_) {
            maxDDef_ = dmax;
        }
    }
}
