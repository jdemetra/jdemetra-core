/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.math.highprecision;

/**
 *
 * @author Jean Palate
 */
public strictfp class DoubleDoubleComputer implements DoubleDoubleType {

    /**
     * The value to split a double-precision value on during multiplication
     */
    private static final double SPLIT = 134217729.0D;    // 2^27+1, for IEEE double

    private double high, low;

    public DoubleDoubleComputer() {
        this.high = 0;
        this.low = 0;
    }

    public DoubleDoubleComputer(double x) {
        this.high = x;
        this.low = 0;
    }

    public DoubleDoubleComputer(DoubleDoubleType x) {
        this.high = x.getHigh();
        this.low = x.getLow();
    }

    public DoubleDoubleComputer(double high, double low) {
        this.high = high;
        this.low = low;
    }

    @Override
    public double getHigh() {
        return high;
    }

    @Override
    public double getLow() {
        return low;
    }

    public DoubleDoubleComputer set(final DoubleDoubleType dd) {
        this.high = dd.getHigh();
        this.low = dd.getLow();
        return this;
    }

    public DoubleDoubleComputer set(final double high, final double low) {
        this.high = high;
        this.low = low;
        return this;
    }

    public DoubleDoubleComputer set(final double d) {
        this.high = d;
        this.low = 0;
        return this;
    }

    @Override
    public double asDouble() {
        return high + low;
    }

    public DoubleDouble result() {
        return new DoubleDouble(high, low);
    }

    /**
     * Adds the argument to the value of <tt>this</tt>.
     *
     * @param yhi
     * @param ylo
     * @return <tt>this</tt>, with its value incremented by <tt>y</tt>
     */
    DoubleDoubleComputer add(final double yhi, final double ylo) {
        double S = high + yhi;
        double T = low + ylo;
        double e = S - high;
        double f = T - low;
        double s = S - e;
        double t = T - f;
        s = yhi - e + (high - s);
        t = ylo - f + (low - t);
        e = s + T;
        double H = S + e;
        double h = e + (S - H);
        e = t + h;

        double zhi = H + e;
        double zlo = e + (H - zhi);
        high = zhi;
        low = zlo;
        return this;
    }

    /**
     * Adds the product of the argument to the value of <tt>this</tt>.
     *
     * @param yhi
     * @param ylo
     * @return <tt>this</tt>, with its value incremented by <tt>y</tt>
     */
    DoubleDoubleComputer addXY(final double xhi, final double xlo, final double yhi, final double ylo) {
        double tmpHi=high, tmpLo=low;
        high=xhi;
        low=xlo;
        return mul(yhi, ylo).add(tmpHi, tmpLo);
    }

    DoubleDoubleComputer addXY(final DoubleDoubleType x, final DoubleDoubleType y) {
        double tmpHi=high, tmpLo=low;
        high=x.getHigh();
        low=x.getLow();
        return mul(y).add(tmpHi, tmpLo);
    }
    /**
     * Subtracts the product of the argument to the value of <tt>this</tt>.
     *
     * @param yhi
     * @param ylo
     * @return <tt>this</tt>, with its value incremented by <tt>y</tt>
     */
    DoubleDoubleComputer subXY(final double xhi, final double xlo, final double yhi, final double ylo) {
        double tmpHi=high, tmpLo=low;
        high=xhi;
        low=xlo;
        return mul(yhi, ylo).chs().add(tmpHi, tmpLo);
    }

    DoubleDoubleComputer subXY(final DoubleDoubleType x, final DoubleDoubleType y) {
        double tmpHi=high, tmpLo=low;
        high=x.getHigh();
        low=x.getLow();
        return mul(y).chs().add(tmpHi, tmpLo);
    }
    /**
     * Adds the product of the argument to the value of <tt>this</tt>.
     *
     * @param yhi
     * @param ylo
     * @return <tt>this</tt>, with its value incremented by <tt>y</tt>
     */
    DoubleDoubleComputer addaXY(final double a, final double xhi, final double xlo, final double yhi, final double ylo) {
        double tmpHi=high, tmpLo=low;
        high=xhi;
        low=xlo;
        return mul(yhi, ylo).mul(a).add(tmpHi, tmpLo);
    }

    public DoubleDoubleComputer add(final double y) {
        return add(y, 0);
    }

    public DoubleDoubleComputer add(final DoubleDoubleType y) {
        return add(y.getHigh(), y.getLow());
    }

    DoubleDoubleComputer sub(final double yhi, final double ylo) {
        return add(-yhi, -ylo);
    }

    DoubleDoubleComputer sub(final double yhi) {
        return add(-yhi, 0);
    }

    public DoubleDoubleComputer sub(final DoubleDoubleType y) {
        return add(-y.getHigh(), -y.getLow());
    }

    /**
     * Multiplies this by the argument, returning this.
     *
     * @param yhi
     *
     * @param ylo
     *
     * a DoubleDouble value to multiply by
     * @return this
     */
    DoubleDoubleComputer mul(final double yhi, final double ylo) {
        double C = SPLIT * high;
        double hx = C - high;
        double c = SPLIT * yhi;
        hx = C - hx;
        double tx = high - hx;
        double hy = c - yhi;
        C = high * yhi;
        hy = c - hy;
        double ty = yhi - hy;
        c = hx * hy - C + hx * ty + tx * hy + tx * ty + (high * ylo + low * yhi);
        high = C + c;
        hx = C - high;
        low = c + hx;
        return this;
    }

    public DoubleDoubleComputer mul(final double y) {
        return mul(y, 0);
    }

    public DoubleDoubleComputer mul(final DoubleDoubleType y) {
        return mul(y.getHigh(), y.getLow());
    }

    DoubleDoubleComputer div(final double yhi, final double ylo) {
        double C = high / yhi;
        double c = SPLIT * C;
        double hc = c - C;
        double u = SPLIT * yhi;
        hc = c - hc;
        double tc = C - hc;
        double hy = u - yhi;
        double U = C * yhi;
        hy = u - hy;
        double ty = yhi - hy;
        u = hc * hy - U + hc * ty + tc * hy + tc * ty;
        c = (high - U - u + low - C * ylo) / yhi;
        u = C + c;

        high = u;
        low = C - u + c;
        return this;
    }

    public DoubleDoubleComputer div(final double y) {
        return div(y, 0);
    }

    public DoubleDoubleComputer div(final DoubleDoubleType y) {
        return div(y.getHigh(), y.getLow());
    }

    /**
     * Returns a DoubleDouble whose value is <tt>1 / this</tt>.
     *
     * @return the reciprocal of this value
     */
    public DoubleDoubleComputer inv() {
        double C = 1.0 / high;
        double c = SPLIT * C;
        double hc = c - C;
        double u = SPLIT * high;
        hc = c - hc;
        double tc = C - hc;
        double hy = u - high;
        double U = C * high;
        hy = u - hy;
        double ty = high - hy;
        u = hc * hy - U + hc * ty + tc * hy + tc * ty;
        c = (1.0 - U - u - C * low) / high;

        high = C + c;
        low = C - high + c;
        return this;
    }

    /**
     * Returns a DoubleDouble whose value is <tt>- this</tt>.
     *
     * @return the reciprocal of this value
     */
    public DoubleDoubleComputer chs() {
        high = -high;
        low = -low;
        return this;
    }

    public DoubleDoubleComputer abs() {
        if (isNegative()) {
            return chs();
        } else {
            return this;
        }
    }

    /**
     * Computes the square of this value.
     *
     * @return the square of this value.
     */
    public DoubleDoubleComputer square() {
        return mul(high, low);
    }

    /**
     * Computes the positive square root of this value. If the number is NaN or
     * negative, NaN is returned.
     *
     * @return the positive square root of this number. If the argument is NaN
     * or less than zero, the result is NaN.
     */
    public DoubleDoubleComputer sqrt() {
        /*
         * Strategy: Use Karp's trick: if x is an approximation to sqrt(a), then
         * 
         * sqrt(a) = a*x + [a - (a*x)^2] * x / 2 (approx)
         * 
         * The approximation is accurate to twice the accuracy of x. Also, the
         * multiplication (a*x) and [-]*x can be done with only half the
         * precision.
         */

        if (isZero()) {
            set(0);
            return this;
        }

        if (isNegative()) {
            set(Double.NaN);
            return this;
        }

        double x = 1.0 / Math.sqrt(high);
        double ax = high * x;

        DoubleDoubleComputer sq = new DoubleDoubleComputer(ax);
        sq.square();
        sub(sq.high, sq.low);
        double d2 = high * (x * 0.5);
        set(ax);
        add(d2, 0);
        return this;
    }

}
