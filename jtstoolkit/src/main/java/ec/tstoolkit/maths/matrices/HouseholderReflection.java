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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 * A Householder reflection is a matrix of the form H = I - 2/(v'v) * vv' v is
 * called the householder vector. 
 *
 * This implementation always uses a transformation that projects x on (|x|,
 * 0...0)
 *
 * See Golub. Van Loan, §5.1
 *
 * It doesn't make any rescaling (an improvement for next release)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class HouseholderReflection implements IVectorTransformation {

    private double beta_, mu_;
    private DataBlock v_;

    /**
     * Creates a new Householder reflection that transform the given array to 
     * [a 0...0]
     *
     * @param vector The vector used to compute the transformation. The vector
     * is unmodified.
     */
    public static HouseholderReflection from(DataBlock v) {
        HouseholderReflection reflection = new HouseholderReflection();
        reflection.householder(v);
        return reflection;
    }

    /**
     * Creates a new Householder reflection that transform the given array to 
     * [a 0...0]
     *
     * @param v The vector used to compute the transformation. The vector
     * is modified "in place". The vector v is rescaled so that v(0)=1.
     * After the processing, v(0) contains and the
     */
    public static HouseholderReflection inPlace(DataBlock v) {
        HouseholderReflection reflection = new HouseholderReflection();
        reflection.v_=v.clone();
        reflection.inPlaceHouseholder();
        return reflection;

    }

    /**
     * Gets the reflection vector (v)
     *
     * @return
     */
    public DataBlock getHouseholderVector() {
        return v_;
    }

    /**
     * Gets the euclidian norm of x
     *
     * @return
     */
    public double getNrm2() {
        return mu_;
    }

    /**
     * Gets the coefficient beta (=2/(v'v))
     *
     * @return
     */
    public double getBeta() {
        return beta_;
    }

    private void householder(DataBlock x) {

        int n = x.getLength();
        if (n == 1) {
            x.set(0, Math.abs(x.get(0)));
            return;
        }
        v_ = x.deepClone();
        inPlaceHouseholder();
        x.set(0);
        x.set(0, mu_);
    }

    private void inPlaceHouseholder() {

        int n = v_.getLength();
        if (n == 1) {
            return;
        }

        double[] v = v_.getData();
        int beg = v_.getStartPosition(), end = v_.getEndPosition(), inc = v_.getIncrement();
        double sig = 0;
        for (int i = beg + inc; i != end; i += inc) {
            sig += v[i] * v[i];
        }
        if (sig < EPS) {
            return; // nothing to do...
        }
        double x0 = v[beg];
        mu_ = Math.sqrt(sig + x0 * x0);

        double v0;
        if (x0 <= 0) {
            v0 = x0 - mu_;
        } else {
            v0 = -sig / (x0 + mu_);
        }

        beta_ = 2 / (sig + v0 * v0);
        v[beg] = v0;
    }

    @Override
    /**
     * Computes y = H(y) = y - beta*v*v'*y
     * = y - v * (beta*v'y) 
     */
    public void transform(DataBlock y) {
        if (beta_ == 0) {
            return;
        }
        // v'y
        double vy = y.dot(v_);
        y.addAY(-beta_ * vy, v_);
    }
}
