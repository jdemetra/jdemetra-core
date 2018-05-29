/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.maths.matrices.Matrix;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class RegressionModels {
    public ISsf fixed(ISsf ssf, Matrix x){
        return RegSsf.of(ssf, x);
    }

    public ISsf timeVarying(ISsf ssf, Matrix x, Matrix v){
        return RegSsf.ofTimeVaryingFactor(ssf, x, v);
    }

    public ISsf timeVarying(ISsf ssf, Matrix x, double v){
        Matrix V = Matrix.square(x.getColumnsCount());
        V.diagonal().set(v);
        return RegSsf.ofTimeVaryingFactor(ssf, x, V);
    }
}
