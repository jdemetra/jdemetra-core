/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.maths.matrices.MatrixType;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class RegressionModels {
    public ISsf fixed(ISsf ssf, MatrixType x){
        return RegSsf.of(ssf, FastMatrix.of(x));
    }

    public ISsf timeVarying(ISsf ssf, MatrixType x, MatrixType v){
        return RegSsf.ofTimeVaryingFactor(ssf, FastMatrix.of(x), FastMatrix.of(v));
    }

    public ISsf timeVarying(ISsf ssf, MatrixType x, double v){
        FastMatrix V = FastMatrix.square(x.getColumnsCount());
        V.diagonal().set(v);
        return RegSsf.ofTimeVaryingFactor(ssf, FastMatrix.of(x), V);
    }
}
