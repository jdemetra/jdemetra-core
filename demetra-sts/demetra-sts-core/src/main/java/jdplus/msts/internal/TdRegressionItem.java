/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import demetra.data.DoubleSeq;
import jdplus.msts.StateItem;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import jdplus.ssf.basic.RegSsf;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.basic.Coefficients;
import jdplus.ssf.basic.Loading;
import demetra.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class TdRegressionItem extends StateItem {

    private final FastMatrix x;
    private final FastMatrix mvar;
    private final VarianceInterpreter v;

    public TdRegressionItem(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
        super(name);
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        this.x = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        this.v = new VarianceInterpreter(name + ".var", var, fixed, true);
        if (var == 0 && fixed) {
            this.mvar = null;
        } else {
            this.mvar = generateVar(dc, contrast);
        }
    }

    private TdRegressionItem(TdRegressionItem item) {
        super(item.name);
        this.x = item.x;
        this.mvar = item.mvar;
        this.v = item.v == null ? null : item.v.duplicate();
    }

    @Override
    public TdRegressionItem duplicate() {
        return new TdRegressionItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double pvar = p.get(0);
            StateComponent cmp;
            if (mvar == null) {
                cmp = Coefficients.fixedCoefficients(x.getColumnsCount());
            } else {
                FastMatrix xvar = mvar.deepClone();
                xvar.mul(pvar);
                cmp = Coefficients.timeVaryingCoefficients(xvar);
            }
            builder.add(name, cmp, Loading.regression(x));
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

    public static Matrix tdContrasts(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        FastMatrix td = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        return td.unmodifiable();
    }

    public static Matrix rawTd(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.raw(dc);
        FastMatrix td = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        return td.unmodifiable();
    }

    public static FastMatrix generateVar(DayClustering dc, boolean contrasts) {
        int groupsCount = dc.getGroupsCount();
        FastMatrix full = FastMatrix.square(7);
        if (!contrasts) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        FastMatrix Q = FastMatrix.make(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.XSXt(full, Q);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double pvar = p.get(0);
        if (mvar == null) {
            return Coefficients.fixedCoefficients(x.getColumnsCount());
        } else {
            FastMatrix xvar = mvar.deepClone();
            xvar.mul(pvar);
            return Coefficients.timeVaryingCoefficients(xvar);
        }
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return x.getColumnsCount();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        } else {
            return Loading.regression(x);
        }
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

}
