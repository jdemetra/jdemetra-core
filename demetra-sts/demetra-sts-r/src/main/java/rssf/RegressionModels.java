/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.math.matrices.Matrix;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.ISsf;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class RegressionModels {

    public ISsf fixed(ISsf ssf, Matrix x) {
        return RegSsf.ssf(ssf, x);
    }

    public ISsf timeVarying(ISsf ssf, MatrixType x, MatrixType v) {
        return RegSsf.timeVaryingSsf(ssf, Matrix.of(x), Matrix.of(v));
    }

    public ISsf timeVarying(ISsf ssf, Matrix x, double[] v) {
        Matrix V = Matrix.square(x.getColumnsCount());
        V.diagonal().copyFrom(v, 0);
        return RegSsf.timeVaryingSsf(ssf, Matrix.of(x), V);
    }
}
