/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import demetra.modelling.regression.GenericTradingDaysVariable;
import jdplus.modelling.regression.Regression;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import jdplus.ssf.SsfComponent;
import jdplus.ssf.implementations.RegSsf;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import demetra.maths.matrices.Matrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class TdRegressionItem extends StateItem {

    private final CanonicalMatrix x;
    private final CanonicalMatrix mvar;
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

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double pvar = p.get(0);
            SsfComponent cmp;
            if (mvar == null) {
                cmp = RegSsf.of(x);
            } else {
                CanonicalMatrix xvar = mvar.deepClone();
                xvar.mul(pvar);
                cmp = RegSsf.ofTimeVarying(x, xvar);
            }
            builder.add(name, cmp);
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
        CanonicalMatrix td = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        return td.unmodifiable();
    }

    public static Matrix rawTd(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.of(dc);
        CanonicalMatrix td = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        return td.unmodifiable();
    }

    public static CanonicalMatrix generateVar(DayClustering dc, boolean contrasts) {
        int groupsCount = dc.getGroupsCount();
        CanonicalMatrix full = CanonicalMatrix.square(7);
        if (!contrasts) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        CanonicalMatrix Q = CanonicalMatrix.make(groupsCount - 1, 7);
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
            return RegSsf.stateComponent(x.getColumnsCount());
        } else {
            CanonicalMatrix xvar = mvar.deepClone();
            xvar.mul(pvar);
            return RegSsf.stateComponent(xvar);
        }
    }

    @Override
    public int parametersCount() {
        return 1;
    }
    
    @Override
    public int stateDim(){
        return x.getColumnsCount();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        } else {
            return RegSsf.loading(x);
        }
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

}
