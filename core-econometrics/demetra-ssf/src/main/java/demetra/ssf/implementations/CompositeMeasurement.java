/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
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

import demetra.data.Cell;
import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfMeasurement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import demetra.data.DataBlockIterator;

/**
 *
 * @author Jean Palate
 */
public class CompositeMeasurement implements ISsfMeasurement {

    public static ISsfMeasurement of(ISsf... ssf) {
        return CompositeMeasurement.of(0, ssf);
    }

    public static ISsfMeasurement of(double var, ISsf... ssf) {
        ISsfMeasurement[] l = new ISsfMeasurement[ssf.length];
        for (int i = 0; i < ssf.length; ++i) {
            l[i] = ssf[i].getMeasurement();
            if (l[i].hasErrors()) {
                return null;
            }
        }
        return new CompositeMeasurement(CompositeSsf.dimensions(ssf), l, var);
    }

    private final ISsfMeasurement[] measurements;
    private final int[] dim;
    private final double var;
    private final DataBlock tmp;

    CompositeMeasurement(final int[] dim, final ISsfMeasurement[] ms, double var) {
        this.measurements = ms;
        this.dim = dim;
        int n = ms.length;
        int tdim = 0;
        for (int i = 0; i < n; ++i) {
            tdim += dim[i];
        }
        this.var = var;
        tmp = DataBlock.make(tdim);
    }

    public List<ISsfMeasurement> getMeasurements() {
        return Arrays.asList(measurements);
    }

    public int getComponentsCount() {
        return measurements.length;
    }

    public void ZX(int pos, DataBlock x, DataBlock zx) {
        DataWindow cur = x.left();
        for (int i = 0; i < measurements.length; ++i) {
            zx.set(i, measurements[i].ZX(pos, cur.next(dim[i])));
        }
    }

    @Override
    public boolean isTimeInvariant() {
        for (int i = 0; i < measurements.length; ++i) {
            if (!measurements[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean areErrorsTimeInvariant() {
        for (int i = 0; i < measurements.length; ++i) {
            if (!measurements[i].areErrorsTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        DataWindow cur = z.left();
        for (int i = 0; i < measurements.length; ++i) {
            measurements[i].Z(pos, cur.next(dim[i]));
        }
    }

    @Override
    public boolean hasErrors() {
        return var != 0;
    }

    @Override
    public boolean hasError(int pos) {
        return var != 0;
    }

    @Override
    public double errorVariance(int pos) {
        return var;
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        DataWindow cur = m.left();
        double x = 0;
        for (int i = 0; i < measurements.length; ++i) {
            x += measurements[i].ZX(pos, cur.next(dim[i]));
        }
        return x;
    }

    @Override
    public double ZVZ(int pos, Matrix v) {
        MatrixWindow D = v.topLeft();
        double x = 0;
        for (int i = 0; i < measurements.length; ++i) {
            int ni = dim[i];
            tmp.set(0);
            DataWindow wnd = tmp.left();
            D.next(ni, ni);
            x += measurements[i].ZVZ(pos, D);
            MatrixWindow C = D.clone();
            for (int j = i + 1; j < measurements.length; ++j) {
                int nj = dim[j];
                DataBlock cur = wnd.next(nj);
                C.vnext(nj);
                measurements[j].ZM(pos, C, cur);
                x += 2 * measurements[i].ZX(pos, cur);
            }
        }
        return x;
    }

    @Override
    public void VpZdZ(int pos, Matrix V, double d) {
        tmp.set(0);
        Z(pos, tmp);
        DataBlockIterator cols = V.columnsIterator();
        Cell cell = tmp.cells();
        while (cols.hasNext()) {
            cols.next().addAY(cell.next(), tmp);
        }
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        DataWindow cur = x.left();
        for (int i = 0; i < measurements.length; ++i) {
            measurements[i].XpZd(pos, cur.next(dim[i]), d);
        }
    }

}
