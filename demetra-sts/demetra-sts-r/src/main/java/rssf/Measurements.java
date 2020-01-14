/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.implementations.TimeInvariantLoading;
import jdplus.ssf.implementations.TimeInvariantMeasurements;
import jdplus.ssf.multivariate.ISsfMeasurements;
import jdplus.ssf.univariate.ISsfMeasurement;
import jdplus.ssf.univariate.Measurement;
import demetra.math.matrices.MatrixType;

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

    public ISsfMeasurements of(MatrixType Z, MatrixType H) {
        return new TimeInvariantMeasurements(Matrix.of(Z), Matrix.of(H), null);
    }

}
