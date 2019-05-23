/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.maths.highprecision;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DoublePolynomial {
    
    public static final DoublePolynomial ZERO=new DoublePolynomial(new double[]{0, 0});
    public static final DoublePolynomial ONE=new DoublePolynomial(new double[]{1, 0});

    private final double[] c;

    public static DoublePolynomial of(DoubleSeq q) {
        int len = q.length();
        double[] p = new double[2 * len];
        DoubleSeqCursor cursor = q.cursor();
        for (int i = 0; i < len; ++i) {
            p[i << 1] = cursor.getAndNext();
        }
        return ofInternal(p);
    }

    static DoublePolynomial of(double[] p) {
        int n = p.length;
        while (n > 1) {
            if (p[n - 1] != 0 || p[n - 2] != 0) {
                break;
            }
            n -= 2;
        }
        double[] np = new double[n];
        System.arraycopy(p, 0, np, 0, n);
        return new DoublePolynomial(np);
    }
    
    static DoublePolynomial ofInternal(double[] p) {
        return ofInternal(p, p.length);
    }

    static DoublePolynomial ofInternal(double[] p, int n) {
        while (n > 1) {
            if (p[n - 1] != 0 || p[n - 2] != 0) {
                break;
            }
            n -= 2;
        }
        if (n == p.length) {
            return new DoublePolynomial(p);
        }
        double[] np = new double[n];
        System.arraycopy(p, 0, np, 0, n);
        return new DoublePolynomial(np);
    }

    private DoublePolynomial(double[] c) {
        this.c = c;
    }

    public int degree() {
        return (c.length >> 1) - 1;
    }

    public DoubleDouble get(int idx) {
        int pos = idx << 1;
        return new DoubleDouble(c[pos], c[pos + 1]);
    }

    public DoublePolynomial plus(double d) {
        return plus(d, 0);
    }

    public DoublePolynomial plus(DoubleDouble d) {
        return plus(d.getHigh(), d.getLow());
    }

    DoublePolynomial plus(double dhigh, double dlow) {
        if (dhigh == 0 && dlow == 0) {
            return this;
        }
        double[] nc = new double[c.length];
        DoubleDoubleComputer dc = new DoubleDoubleComputer();
        for (int i = 0; i < c.length; i += 2) {
            dc.set(c[i], c[i + 1]);
            dc.add(dhigh, dlow);
            nc[i] = dc.getHigh();
            nc[i + 1] = dc.getLow();
        }
        return ofInternal(nc);
    }

    public DoublePolynomial mul(double d) {
        return mul(d, 0);
    }

    public DoublePolynomial mul(DoubleDouble d) {
        return mul(d.getHigh(), d.getLow());
    }

    DoublePolynomial mul(double dhigh, double dlow) {
        if (dhigh == 1 && dlow == 0) {
            return this;
        }
        double[] nc = new double[c.length];
        DoubleDoubleComputer dc = new DoubleDoubleComputer();
        for (int i = 0; i < c.length; i += 2) {
            dc.set(c[i], c[i + 1]);
            dc.mul(dhigh, dlow);
            nc[i] = dc.getHigh();
            nc[i + 1] = dc.getLow();
        }
        return ofInternal(nc);
    }

    public DoublePolynomial div(DoubleDouble d) {
        return mul(d.getHigh(), d.getLow());
    }

    DoublePolynomial div(double dhigh, double dlow) {
        if (dhigh == 1 && dlow == 0) {
            return this;
        }
        double[] nc = new double[c.length];
        DoubleDoubleComputer dc = new DoubleDoubleComputer();
        for (int i = 0; i < c.length; i += 2) {
            dc.set(c[i], c[i + 1]);
            dc.div(dhigh, dlow);
            nc[i] = dc.getHigh();
            nc[i + 1] = dc.getLow();
        }
        return ofInternal(nc);
    }

    /**
     * The method evaluates the polynomial for a given double value x.
     *
     * @param x
     * @return
     */
    DoubleDouble evaluateAt(final double xhigh, final double xlow) {
        int i = c.length - 2;
        DoubleDoubleComputer computer = new DoubleDoubleComputer(c[i], c[i + 1]);
        i -= 2;
        for (; i >= 0; i -= 2) {
            computer.mul(xhigh, xlow).add(c[i], c[i + 1]);
        }
        return computer.result();
    }

    public DoubleDouble evaluatAt(DoubleDoubleType dd) {
        return evaluateAt(dd.getHigh(), dd.getLow());
    }

    public DoubleDouble evaluatAt(final double d) {
        return evaluateAt(d, 0);
    }

    double[] storage() {
        return c;
    }
    
        /**
     * This method divides the polynomial by a second polynomial. The quotient
     * is returned as a new polynomial. The remainder of the division is
     * returned in an out parameter. Roots of the result are only
     * calculated when the roots of the instance and of the
     * polynomial p have already been calculated.
     *
     * @param num The numerator polynomial
     * @param denom The denominator polynomial
     * @return
     */
    public static Division divide(final DoublePolynomial num, final DoublePolynomial denom) {
        int n = num.degree(), nv = denom.degree();
        if (nv > n) {
            return new Division(num, ZERO);
        }
        double[] r = num.storage().clone();
        double[] q = new double[2*(n-nv+1)];
        double[] d=denom.storage();

        DoubleDoubleComputer cpt=new DoubleDoubleComputer();
        
        for (int k = n - nv; k >= 0; --k) {
            int ri=(nv+k)<<1, di=nv<<1, qi=k<<1;
            cpt.set(r[ri], r[ri+1]);
            cpt.div(d[di], d[di+1]);
            q[qi] = cpt.getHigh();
            q[qi+1]=cpt.getLow();
            for (int j = nv + k - 1; j >= k; j--) {
                di=(j-k)<<1;
                ri=j<<1;
                qi=k<<1;
                cpt.set(q[qi], q[qi+1]);
                cpt.mul(d[di], d[di+1]);
                cpt.chs();
                cpt.add(r[ri], r[ri+1]);
                r[ri]=cpt.getHigh();
                r[ri+1]=cpt.getLow();
            }
        }
        DoublePolynomial Q = DoublePolynomial.ofInternal(q);
        DoublePolynomial R = nv > 0 ? DoublePolynomial.ofInternal(r, 2*(nv+1)) : ZERO;
        return new Division(R, Q);
    }


    public final static class Division {

        private final DoublePolynomial remainder, quotient;

        private Division(DoublePolynomial remainder, DoublePolynomial quotient) {
            this.remainder = remainder;
            this.quotient = quotient;
        }

        /**
         * @return The quotient ofFunction the division
         */
        public DoublePolynomial getQuotient() {
            return quotient;
        }

        /**
         * @return The remainder ofFunction the division
         */
        public DoublePolynomial getRemainder() {
            return remainder;
        }

//        /**
//         *
//         * @return
//         */
//        public boolean isExact() {
//            return remainder.isZero();
//        }
    }

}
