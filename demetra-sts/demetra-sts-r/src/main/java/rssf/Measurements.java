/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.basic.Loading;
import jdplus.ssf.basic.TimeInvariantLoading;
import jdplus.ssf.basic.TimeInvariantMeasurements;
import jdplus.ssf.multivariate.ISsfMeasurements;
import jdplus.ssf.univariate.ISsfMeasurement;
import jdplus.ssf.univariate.Measurement;
import demetra.math.matrices.Matrix;

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
        return new TimeInvariantMeasurements(FastMatrix.of(Z), FastMatrix.of(H), null);
    }

}
