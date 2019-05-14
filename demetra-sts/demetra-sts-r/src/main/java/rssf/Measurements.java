/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.DataBlock;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.ssf.implementations.Loading;
import demetra.ssf.implementations.TimeInvariantLoading;
import demetra.ssf.implementations.TimeInvariantMeasurements;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Measurement;
import demetra.maths.matrices.MatrixType;

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
        return new TimeInvariantMeasurements(CanonicalMatrix.of(Z), CanonicalMatrix.of(H), null);
    }

}
