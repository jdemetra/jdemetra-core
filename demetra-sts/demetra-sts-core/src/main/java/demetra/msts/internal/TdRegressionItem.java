/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import demetra.modelling.regression.GenericTradingDaysVariable;
import demetra.modelling.regression.Regression;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceInterpreter;
import demetra.ssf.SsfComponent;
import demetra.ssf.implementations.RegSsf;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.Collections;
import java.util.List;
import demetra.msts.ParameterInterpreter;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class TdRegressionItem extends AbstractModelItem {

    private final CanonicalMatrix x;
    private final CanonicalMatrix mvar;
    private final VarianceInterpreter v;

    public TdRegressionItem(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
        super(name);
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        this.x = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        this.mvar = generateVar(dc, contrast);
        this.v = new VarianceInterpreter(name + ".var", var, fixed, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double pvar = p.get(0);
            CanonicalMatrix xvar = mvar.deepClone();
            xvar.mul(pvar);
            SsfComponent cmp = RegSsf.ofTimeVarying(x, xvar);
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

}
