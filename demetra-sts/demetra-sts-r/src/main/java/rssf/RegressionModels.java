/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class RegressionModels {
    public ISsf fixed(ISsf ssf, MatrixType x){
        return RegSsf.of(ssf, Matrix.of(x));
    }

    public ISsf timeVarying(ISsf ssf, MatrixType x, MatrixType v){
        return RegSsf.ofTimeVaryingFactor(ssf, Matrix.of(x), Matrix.of(v));
    }

    public ISsf timeVarying(ISsf ssf, MatrixType x, double v){
        Matrix V = Matrix.square(x.getColumnsCount());
        V.diagonal().set(v);
        return RegSsf.ofTimeVaryingFactor(ssf, Matrix.of(x), V);
    }
}
