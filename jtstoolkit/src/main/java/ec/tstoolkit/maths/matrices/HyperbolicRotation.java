/*
* Copyright 2013 National Bank of Belgium
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

package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;

/**
 * The implementation is the mixed downdating version of the hyperbolic rotation
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class HyperbolicRotation implements IVectorTransformation {

    static final double XEPS = 1e-12;
    private int lentry_, rentry_;
    private double d_, ro_, a_, b_;

    public static HyperbolicRotation create(DataBlock vector, int entry) {
        return create(vector, 0, entry);
    }

    public static HyperbolicRotation create(DataBlock vector, int lentry, int rentry) {
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
                vector.set(lentry, a * r.d_);
                vector.set(rentry, 0);
            } else {
                vector.set(rentry, b * r.d_);
                vector.set(lentry, 0);
            }
        }
        return r;
    }

    private HyperbolicRotation(int lentry, int rentry, boolean same) {
        lentry_ = lentry;
        rentry_ = rentry;
        if (same) {
            ro_ = 1;
        } else {
            ro_ = -1;
        }
    }

    private HyperbolicRotation(double a, int lentry, double b, int rentry) {
        lentry_ = lentry;
        rentry_ = rentry;
        // (a, b) -> (e, 0) with a*a - b*b = e*e
        if (a == 0 || b == 0) {
            ro_ = 0;
            d_ = 1;
            b_=0;
            a_=1;
        } else if (Math.abs(a) > Math.abs(b)) {
            ro_ = b / a;
            d_ = Math.sqrt(1 - ro_ * ro_);
            if (a < 0) {
                d_ = -d_;
            }
            a_ = a;
            b_ = b;
        } else {
            // (a, b) -> (0, e) with a*a - b*b = e*e
            ro_ = a / b;
            d_ = Math.sqrt(1 - ro_ * ro_);
            if (b < 0) {
                d_ = -d_;
            }
            a_ = b;
            b_ = a;
        }
    }

    @Override
    public void transform(DataBlock vector) {
        double a = vector.get(lentry_);
        double b = vector.get(rentry_);
        // compute a1 
        if (d_ == 0) {
            double s;
            if (ro_ == 1) {
                s = a - b;
                vector.set(lentry_, s);
                vector.set(rentry_, -s);
            } else {
                s = a + b;
                vector.set(lentry_, s);
                vector.set(rentry_, s);
            }
        } else {
            H(vector);
//            OD(vector, a, b);
//            double a1 = (a - ro_ * b) / d_;
//            double b1 = -ro_ * a1 + d_ * b;
//            double b2 = (b - ro_ * a) / d_;
//            double a2 = -ro_ * b2 + d_ * a;
//            vector.set(lentry_, .5 * (a1 + a2));
//            vector.set(rentry_, .5 * (b1 + b2));
        }


    }

    private void OD(DataBlock vector, double x, double y) {
        double x1 = x - y;
        double y1 = x + y;
        double z = (a_ + b_) / (a_ - b_);
        z=Math.sqrt(z);
        x1 *= .5 * z;
        y1 *= .5 / z;
        vector.set(lentry_, x1 + y1);
        vector.set(rentry_, y1 - x1);
    }

    private void H(DataBlock vector) {
        double x = vector.get(lentry_);
        double y = vector.get(rentry_);
        int l = lentry_, r = rentry_;
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

        double z = ro_ * y / x, e;
        if (z < .5) {
            e = 1 - z;
        } else {
            double d1 = 1 - Math.abs(ro_), d2 = 1 - ry / rx;
            e = d1 + d2 - d1 * d2;
        }

        double x1 = Math.abs(a_) * x * e / Math.sqrt(Math.abs((a_ - b_) * (a_ + b_)));
        double y1 = x1 - Math.sqrt(Math.abs((a_ + b_) / (a_ - b_))) * (x - y);

        vector.set(l, x1);
        vector.set(r, y1);
    }

    /**
     * Considering a matrix M = | M1, M2 |, M1=nrows x npos, M2 = nrows x (ncols
     * - npos), we search a J-unitary transformation Q such that | M1, M2| Q = |
     * L, 0| with M1*M1' - M2*M2' = L*L'. The transformation is based on
     * hyperbolic rotations (mixed downdating)
     *
     * @param L
     */
    public static boolean triangularize(final SubMatrix M, int npos) {
        int r = M.getRowsCount(), c = M.getColumnsCount();
        ElementaryTransformations.fastGivensTriangularize(M.extract(0, r, 0, npos));
        ElementaryTransformations.fastGivensTriangularize(M.extract(0, r, npos, c));
        try {
            SubMatrix L = M;
            do {
                makeHyperbolicRotation(L, npos--);
                L = L.extract(1, r--, 1, c--);
            } while (!L.isEmpty());
            return true;
        } catch (BaseException err) {
            return false;
        }
    }
    
    private static void makeHyperbolicRotation(SubMatrix L, int npos) {
        int ncols = L.getColumnsCount();
        DataBlockIterator rows = L.rows();
        DataBlock cur = rows.getData();
        for (int i = npos; i < ncols; ++i) {
            rows.begin();
            HyperbolicRotation rotation = HyperbolicRotation.create(cur, i);
            if (rotation != null) {
                while (rows.next()) {
                    rotation.transform(cur);
                }
            }
        }

        // step2: elimination of the "negative part"
    }

    public static double jhypotenuse(double x, double y) {
        // Purpose
        // =======
        // DLAPY2
        // Arguments
        // =========
        // X (input) DOUBLE PRECISION
        // Y (input) DOUBLE PRECISION
        // X and Y specify the values x and y.
        // =====================================================================
        double xabs = Math.abs(x);
        double yabs = Math.abs(y);
        double w = Math.max(xabs, yabs);
        double z = Math.min(xabs, yabs);
        if (z == 0) {
            return w;
        } else {
            double zw = z / w;
            return w * Math.sqrt(1 - zw * zw);
        }
    }
}
