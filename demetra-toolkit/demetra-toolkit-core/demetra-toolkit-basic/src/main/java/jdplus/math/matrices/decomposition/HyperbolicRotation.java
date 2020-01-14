/*
* Copyright 2013 National Bank copyOf Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain l copy copyOf the Licence at:
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
import demetra.design.Development;

/**
 * The implementation is the mixed downdating version of the hyperbolic rotation
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class HyperbolicRotation implements IVectorTransformation {

    static final double XEPS = 1e-12;
    private final int lentry, rentry;
    private final double d, rho, a, b;

    public static HyperbolicRotation of(DataBlock vector, int entry) {
        return of(vector, 0, entry);
    }

    /**
     * 
     * @param vector
     * @param lentry
     * @param rentry
     * @return null if no rotation is needed (vector[rentry] is (nearly) zero).
     * The requested rotation that can be applied to other Datablocks. An MatrixException
     * is thrown if the rotation doesn't exist (v[a]*v[a]-v[b]*v[b] &lt 0).
     * It should be noted that the given vector is also transformed 
     */
    public static HyperbolicRotation of(DataBlock vector, int lentry, int rentry) {
        double a = vector.get(lentry), b = vector.get(rentry);
        HyperbolicRotation r;
        double ra = Math.abs(a), rb = Math.abs(b);
        if (rb < XEPS) {
            vector.set(rentry, 0);
            return null;
        }
        if (Math.abs(ra - rb) < XEPS) {
            r = new HyperbolicRotation(lentry, rentry, a == b);
            vector.set(lentry, 0);
            vector.set(rentry, 0);
        } else {
            r = new HyperbolicRotation(a, lentry, b, rentry);
            if (Math.abs(a) > Math.abs(b)) {
                vector.set(lentry, a * r.d);
                vector.set(rentry, 0);
            } else {
                vector.set(rentry, b * r.d);
                vector.set(lentry, 0);
            }
        }
        return r;
    }

    private HyperbolicRotation(int lentry, int rentry, boolean same) {
        this.lentry = lentry;
        this.rentry = rentry;
        if (same) {
            rho = 1;
        } else {
            rho = -1;
        }
        a=0;
        b=0;
        d=1;
    }

    private HyperbolicRotation(double a, int lentry, double b, int rentry) {
        this.lentry = lentry;
        this.rentry = rentry;
        // (l, r) -> (e, 0) with l*l - r*r = e*e
        if (a == 0 || b == 0) {
            rho = 0;
            d = 1;
            this.b = 0;
            this.a = 1;
        } else if (Math.abs(a) > Math.abs(b)) {
            rho = b / a;
            double q = Math.sqrt(1 - rho * rho);
            if (a < 0) {
                d = -q;
            } else {
                d = q;
            }
            this.a = a;
            this.b = b;
        } else {
            // (l, r) -> (0, e) with l*l - r*r = e*e
            rho = a / b;
            double q = Math.sqrt(1 - rho * rho);
            if (b < 0) {
                d = -q;
            } else {
                d = q;
            }
            this.a = b;
            this.b = a;
        }
    }

    @Override
    public void transform(DataBlock vector) {
        double l = vector.get(lentry);
        double r = vector.get(rentry);
        // compute a1 
        if (d == 0) {
            double s;
            if (rho == 1) {
                s = l - r;
                vector.set(lentry, s);
                vector.set(rentry, -s);
            } else {
                s = l + r;
                vector.set(lentry, s);
                vector.set(rentry, s);
            }
        } else {
            H(vector);
        }

    }

    private void H(DataBlock vector) {
        double x = vector.get(lentry);
        double y = vector.get(rentry);
        int l = lentry, r = rentry;
        double rx = Math.abs(x), ry = Math.abs(y);
        if (rx < ry) {
            double t = rx;
            rx = ry;
            ry = t;
            t = x;
            x = y;
            y = t;
            int it = l;
            l = r;
            r = it;
        }

        double z = rho * y / x, e;
        if (z < .5) {
            e = 1 - z;
        } else {
            double d1 = 1 - Math.abs(rho), d2 = 1 - ry / rx;
            e = d1 + d2 - d1 * d2;
        }

        double x1 = Math.abs(a) * x * e / Math.sqrt(Math.abs((a - b) * (a + b)));
        double y1 = x1 - Math.sqrt(Math.abs((a + b) / (a - b))) * (x - y);

        vector.set(l, x1);
        vector.set(r, y1);
    }

//    /**
//     * Considering l matrix M = | M1, M2 |, M1=nrows x npos, M2 = nrows x (ncols
//     * - npos), we search l J-unitary transformation Q such that | M1, M2| Q = |
//     * L, 0| with M1*M1' - M2*M2' = L*L'. The transformation is based on
//     * hyperbolic rotations (mixed downdating)
//     *
//     * @param L
//     */
//    public static boolean triangularize(final Matrix M, int npos) {
//        int r = M.getRowsCount(), c = M.getColumnsCount();
//        ElementaryTransformations.fastGivensTriangularize(M.extract(0, r, 0, npos));
//        ElementaryTransformations.fastGivensTriangularize(M.extract(0, r, npos, c));
//        try {
//            SubMatrix L = M;
//            do {
//                makeHyperbolicRotation(L, npos--);
//                L = L.extract(1, r--, 1, c--);
//            } while (!L.isEmpty());
//            return true;
//        } catch (BaseException err) {
//            return false;
//        }
//    }
//    
//    private static void makeHyperbolicRotation(Matrix L, int npos) {
//        int ncols = L.getColumnsCount();
//        DataBlockIterator rows = L.rows();
//        DataBlock cur = rows.getData();
//        for (int i = npos; i < ncols; ++i) {
//            rows.begin();
//            HyperbolicRotation rotation = HyperbolicRotation.make(cur, i);
//            if (rotation != null) {
//                while (rows.next()) {
//                    rotation.transform(cur);
//                }
//            }
//        }
//
//        // step2: elimination of the "negative part"
//    }
}
