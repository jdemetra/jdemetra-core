/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.implementations.TimeInvariantLoading;
import jdplus.ssf.implementations.TimeInvariantMeasurements;
import jdplus.ssf.multivariate.ISsfMeasurements;
import jdplus.ssf.univariate.ISsfMeasurement;
import jdplus.ssf.univariate.Measurement;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Measurements {

    public ISsfMeasurement of(int mpos, double var) {
        return new Measurement(Loading.fromPosition(mpos), var);
    }

    public ISsfMeasurement of(double[] Z, double var) {
        return new Measurement(new TimeInvariantLoading(DataBlock.of(Z)), var);
    }

    public ISsfMeasurements of(Matrix Z, Matrix H) {
        return new TimeInvariantMeasurements(CanonicalMatrix.of(Z), CanonicalMatrix.of(H), null);
    }

}
