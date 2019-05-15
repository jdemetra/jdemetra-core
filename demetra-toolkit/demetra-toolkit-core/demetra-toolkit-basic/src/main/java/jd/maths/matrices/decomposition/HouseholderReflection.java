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

package jd.maths.matrices.decomposition;

import jd.data.DataBlock;
import demetra.design.Development;
import demetra.maths.Constants;

/**
 * A Householder reflection is represented by a matrix of the form H = I - 2/(v'v) * vv'
 * v is called the householder vector. 
 *
 * This implementation always uses a transformation that projects x on (|x|,
 * 0...0)
 *
 * See Golub. Van Loan, §5.1
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class HouseholderReflection implements IVectorTransformation {

    private double beta, mu;
    private DataBlock vector;

    /**
     * Creates a new Householder reflection that transform the given array to 
     * [a 0...0]
     *
     * @param v The vector used to compute the transformation. The vector
     * is modified to [a 0...0].
     * @return 
     */
    public static HouseholderReflection of(DataBlock v) {
        HouseholderReflection reflection = new HouseholderReflection();
        reflection.householder(v);
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
     * Gets the euclidian norm copyOf x
     *
     * @return
     */
    public double getNrm2() {
        return mu;
    }

    /**
     * Gets the coefficient beta (=2/(v'v))
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
        prepare();
        x.set(0.0);
        x.set(0, mu);
    }

    private void prepare() {

        int n = vector.length();
        if (n == 1) {
            return;
        }

        double[] v = vector.getStorage();
        int beg = vector.getStartPosition(), end = vector.getEndPosition(), inc = vector.getIncrement();
        double sig = 0;
        double x0 = v[beg];
        for (int i = beg + inc; i != end; i += inc) {
            sig += v[i] * v[i];
        }
        if (sig < Constants.getEpsilon()) {
            mu=Math.abs(x0);
            return; // nothing to do...
        }
        mu = Math.sqrt(sig + x0 * x0);

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
     * Computes y = H(y) = y - beta*v*v'*y
     * = y - v * (beta*v'y) 
     */
    public void transform(DataBlock y) {
        if (beta == 0) {
            return;
        }
        // v'y
        double vy = y.dot(vector);
        y.addAY(-beta * vy, vector);
    }
}
