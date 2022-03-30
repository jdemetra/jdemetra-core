/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.FastMatrix;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.implementations.Coefficients;
import jdplus.ssf.implementations.Loading;
import demetra.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class RegressionItem extends StateItem {

    public final FastMatrix x;
    public final VarianceInterpreter[] v;

    public RegressionItem(String name, Matrix x, final double[] vars, final boolean fixed) {
        super(name);
        this.x = FastMatrix.of(x);
        if (vars == null) {
            v = null;
        } else {
            v = new VarianceInterpreter[vars.length];
            if (v.length == 1) {
                v[0] = new VarianceInterpreter(name + ".var", vars[0], fixed, true);
            } else {
                for (int i = 0; i < v.length; ++i) {
                    v[i] = new VarianceInterpreter(name + ".var" + (i + 1), vars[i], fixed, true);
                }
            }
        }
    }
    
    private RegressionItem(RegressionItem item){
        super(item.name);
        this.x=item.x;
        if (item.v == null)
            v=null;
        else{
            v=new VarianceInterpreter[item.v.length];
            for (int i=0; i<v.length; ++i){
                v[i]=item.v[i].duplicate();
            }
        }
    }
    
    @Override
    public RegressionItem duplicate(){
        return new RegressionItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (v == null) {
            mapping.add((p, builder) -> {
                StateComponent cmp = Coefficients.fixedCoefficients(x.getColumnsCount());
                builder.add(name, cmp, Loading.regression(x));
                return 0;
            });
        } else if (v.length == 1) {
            mapping.add(v[0]);
            mapping.add((p, builder) -> {
                StateComponent cmp = Coefficients.timeVaryingCoefficients(DoubleSeq.onMapping(x.getColumnsCount(), j->p.get(0)));
                builder.add(name, cmp, Loading.regression(x));
                return 1;
            });
        } else {
            for (int i = 0; i < v.length; ++i) {
                mapping.add(v[i]);
            }
            mapping.add((p, builder) -> {
                StateComponent cmp = Coefficients.timeVaryingCoefficients(   p.extract(0, v.length));
                builder.add(name, cmp, Loading.regression(x));
                return v.length;
            });
        }
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        if (v == null) {
            return Collections.emptyList();
        } else if (v.length == 1) {
            return Collections.singletonList(v[0]);
        } else {
            return Arrays.asList(v);
        }
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        if (v == null) {
            return Coefficients.fixedCoefficients(x.getColumnsCount());
        } else if (v.length == 1){
            return Coefficients.timeVaryingCoefficients(DoubleSeq.onMapping(x.getColumnsCount(), j->p.get(0)));
        } else{
            return Coefficients.timeVaryingCoefficients(p.extract(0, v.length));
        }
    }

    @Override
    public int parametersCount() {
        return v == null ? 0 : v.length;
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
    
    @Override
    public int stateDim(){
        return x.getColumnsCount();
    }

    
}
