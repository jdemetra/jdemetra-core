/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.implementations;

import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.ISsfLoading;
import demetra.ssf.multivariate.ISsfErrors;
import demetra.ssf.univariate.ISsfError;

/**
 *
 * @author Jean Palate
 */
public class TimeInvariantMeasurements implements ISsfMeasurements {

    private final Matrix Z;
    private final ISsfErrors errors;

    public static TimeInvariantMeasurements of(int dim, ISsfMeasurements measurements) {
        if (!measurements.isTimeInvariant()) {
            return null;
        }
        int m = measurements.getCount();
        Matrix Z = Matrix.make(m, dim);
        for (int i = 0; i < m; ++i) {
            measurements.loading(i).Z(0, Z.row(i));
        }
        ISsfErrors errors = measurements.errors();
        if (errors == null) {
            return new TimeInvariantMeasurements(Z, null, null);
        }
        Matrix H = Matrix.square(m), R = Matrix.square(m);
        errors.H(0, H);
        errors.R(0, R);
        return new TimeInvariantMeasurements(Z, H, R);

    }

    public static TimeInvariantMeasurements of(int dim, ISsfMeasurement measurement) {
        if (!measurement.isTimeInvariant()) {
            return null;
        }
        Matrix Z = Matrix.make(1, dim);
        measurement.loading().Z(0, Z.row(0));
        ISsfError error = measurement.error();
        if (error == null) {
            return new TimeInvariantMeasurements(Z, null, null);
        }
        double v = error.at(0);
        Matrix H = Matrix.square(1);
        H.set(0, 0, v);
        Matrix R = Matrix.square(1);
        R.set(0, 0, Math.sqrt(v));
        return new TimeInvariantMeasurements(Z, H, R);

    }

    public TimeInvariantMeasurements(Matrix Z, Matrix H, Matrix R) {
        this.Z = Z;
        if (H == null && R == null) {
            errors = null;
        } else if (H != null && H.isDiagonal()) {
            errors = MeasurementsError.of(H.diagonal());
        } else {
            errors = MeasurementsError.of(H, R);
        }

    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public int getCount() {
        return Z.getRowsCount();
    }

    @Override
    public ISsfLoading loading(int equation) {
        return new TimeInvariantLoading(Z.row(equation));
    }

    @Override
    public ISsfErrors errors() {
        return errors;
    }

}
