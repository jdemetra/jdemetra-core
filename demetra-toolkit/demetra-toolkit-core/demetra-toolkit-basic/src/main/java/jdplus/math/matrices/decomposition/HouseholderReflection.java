/*
* Copyright 2013 National Bank copyOf Belgium
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
package jdplus.math.matrices.decomposition;

import jdplus.data.DataBlock;
import nbbrd.design.Development;
import demetra.math.Constants;
import jdplus.math.matrices.DataPointer;
import jdplus.math.matrices.Matrix;

/**
 * A Householder reflection is represented by a matrix of the form H = I -
 * [2/(v'v)] * vv' v is called the householder vector.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class HouseholderReflection implements IVectorTransformation {

    private final double alpha; //+-||x|| (=Hv)
    private final double beta;
    private final double[] px;

    public static HouseholderReflection of(DataBlock v) {
        return new HouseholderReflection(v.toArray());
    }

    public static HouseholderReflection of(DataBlock v, boolean apply) {
        HouseholderReflection hr = new HouseholderReflection(v.toArray());
        if (apply) {
            v.set(0);
            v.set(0, hr.alpha);
        }
        return hr;
    }

    public double x0() {
        return px[0];
    }

    public DataPointer v() {
        return DataPointer.of(px, 1);
    }

    public DataPointer x() {
        return DataPointer.of(px, 0);
    }

    HouseholderReflection(double[] w) {
        this.px = w;
        int m = w.length - 1;
        switch (m) {
            case -1:
                alpha = 0;
                beta = 0;
                break;
            case 0:
                alpha = Math.abs(px[0]);
                beta = 0;
                break;
            default:
                double x0 = px[0];
                DataPointer v = v(),
                 x = x();
                if (v.test(m, q -> q == 0)) {
                    alpha = Math.abs(x0);
                    beta = 0;
                } else {
                    double nrm = x.norm2(px.length);
                    double eps = Constants.getEpsilon();
                    double safemin = Constants.getSafeMin() / eps;
                    int k = 0;
                    if (nrm < safemin) {
                        double rsafemin = 1 / safemin;
                        do {
                            v.mul(m, rsafemin);
                            x0 *= rsafemin;
                            nrm *= rsafemin;
                        } while (nrm < safemin && ++k < 4);
                        nrm = x.norm2(px.length);
                    }
                    if (x0 < 0) {
                        nrm = -nrm;
                    }
                    for (int j = 0; j < k; ++j) {
                        nrm *= safemin;
                    }
                    beta = nrm / (nrm + x0);
                    v().div(m, nrm);
                    alpha = -nrm;

                    // beta = -+ || x ||
                }
        }
    }

    void lapply(Matrix M) {
        if (beta == 0) {
            return;
        }
        int nc = M.getColumnsCount(), lda = M.getColumnIncrement(), m = px.length - 1, mstart = M.getStartPosition();
        double[] pm = M.getStorage();
        for (int k = 0, im = mstart; k < nc; ++k, im += lda) {
            double s = pm[im] / beta;
            for (int i = 1, j = im + 1; i < px.length; ++i, ++j) {
                s += pm[j] * px[i];
            }
            if (s != 0) {
                pm[im] -= s;
                s *= -beta;
                for (int i = 1, j = im + 1; i < px.length; ++i, ++j) {
                    pm[j] += s * px[i];
                }
            }
        }
    }

    @Override
    /**
     * Computes y = H(y) = y - beta*v*v'*y = y - v * (beta*v'y)
     */
    public void transform(DataBlock y) {
        if (beta == 0) {
            return;
        }
        double[] py = y.getStorage();
        int inc = y.getIncrement(), im = y.getStartPosition();
        double s = py[im] / beta;
        for (int i = 1, j = im + inc; i < px.length; ++i, j += inc) {
            s += py[j] * px[i];
        }
        if (s != 0) {
            py[im] -= s;
            s *= -beta;
            for (int i = 1, j = im + inc; i < px.length; ++i, j += inc) {
                py[j] += s * px[i];
            }
        }
    }
}
