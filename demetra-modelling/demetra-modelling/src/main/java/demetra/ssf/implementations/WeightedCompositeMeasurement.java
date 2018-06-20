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

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DataWindow;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.data.DoubleReader;

/**
 *
 * @author Jean Palate
 */
public class WeightedCompositeMeasurement implements ISsfMeasurement {

    public static interface IWeights {

        double get(int cmp, int pos);

        boolean isTimeInvariant();
    };

    public static ISsfMeasurement of(IWeights weights, ISsf... ssf) {
        return of(weights, 0, ssf);
    }

    public static ISsfMeasurement of(IWeights weights, double var, ISsf... ssf) {
         if (weights == null) {
            return CompositeMeasurement.of(var, ssf);
        }
        ISsfMeasurement[] m=new ISsfMeasurement[ssf.length]; 
        for (int i=0; i< ssf.length; ++i) {
            ISsfMeasurement cur=ssf[i].getMeasurement();
            if (var != 0 && cur.hasErrors()) {
                return null;
            }
            m[i]=cur;
        }
        return new WeightedCompositeMeasurement(CompositeSsf.dimensions(ssf), m, weights, var);
    }

    private final ISsfMeasurement[] measurements;
    private final IWeights weights;
    private final int[] dim;
    private final double var;
    private final DataBlock tmp;

    WeightedCompositeMeasurement(final int[] dim, final ISsfMeasurement[] ms, final IWeights weights, double var) {
        this.measurements = ms;
        this.dim=dim;
        this.weights = weights;
        int n = ms.length;
        int tdim = 0;
        for (int i = 0; i < n; ++i) {
            tdim += dim[i];
        }
        this.var = var;
        tmp = DataBlock.make(tdim);
    }

    @Override
    public boolean isTimeInvariant() {
        if (!weights.isTimeInvariant()) {
            return false;
        }
        for (int i = 0; i < measurements.length; ++i) {
            if (!measurements[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        DataWindow wnd = z.left();
        for (int i = 0; i < measurements.length; ++i) {
            double wi = weights.get(i, pos);
            DataBlock cur = wnd.next(dim[i]);
            if (wi != 0) {
                measurements[i].Z(pos, cur);
                cur.mul(wi);
            }
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
    public boolean areErrorsTimeInvariant(){
        return true;
    }

    @Override
    public double errorVariance(int pos) {
        return var;
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        DataWindow wnd = m.left();
        double x = 0;
        for (int i = 0; i < measurements.length; ++i) {
            DataBlock cur = wnd.next(dim[i]);
            double wi = weights.get(i, pos);
            if (wi != 0) {
                x += measurements[i].ZX(pos, cur) * wi;
            }
        }
        return x;
    }

    @Override
    public double ZVZ(int pos, Matrix v) {
        MatrixWindow D = v.topLeft();
        double x = 0;
        for (int i = 0; i < measurements.length; ++i) {
            int ni = dim[i];
            D.next(ni, ni);
            double wi = weights.get(i, pos);
            if (wi != 0) {
                tmp.set(0);
                DataWindow wnd = tmp.left();
                x += measurements[i].ZVZ(pos, D) * wi * wi;
                MatrixWindow C = D.clone();
                for (int j = i + 1; j < measurements.length; ++j) {
                    int nj = dim[j];
                    DataBlock cur = wnd.next(nj);
                    C.vnext(nj);
                    double wj = weights.get(j, pos);
                    if (wj != 0) {
                        measurements[j].ZM(pos, C, cur);
                        x += 2 * wi * wj * measurements[i].ZX(pos, cur);
                    }
                }
            }
        }
        return x;
    }

    @Override
    public void VpZdZ(int pos, Matrix V, double d) {
        tmp.set(0);
        Z(pos, tmp);
        DoubleReader cell = tmp.reader();
        DataBlockIterator cols = V.columnsIterator();
        while (cols.hasNext()) {
            cols.next().addAY(d * cell.next(), tmp);
        }
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        DataWindow wnd = x.left();
        for (int i = 0; i < measurements.length; ++i) {
            DataBlock cur = wnd.next(dim[i]);
            double wi = weights.get(i, pos);
            if (wi != 0) {
                measurements[i].XpZd(pos, cur, d * wi);
            }
        }
    }

}
