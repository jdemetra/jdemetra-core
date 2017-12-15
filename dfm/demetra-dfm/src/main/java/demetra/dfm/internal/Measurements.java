/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.dfm.internal;

import demetra.data.DataBlock;
import demetra.dfm.IDfmMeasurement;
import demetra.dfm.MeasurementDescriptor;
import demetra.maths.matrices.Matrix;
import demetra.ssf.implementations.TimeInvariantMeasurements;
import demetra.ssf.multivariate.ISsfMeasurements;

/**
 *
 * @author Jean Palate
 */
class Measurements extends TimeInvariantMeasurements {

    static Measurements of(int nf, int c, MeasurementDescriptor... mdesc) {
        return new Measurements(mdesc, nf, c);
    }

    private final MeasurementDescriptor[] mdesc;
    private final int nf, c;

    private static Matrix Z(MeasurementDescriptor[] mdesc, int nf, int c) {
        int mdim = nf * c, vdim = mdesc.length;
        Matrix Z = Matrix.make(vdim, mdim);
        // Measurement
        for (int i = 0; i < vdim; ++i) {
            MeasurementDescriptor zdesc = mdesc[i];
            DataBlock z = Z.row(i);
            for (int j = 0, start = 0; j < nf; ++j, start += c) {
                if (mused(zdesc, j)) {
                    IDfmMeasurement m = zdesc.getType();
                    DataBlock cur = z.range(start, start + m.getLength());
                    m.fill(cur);
                    cur.mul(zdesc.getCoefficient(j));
                }
            }
        }
        return Z;

    }

    private static Matrix H(MeasurementDescriptor[] mdesc) {
        Matrix h = Matrix.square(mdesc.length);

        DataBlock diagonal = h.diagonal();
        for (int i = 0; i < mdesc.length; ++i) {
            diagonal.set(i, mdesc[i].getVar());
        }
        return h;
    }

    private static Matrix R(MeasurementDescriptor[] mdesc) {
        Matrix r = Matrix.square(mdesc.length);

        DataBlock diagonal = r.diagonal();
        for (int i = 0; i < mdesc.length; ++i) {
            diagonal.set(i, Math.sqrt(mdesc[i].getVar()));
        }
        return r;
    }

    private Measurements(MeasurementDescriptor[] mdesc, int nf, int c) {
        super(Z(mdesc, nf, c), H(mdesc), R(mdesc));
        this.mdesc = mdesc;
        this.nf = nf;
        this.c = c;
    }

    private static boolean mused(MeasurementDescriptor m, int i) {
        double z = m.getCoefficient(i);
        return z != 0 && !Double.isNaN(z);
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public int getCount(int pos) {
        return mdesc.length;
    }

    @Override
    public int getMaxCount() {
        return mdesc.length;
    }

    @Override
    public boolean isHomogeneous() {
        return true;
    }

    @Override
    public boolean hasErrors() {
        return true;
    }

    @Override
    public boolean hasIndependentErrors() {
        return true;
    }

    @Override
    public boolean hasError(int pos) {
        return true;
    }

    @Override
    public double ZX(int pos, int var, DataBlock m) {
        MeasurementDescriptor zdesc = mdesc[var];
        double r = 0;
        for (int j = 0, start = 0; j < nf; ++j, start += c) {
            if (mused(zdesc, j)) {
                IDfmMeasurement dfm = zdesc.getType();
                DataBlock cur = m.range(start, start + dfm.getLength());
                r += zdesc.getCoefficient(j) * dfm.dot(cur);
            }
        }
        return r;
    }

    @Override
    public void addH(int pos, Matrix V) {
        DataBlock diagonal = V.diagonal();
        for (int i = 0; i < mdesc.length; ++i) {
            diagonal.add(i, mdesc[i].getVar());
        }
    }

}
