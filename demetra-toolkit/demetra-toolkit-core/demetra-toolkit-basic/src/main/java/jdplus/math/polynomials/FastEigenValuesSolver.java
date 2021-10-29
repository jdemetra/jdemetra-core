/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.polynomials;

import demetra.math.Complex;
import demetra.math.Constants;
import java.util.Random;
import jdplus.math.matrices.GeneralMatrix;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class FastEigenValuesSolver implements RootsSolver {

    /**
     * |c -s| |s c|
     */
    static final class Rotator implements Cloneable {

        double c, s;

        Rotator(double c, double s) {
            this.c = c;
            this.s = s;
        }

        // Do nothing
        Rotator() {
            this.c = 1;
            this.s = 0;
        }

        FastMatrix asMatrix(int n, int pos) {
            FastMatrix M = FastMatrix.identity(n);
            fill(M.extract(pos, 2, pos, 2));
            return M;
        }

        void fill(FastMatrix m) {
            m.set(0, 0, c);
            m.set(0, 1, -s);
            m.set(1, 0, s);
            m.set(1, 1, c);
        }

        boolean simplify(double eps) {
            if (Math.abs(s) <= eps) {
                s = 0;
                c = c > 0 ? 1 : -1;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Rotator clone() {
            try {
                return (Rotator) super.clone();
            } catch (CloneNotSupportedException ex) {
                return null;
            }
        }

        /**
         * Fuses two rotators. The result is in the first one; the other one is
         * unmodified;
         *
         */
        public static void fuse(Rotator q1, Rotator q2) {
            double tmp = q1.c * q2.c - q1.s * q2.s;
            q1.s = q1.s * q2.c + q1.c * q2.s;
            q1.c = tmp;
        }

        public static void fuse(Rotator q1, Rotator q2, int type) {
            if (type == 0) {
                double tmp = q1.c * q2.c - q1.s * q2.s;
                q1.s = q1.s * q2.c + q1.c * q2.s;
                q1.c = tmp;
            } else {
                double tmp = q1.c * q2.c - q1.s * q2.s;
                q2.s = q1.s * q2.c + q1.c * q2.s;
                q2.c = tmp;

            }
        }

        public void clear() {
            c = 1;
            s = 0;
        }

        /**
         * Passes the givens rotation Q3 from the left down through Q1 and Q2.
         * It overwrites Q1, Q2 and Q3.
         *
         * @param q1
         * @param q2
         * @param q3
         */
        public static void turnover(Rotator q1, Rotator q2, Rotator q3) {
            double c1 = q1.c, s1 = q1.s;
            double c2 = q2.c, s2 = q2.s;
            double c3 = q3.c, s3 = q3.s;
            double a = s1 * c3 + c1 * c2 * s3;
            double b = s2 * s3;
            Rotator q = new Rotator();
            double nrm = givensRotation(a, b, q);
            double c4 = q.c, s4 = q.s;
            a = c1 * c3 - s1 * c2 * s3;
            b = nrm;
            givensRotation2(a, b, q);
            double c5 = q.c, s5 = q.s;
            double t1 = s1 * s2, t2 = -c1 * s2, t3 = c2;
            a = -t2 * s4 + t3 * c4;
            t2 = t2 * c4 + t3 * s4;
            b = -(-t1 * s5 + t2 * c5);
            givensRotation2(a, b, q);
            double c6 = q.c, s6 = q.s;
            q1.c = c5;
            q1.s = s5;
            q2.c = c6;
            q2.s = s6;
            q3.c = c4;
            q3.s = s4;
        }

        public void copy(Rotator rotator) {
            c = rotator.c;
            s = rotator.s;
        }

        private void copyInverse(Rotator rotator) {
            c = rotator.c;
            s = -rotator.s;
        }
    }

    private Complex[] roots;

    @Override
    public boolean factorize(Polynomial p) {
        n = p.degree();
        roots = new Complex[n];
        switch (n) {
            case 0:
                return true;
            case 1:
                monic(p);
                return true;
            case 2:
                quadratic(p);
                return true;
            default:
                qr(p);
                return true;
        }
    }

    @Override
    public Polynomial remainder() {
        return Polynomial.ZERO;
    }

    @Override
    public Complex[] roots() {
        return roots;
    }

    private void monic(Polynomial p) {
        double a = p.get(1), b = p.get(0);
        roots[0] = Complex.cart(-b / a);
    }

    private void quadratic(Polynomial p) {
        /* discr = p1^2-4*p2*p0 */
        double a = p.get(2), b = p.get(1), c = p.get(0), aa = 2 * a;
        double rdiscr = b * b - 4 * a * c;
        if (rdiscr < 0) {
            double z = Math.sqrt(-rdiscr);
            Complex r = Complex.cart(-b / aa, +z / aa);
            roots[0] = r;
            roots[1] = r.conj();
        } else {
            double z = Math.sqrt(rdiscr);
            roots[0] = Complex.cart((-b + z) / aa);
            roots[1] = Complex.cart((-b - z) / aa);
        }
    }

    private Rotator[] Q, C, B;

    private int n, start, stop, zero, it_max, it_count, chase_count, tr;
    private int[] its;
    private double tol = Constants.getEpsilon();
    private Complex[] shifts;
    private FastMatrix D = FastMatrix.square(2);

    private void qr(Polynomial p) {
        //    polynomial has a degree larger than 2
        its = new int[n];
        initialize(p);
        tr = n - 3;
        start = 0;
        stop = n - 2;
        zero = -1;
        it_max = 30 * n;
        it_count = 0;
        chase_count = 0;
        shifts = new Complex[2];
        for (int k = 0; k < it_max; ++k) {
            if (stop < 0) {
                return;
            }
            // check for deflation
            checkDeflation();
            if (!updateRoots()) {
                it_count++;
                bulge();
            }
        }
    }

    /**
     * Creates the bulge
     */
    private void bulge() {
        if (it_count % 25 != 0) {
            diagonalBlock(stop, D);
            eigenValues(D, shifts, 0);
        } else {
            diagonalBlock(stop, D);
            // Random shifts
            Random rnd = new Random();
            double r = rnd.nextGaussian(), i = rnd.nextGaussian();
            shifts[0] = Complex.cart(r, i);
            shifts[1] = Complex.cart(r, -i);
        }
        diagonalBlock(start, D);
        double t00 = D.get(0, 0), t10 = D.get(1, 0), t01 = D.get(0, 1), t11 = D.get(1, 1);
        diagonalBlock(start + 1, D);
        double t21 = D.get(1, 0);

        double c0 = t00 * t00 + t01 * t10 + shifts[0].getRe() * shifts[1].getRe()
                - shifts[0].getIm() * shifts[1].getIm()
                - t00 * (shifts[0].getRe() + shifts[1].getRe());
        double c1 = t10 * (t00 + t11 - shifts[0].getRe() - shifts[1].getRe());
        double c2 = t10 * t21;
        double r = givensRotation(c1, c2, B1);
        givensRotation2(c0, r, B2);
        chase();
    }

    private Rotator B1 = new Rotator(), B2 = new Rotator(), B3 = new Rotator(), tmp = new Rotator(), B1b = new Rotator(), B2b = new Rotator();

    /**
     * Chase the bulge
     */
    private void chase() {
        chase_count++;
        if (start == 0) {
            tmp.copyInverse(B2);
            B3.copyInverse(B1);
            Rotator.turnover(tmp, B3, Q[0]);
            Rotator.fuse(B3, Q[1], 1);
            B3.copy(Q[0]);
            Q[0].copy(tmp);
        } else {
            tmp.c = B2.c;
            tmp.s = -Q[start - 1].c * B2.s;
            B3.copyInverse(B1);
            Rotator.turnover(tmp, B3, Q[start]);
            Rotator.fuse(B3, Q[start + 1], 1);
            B3.copy(Q[start]);
            Q[start].copy(tmp);
        }

        for (int i = start; i <= stop - 2; ++i) {
            if (i < tr - 1) {
                B1b.copy(B1);
                B2b.copy(B2);
                Rotator.turnover(B[i + 1], B[i + 2], B1b);
                Rotator.turnover(B[i], B[i + 1], B2b);
                C[i].copyInverse(B[i]);
                C[i + 1].copyInverse(B[i + 1]);
                C[i + 2].copyInverse(B[i + 2]);

            } else {
                Rotator.turnover(B[i + 1], B[i + 2], B1);
                Rotator.turnover(B[i], B[i + 1], B2);
                Rotator.turnover(C[i + 2], C[i + 1], B1);
                Rotator.turnover(C[i + 1], C[i], B2);
            }

            Rotator.turnover(Q[i + 1], Q[i + 2], B1);
            Rotator.turnover(Q[i], Q[i + 1], B2);

            Rotator.turnover(B3, B1, B2);
            tmp.copy(B2);
            B2.copy(B3);
            B3.copy(B1);
            B1.copy(tmp);
        }

        Rotator.turnover(B[stop], B[stop + 1], B1);
        Rotator.turnover(B[stop - 1], B[stop], B2);
        Rotator.turnover(C[stop + 1], C[stop], B1);
        Rotator.turnover(C[stop], C[stop - 1], B2);
        B1.s *= Q[stop + 1].c;
        Rotator.fuse(Q[stop], B1, 0);
        Rotator.turnover(Q[stop - 1], Q[stop], B2);
        Rotator.fuse(B3, B2, 0);

        Rotator.turnover(B[stop], B[stop + 1], B3);
        Rotator.turnover(C[stop + 1], C[stop], B3);
        B3.s *= Q[stop + 1].c;
        Rotator.fuse(Q[stop], B3, 0);
        tr -= 2;
    }

    /**
     * Computes the diagonal block and stores the result in D
     *
     * @param k A[k,k+1; k, k+1]
     * @param D
     */
    private void diagonalBlock(int k, FastMatrix D) {
        D.set(0);
        if (k == 0) {
            // unoptimized
            FastMatrix R = FastMatrix.square(2);
            R.set(0, 0, -B[0].s / C[0].s);
            double r11 = -B[1].s / C[1].s;
            R.set(0, 1, -(B[0].c * B[1].c - r11 * C[0].c * C[1].c) / C[0].s);
            R.set(1, 1, r11 * Q[1].c);

            FastMatrix A = FastMatrix.square(2);
            A.set(0, 0, Q[0].c);
            A.set(1, 0, Q[0].s);
            A.set(0, 1, -Q[0].s);
            A.set(1, 1, Q[0].c);
            GeneralMatrix.aAB_p_bC(1, A, R, 0, D);

        } else {
            FastMatrix R = FastMatrix.make(3, 2);
            double r10 = -B[k].s / C[k].s;
            R.set(1, 0, r10);
            R.set(0, 0, -(B[k - 1].c * B[k].c - r10 * C[k - 1].c * C[k].c) / C[k - 1].s);
            double r21 = -B[k + 1].s / C[k + 1].s;
            R.set(0, 1, (B[k - 1].c * B[k].s * B[k + 1].c - C[k - 1].c * (C[k].c * B[k].c * B[k + 1].c - C[k + 1].c * r21) / C[k].s) / C[k - 1].s);
            R.set(1, 1, -(B[k].c * B[k + 1].c - r21 * C[k].c * C[k + 1].c) / C[k].s);
            R.set(2, 1, r21 * Q[k + 1].c);

            FastMatrix A = FastMatrix.square(2);
            A.set(0, 0, Q[k].c);
            A.set(1, 0, Q[k].s);
            A.set(0, 1, -Q[k].s);
            A.set(1, 1, Q[k].c);

            FastMatrix R12 = R.extract(1, 2, 0, 2);
            R12.copy(GeneralMatrix.AB(A, R12));
            A.set(0, 0, Q[k - 1].c);
            A.set(1, 0, Q[k - 1].s);
            A.set(0, 1, -Q[k - 1].s);
            A.set(1, 1, Q[k - 1].c);

            FastMatrix R01 = R.extract(0, 2, 0, 2);
            R01.copy(GeneralMatrix.AB(A, R01));
            D.copy(R12);
        }
    }

    /**
     * This subroutine computes a factorization of the column companion matrix
     * for P(x), P(x) = x^N + a_N-1 x^N-1 + ... + a_1 x + a_0.
     *
     * @param P Polynomial
     */
    private void initialize(Polynomial p) {
        // n degree of the polynomial
        Q = new Rotator[n]; // It would be possible to omit the last one (1,0)
        C = new Rotator[n];
        B = new Rotator[n];

        int n1 = n - 1;
        // Q(i): c=0, s=1 
        for (int i = 0; i < n1; ++i) {
            Q[i] = new Rotator(0, 1);
        }
        Q[n1] = new Rotator(1, 0);
        // build C, B (from n-1 to 0)
        double u = n % 2 == 0 ? -1 : 1, v = -u; //(-1)^(n-1), (-1)^n
        // C[n]
        Rotator c = new Rotator();
        double pn = p.get(n);
        double r = givensRotation(v * p.get(0) / pn, u, c);
        C[n1] = c;
        B[n1] = new Rotator(v * c.s, v * c.c);
        for (int i = n - 1; i > 0; --i) {
            c = new Rotator();
            r = givensRotation(-p.get(i) / pn, r, c);
            C[i - 1] = c;
            B[i - 1] = new Rotator(c.c, -c.s);
        }
    }

    /**
     * Specialised implementation of Givens rotation.
     *
     * @param a
     * @param b
     * @param gr Contains cos, sin
     * @return r=norm2(a,b)
     */
    private static double givensRotation(double a, double b, Rotator gr) {
        if (b == 0) {
            if (a < 0) {
                gr.c = -1;
                gr.s = 0;
                return -a;
            } else {
                gr.c = 1;
                gr.s = 0;
                return a;
            }
        }
        double absa = Math.abs(a), absb = Math.abs(b);
        double s, c, r;
        if (absa >= absb) {
            s = b / a;
            r = Math.sqrt(1 + s * s);
            if (a < 0) {
                c = -1 / r;
                s *= c;
                r *= -a;
            } else {
                c = 1 / r;
                s *= c;
                r *= a;
            }
        } else {
            c = a / b;
            r = Math.sqrt(1 + c * c);
            if (b < 0) {
                s = -1 / r;
                c *= s;
                r *= -b;
            } else {
                s = 1 / r;
                c *= s;
                r *= b;
            }
        }
        gr.c = c;
        gr.s = s;
        return r;
    }

    /**
     * Specialised implementation of Givens rotation. Same as givensRotation,
     * without the computation of r
     *
     * @param a
     * @param b
     * @param gr Contains cos, sin
     */
    private static void givensRotation2(double a, double b, Rotator gr) {
        if (b == 0) {
            if (a < 0) {
                gr.c = -1;
                gr.s = 0;
            } else {
                gr.c = 1;
                gr.s = 0;
                return;
            }
        } else {
            double absa = Math.abs(a), absb = Math.abs(b);
            double s, c, r;
            if (absa >= absb) {
                s = b / a;
                r = Math.sqrt(1 + s * s);
                if (a < 0) {
                    c = -1 / r;
                    s *= c;
                } else {
                    c = 1 / r;
                    s *= c;
                }
            } else {
                c = a / b;
                r = Math.sqrt(1 + c * c);
                if (b < 0) {
                    s = -1 / r;
                    c *= s;
                } else {
                    s = 1 / r;
                    c *= s;
                }
            }
            gr.c = c;
            gr.s = s;
        }
    }

    /**
     * Check for deflation
     */
    private void checkDeflation() {
        for (int i = stop; i >= 0; --i) {
            if (Q[i].simplify(tol)) {
                zero = i;
                start = i + 1;
                its[zero] = it_count;
                it_count = 0;
                return;
            }
        }
    }

    private boolean updateRoots() {
        if (stop == zero) {
            diagonalBlock(stop, D);
            if (stop == 0) {
                // top
                roots[stop] = Complex.cart(D.get(0, 0));
                roots[stop + 1] = Complex.cart(D.get(1, 1));
                stop = -1;
            } else {
                roots[stop + 1] = Complex.cart(D.get(1, 1));
                stop--;
                zero = -1;
                start = 0;
            }
            return true;
        } else if (stop - 1 == zero) {
            diagonalBlock(stop, D);
            eigenValues(D, roots, stop);
            if (stop == 1) {
                diagonalBlock(0, D);
                roots[0] = Complex.cart(D.get(0, 0));
            }
            stop -= 2;
            zero = -1;
            start = 0;
            return true;
        } else {
            return false;
        }

    }

    private void eigenValues(FastMatrix M, Complex[] ev, int pos) {
        double m00 = M.get(0, 0), m01 = M.get(0, 1), m10 = M.get(1, 0), m11 = M.get(1, 1);
        double trace = m00 + m11;
        double detm = m00 * m11 - m10 * m01;
        double disc = trace * trace - 4 * detm;
        if (disc < 0) {
            Complex v = Complex.cart(trace / 2, Math.sqrt(-disc) / 2);
            ev[pos] = v;
            ev[pos + 1] = v.conj();
        } else {
            double sdisc = Math.sqrt(disc);
            double qp = Math.abs(trace + sdisc), qm = Math.abs(trace - sdisc);
            if (qp > qm) {
                double re = (trace + sdisc) / 2;
                ev[pos] = Complex.cart(re);
                ev[pos + 1] = Complex.cart(detm / re);
            } else if (qm > 0) {
                double re = (trace - sdisc) / 2;
                ev[pos] = Complex.cart(re);
                ev[pos + 1] = Complex.cart(detm / re);
            } else {
                ev[pos] = Complex.ZERO;
                ev[pos + 1] = Complex.ZERO;
            }
        }
    }
}
