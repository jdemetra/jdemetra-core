/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.modelling.regression.GenericTradingDaysVariables;
import demetra.modelling.regression.RegressionUtility;
import demetra.msts.IMstsParametersBlock;
import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceParameter;
import demetra.ssf.SsfComponent;
import demetra.ssf.implementations.RegSsf;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author palatej
 */
public class TdRegressionItem extends AbstractModelItem {

    private final Matrix x;
    private final Matrix mvar;
    private final VarianceParameter v;

    public TdRegressionItem(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
        super(name);
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        this.x = RegressionUtility.data(domain, new GenericTradingDaysVariables(gtd));
        this.mvar = generateVar(dc, contrast);
        this.v = new VarianceParameter(name + ".var", var, fixed, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double pvar = p.get(0);
            Matrix xvar = mvar.deepClone();
            xvar.mul(pvar);
            SsfComponent cmp = RegSsf.ofTimeVarying(x, xvar);
            builder.add(name, cmp);
            return 1;
        });
    }

    @Override
    public List<IMstsParametersBlock> parameters() {
        return Collections.singletonList(v);
    }

    public static MatrixType tdContrasts(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        Matrix x = RegressionUtility.data(domain, new GenericTradingDaysVariables(gtd));
        return x.unmodifiable();
    }

    public static MatrixType rawTd(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.of(dc);
        Matrix x = RegressionUtility.data(domain, new GenericTradingDaysVariables(gtd));
        return x.unmodifiable();
    }

    public static Matrix generateVar(DayClustering dc, boolean contrasts) {
        int groupsCount = dc.getGroupsCount();
        Matrix full = Matrix.square(7);
        if (!contrasts) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        Matrix Q = Matrix.make(groupsCount - 1, 7);
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
