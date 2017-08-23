/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.regarima.internals;

import demetra.arima.IArimaModel;
import demetra.arima.StationaryTransformation;
import demetra.arima.regarima.RegArimaModel;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.design.Immutable;
import demetra.eco.EcoException;
import demetra.linearmodel.LinearModel;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.IFilterOutput;
import demetra.maths.matrices.Matrix;
import java.util.List;

/**
 * Linear model with stationary ARMA process
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <M>
 */
@Immutable
@lombok.Value
public class RegArmaModel<M extends IArimaModel> {

    public static <M extends IArimaModel> RegArmaModel<M> of(RegArimaModel<M> regarima) {
        StationaryTransformation<M> st = (StationaryTransformation<M>) regarima.arima().stationaryTransformation();
        M arma = st.getStationaryModel();
        BackFilter ur = st.getUnitRoots();
        int d = ur.length() - 1;
        int n = regarima.getObservationsCount();
        int ndy = n - d;
        if (ndy <= 0) {
            throw new EcoException(EcoException.NOT_ENOUGH_OBS);
        }

        DoubleSequence y = regarima.getY();
        boolean mean = regarima.isMean();
        List<DoubleSequence> x = regarima.getX();
        int[] missing = regarima.missing();
        int nx = regarima.getMissingValuesCount() + regarima.getVariablesCount();
        Matrix dx = Matrix.make(ndy, nx);
        double[] dy;
        // dy
        if (d > 0) {
            dy = new double[y.length() - d];
            ur.apply(i -> y.get(i), IFilterOutput.of(dy, d));
        } else {
            dy = y.toArray();
        }
        // dx
        if (nx > 0) {
            DataBlockIterator cols = dx.columnsIterator();
            if (d > 0) {
                if (missing.length > 0) {
                    DoubleSequence coeff = ur.asPolynomial().coefficients().reverse();
                    for (int i = 0; i < missing.length; ++i) {
                        DataBlock col = cols.next();
                        if (missing[i] >= dy.length) {
                            col.range(missing[i] - d, dy.length).copy(coeff.drop(0, y.length() - missing[i]));
                        } else if (missing[i] >= d) {
                            col.range(missing[i] - d, missing[i] + 1).copy(coeff);
                        } else {
                            col.range(0, missing[i] + 1).copy(coeff.drop(d - missing[i], 0));
                        }
                    }
                }
                if (mean) {
                    cols.next().set(1);
                }
                for (DoubleSequence var : x) {
                    ur.apply(i -> var.get(i), IFilterOutput.of(cols.next(), d));
                }
            } else {
                for (int i = 0; i < missing.length; ++i) {
                    cols.next().set(missing[i], 1);
                }
                if (mean) {
                    cols.next().set(1);
                }
                for (DoubleSequence var : x) {
                    ur.apply(i -> var.get(i), IFilterOutput.of(cols.next(), d));
                }
            }
        }
        return new RegArmaModel<>(DoubleSequence.ofInternal(dy), arma, dx, missing.length);

    }

    /**
     * The differenced (interpolated) observations. Should not contain missing values
     * (handled by additive outliers)
     */
    @lombok.NonNull
    DoubleSequence y;
    /**
     * The stationary model
     */
    @lombok.NonNull
    M arma;
    /**
     * The differenced regression variables. Contains successively
     * the additive outliers corresponding to the missing values,
     * the constant
     * the other regression variables
     */
    Matrix x;
    /**
     * Number of missing observations (additive outliers at the beginning of x)
     */
    int missingCount;
    
    public LinearModel asLineaModel(){
        return new LinearModel(y.toArray(), false, x);
    }

}
