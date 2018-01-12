/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package demetra.maths.matrices.internal;

import demetra.data.DataBlock;
import demetra.maths.matrices.decomposition.IVectorTransformation;
import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.Constants;

/**
 * A Householder reflection is a matrix ofInternal the form H = I - 2/(v'Jv) * (vv'J) 
 v is called the householder vector. Transforms x = (a, b) into (c, 0) such that
 * a*a - b*b = c*c We consider here that a*a &gt b*b. J-unitary transformations
 * are considered. if a*a = b*b, the transformation is | 1 0| | 0 -1|
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class HyperbolicHouseholderReflection implements IVectorTransformation {

    private final int pos;
    private double beta, mu;
    private DataBlock vector;

    private HyperbolicHouseholderReflection(int pos) {
        this.pos = pos;
    }

    /**
     * Creates a new Householder reflection that transform the given array to [a
     * 0...0]
     *
     * @param v The vector used to compute the transformation. The vector
     * is unmodified.
     * @param pos
     * @return 
     */
    public static HyperbolicHouseholderReflection of(DataBlock v, int pos) {
        HyperbolicHouseholderReflection reflection = new HyperbolicHouseholderReflection(pos);
        reflection.householder(v);
        return reflection;
    }

    /**
     * Creates a new Householder reflection that transform the given array to [a
     * 0...0]
     *
     * @param v The vector used to compute the transformation. The vector is
     * modified "in place". The vector v is not rescaled.
     * @param pos
     * @return 
     */
    public static HyperbolicHouseholderReflection inPlace(DataBlock v, int pos) {
        HyperbolicHouseholderReflection reflection = new HyperbolicHouseholderReflection(pos);
        reflection.vector = v;
        reflection.inPlaceHouseholder();
        return reflection;

    }

    /**
     * Gets the reflection vector (v)
     *
     * @return
     */
    public DataBlock getHouseholderVector() {
        return vector;
    }

    /**
     * Gets the euclidian norm ofInternal x
     *
     * @return
     */
    public double getNrm2() {
        return mu;
    }

    /**
     * Gets the coefficient beta (=2/(v'Jv))
     *
     * @return
     */
    public double getBeta() {
        return beta;
    }

    private void householder(DataBlock x) {

        int n = x.length();
        if (n == 1) {
            x.set(0, Math.abs(x.get(0)));
            return;
        }
        vector = DataBlock.of(x);
        inPlaceHouseholder();
        x.set(()->0);
        x.set(0, mu);
    }

    private void inPlaceHouseholder() {

        int n = vector.length();
        if (n == 1) {
            return;
        }

        double[] v = vector.getStorage();
        int beg = vector.getStartPosition(), end = vector.getEndPosition(), inc = vector.getIncrement();
        int pend = beg + inc * pos;
        double sigp = 0, sign = 0;
        for (int i = beg + inc; i != pend; i += inc) {
            sigp += v[i] * v[i];
        }
        for (int i = pend; i != end; i += inc) {
            sign += v[i] * v[i];
        }
        if (sigp < Constants.getEpsilon() && sign < Constants.getEpsilon()) {
            return; // nothing to do...
        }
        double x0 = v[beg];
        double sig = x0 * x0 + sigp - sign;
        mu = Math.sqrt(sig);

        double v0;
        if (x0 <= 0) {
            v0 = x0 - mu;
        } else {
            v0 = -sig / (x0 + mu);
        }
        beta = 2 / (sig + v0 * v0);
        v[beg] = v0;
    }

    @Override
    /**
     * Computes y = H(y) = y - beta*v*v'*J*y = y - v * (beta*v'Jy)
     */
    public void transform(DataBlock y) {
        if (beta == 0) {
            return;
        }
        // v'y
        double vy = y.jdot(vector, pos);
        y.addAY(-beta * vy, vector);
    }
    
//    public static boolean triangularize(final SubMatrix M, int npos) {
//        try {
//            int r = M.getRowsCount(), c = M.getColumnsCount();
//            SubMatrix L = M;
//            do {
//                reflection(L.rows(), npos--);
//                L = L.extract(1, r--, 1, c--);
//            } while (!L.isEmpty());
//            return true;
//        } catch (BaseException err) {
//            return false;
//        }
//
//    }
//
//    public static void reflection(DataBlockIterator vectors, int npos) {
//        DataBlock cur = vectors.getData();
//        HyperbolicHouseholderReflection reflection = HyperbolicHouseholderReflection.from(cur, npos);
//        while (vectors.next()) {
//            reflection.transform(cur);
//        }
//    }
 
}
