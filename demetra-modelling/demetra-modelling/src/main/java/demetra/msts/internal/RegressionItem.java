/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.msts.IMstsParametersBlock;
import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceParameter;
import demetra.ssf.SsfComponent;
import demetra.ssf.implementations.RegSsf;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author palatej
 */
public class RegressionItem extends AbstractModelItem {

    public final Matrix x;
    public final VarianceParameter[] v;

    public RegressionItem(String name, MatrixType x, final double[] vars, final boolean fixed) {
        super(name);
        this.x = Matrix.of(x);
        if (vars == null) {
            v = null;
        } else {
            v = new VarianceParameter[vars.length];
            if (v.length == 1) {
                v[0] = new VarianceParameter(name + ".var", vars[0], fixed, true);
            } else {
                for (int i = 0; i < v.length; ++i) {
                    v[i] = new VarianceParameter(name + ".var" + (i + 1), vars[i], fixed, true);
                }
            }
        }
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (v == null) {
            mapping.add((p, builder) -> {
                SsfComponent cmp = RegSsf.of(x);
                builder.add(name, cmp);
                return 0;
            });
        } else if (v.length == 1) {
            mapping.add((p, builder) -> {
                SsfComponent cmp = RegSsf.ofTimeVarying(x, p.get(0));
                builder.add(name, cmp);
                return 1;
            });
        } else {
            mapping.add((p, builder) -> {
                SsfComponent cmp = RegSsf.ofTimeVarying(x, p.extract(0, v.length));
                builder.add(name, cmp);
                return v.length;
            });
        }
    }

    @Override
    public List<IMstsParametersBlock> parameters() {
        if (v == null) {
            return Collections.EMPTY_LIST;
        } else if (v.length == 1) {
            return Collections.singletonList(v[0]);
        } else {
            return Arrays.asList(v);
        }
    }
}
