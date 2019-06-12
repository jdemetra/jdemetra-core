/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.ISsf;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class RegressionModels {
    public ISsf fixed(ISsf ssf, Matrix x){
        return RegSsf.of(ssf, CanonicalMatrix.of(x));
    }

    public ISsf timeVarying(ISsf ssf, Matrix x, Matrix v){
        return RegSsf.ofTimeVaryingFactor(ssf, CanonicalMatrix.of(x), CanonicalMatrix.of(v));
    }

    public ISsf timeVarying(ISsf ssf, Matrix x, double v){
        CanonicalMatrix V = CanonicalMatrix.square(x.getColumnsCount());
        V.diagonal().set(v);
        return RegSsf.ofTimeVaryingFactor(ssf, CanonicalMatrix.of(x), V);
    }
}
