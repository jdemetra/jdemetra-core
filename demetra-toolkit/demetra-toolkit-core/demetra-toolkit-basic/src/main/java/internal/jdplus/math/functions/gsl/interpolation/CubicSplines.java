/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.math.functions.gsl.interpolation;

import demetra.data.DoubleSeq;
import demetra.math.MathException;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class CubicSplines {

    public static abstract class Spline implements DoubleUnaryOperator {

        final double[] b;
        final double[] c;
        final double[] d;
        final double[] xa, ya;

        Spline(DoubleSeq x, DoubleSeq y) {
            this.xa = x.toArray();
            this.ya = y.toArray();
            int size = xa.length - 1;
            b = new double[size];
            c = new double[size + 1];
            d = new double[size];
        }

        protected double interpolate(double x, int index) {
            double delx = x - xa[index];
            return ya[index] + delx * (b[index] + delx * (c[index] + delx * d[index]));
        }

        int size() {
            return xa.length;
        }
    }

    private static class NaturalSpline extends Spline {

        @Override
        public double applyAsDouble(double x) {
            if (x < xa[0]) {
                return extrapolate0(x);
            } else if (x > xa[xa.length - 1]) {
                return extrapolate1(x);
            } else {
                int pos = Arrays.binarySearch(xa, x);
                if (pos >= 0) {
                    return ya[pos];
                } else {
                    return interpolate(x, -pos - 2);
                }
            }
        }

        private double extrapolate0(double x) {
            double df = b[0];
            return ya[0] + (x - xa[0]) * df;
        }

        private double extrapolate1(double x) {
            int n = xa.length - 1;
            double dx = xa[n] - xa[n - 1], dx2 = dx * dx;
            double df = b[n - 1] + 2 * c[n - 1] * dx + 3 * d[n - 1] * dx2;
            return ya[n] + (x - xa[n]) * df;
        }

        private NaturalSpline(DoubleSeq X, DoubleSeq Y) {
            super(X, Y);
        }
    }

    public static Spline natural(DoubleSeq X, DoubleSeq Y) {
        if (X.length() != Y.length()) {
            throw new IllegalArgumentException();
        }
        NaturalSpline spline = new NaturalSpline(X, Y);

        int psize = spline.size() - 1;
        int sys_size = psize - 1;
        /* linear system is sys_size x sys_size */
        double[] xa = spline.xa, ya = spline.ya;
        double[] g = new double[psize];
        double[] diag = new double[psize];
        double[] offdiag = new double[psize];

        for (int i = 0; i < sys_size; i++) {
            double h_i = xa[i + 1] - xa[i];
            double h_ip1 = xa[i + 2] - xa[i + 1];
            double ydiff_i = ya[i + 1] - ya[i];
            double ydiff_ip1 = ya[i + 2] - ya[i + 1];
            double g_i = (h_i != 0.0) ? 1.0 / h_i : 0.0;
            double g_ip1 = (h_ip1 != 0.0) ? 1.0 / h_ip1 : 0.0;
            offdiag[i] = h_ip1;
            diag[i] = 2.0 * (h_ip1 + h_i);
            g[i] = 3.0 * (ydiff_ip1 * g_ip1 - ydiff_i * g_i);
        }

        if (sys_size == 1) {
            spline.c[1] = g[0] / diag[0];
        } else {
            DoubleSeq g_vec = DoubleSeq.of(g, 0, sys_size);
            DoubleSeq diag_vec = DoubleSeq.of(diag, 0, sys_size);
            DoubleSeq offdiag_vec = DoubleSeq.of(offdiag, 0, sys_size - 1);
            DataBlock solution_vec = DataBlock.of(spline.c, 1, sys_size + 1);
            solveTriDiag(diag_vec, offdiag_vec, g_vec, solution_vec);

        }

        for (int i = 0; i < psize; i++) {
            double h_i = xa[i + 1] - xa[i];
            spline.b[i] = (ya[i + 1] - ya[i]) / h_i - h_i / 3.0 * (2.0 * spline.c[i] + spline.c[i + 1]);
            spline.d[i] = (spline.c[i + 1] - spline.c[i]) / (3.0 * h_i);
        }
        return spline;
    }

    private static class PeriodicSpline extends Spline {

        private double translate(double x) {
            int n = size() - 1;
            if (x < xa[0] || x > xa[n]) {
                double xc = x - xa[0];
                double dx = xa[n] - xa[0];
                double m = Math.floor(xc / dx);
                xc -= m * dx;
                return xc + xa[0];
            } else {
                return x;
            }
        }

        @Override
        public double applyAsDouble(double x) {
            double xc = translate(x);
            int pos = Arrays.binarySearch(xa, xc);
            if (pos >= 0) {
                return ya[pos];
            } else {
                return interpolate(xc, -pos - 2);
            }
        }

        private PeriodicSpline(DoubleSeq X, DoubleSeq Y) {
            super(X, Y);
        }

    }

    public Spline periodic(DoubleSeq X, DoubleSeq Y) {
        int n = Y.length();
        if (n != X.length() || Y.get(0) != Y.get(n - 1)) {
            throw new IllegalArgumentException();
        }
        Spline spline = new PeriodicSpline(X, Y);

        int psize = spline.size() - 1;
        /* Engeln-Mullges + Uhlig "n" */
        int sys_size = psize;
        /* linear system is sys_size x sys_size */

        double[] xa = spline.xa, ya = spline.ya;

        if (sys_size == 2) {
            /* solve 2x2 system */

            double h0 = xa[1] - xa[0];
            double h1 = xa[2] - xa[1];

            double A = 2.0 * (h0 + h1);
            double B = h0 + h1;

            double g0 = 3.0 * ((ya[2] - ya[1]) / h1 - (ya[1] - ya[0]) / h0);
            double g1 = 3.0 * ((ya[1] - ya[2]) / h0 - (ya[2] - ya[1]) / h1);

            double det = 3.0 * (h0 + h1) * (h0 + h1);
            spline.c[1] = (A * g0 - B * g1) / det;
            spline.c[2] = (-B * g0 + A * g1) / det;
            spline.c[0] = spline.c[2];

        } else {
            double[] g = new double[psize];
            double[] diag = new double[psize];
            double[] offdiag = new double[psize];

            for (int i = 0; i < sys_size - 1; i++) {
                double h_i = xa[i + 1] - xa[i];
                double h_ip1 = xa[i + 2] - xa[i + 1];
                double ydiff_i = ya[i + 1] - ya[i];
                double ydiff_ip1 = ya[i + 2] - ya[i + 1];
                double g_i = (h_i != 0.0) ? 1.0 / h_i : 0.0;
                double g_ip1 = (h_ip1 != 0.0) ? 1.0 / h_ip1 : 0.0;
                offdiag[i] = h_ip1;
                diag[i] = 2.0 * (h_ip1 + h_i);
                g[i] = 3.0 * (ydiff_ip1 * g_ip1 - ydiff_i * g_i);
            }
            int ilast = sys_size - 1;
            double h_i = xa[ilast + 1] - xa[ilast];
            double h_ip1 = xa[1] - xa[0];
            double ydiff_i = ya[ilast + 1] - ya[ilast];
            double ydiff_ip1 = ya[1] - ya[0];
            double g_i = (h_i != 0.0) ? 1.0 / h_i : 0.0;
            double g_ip1 = (h_ip1 != 0.0) ? 1.0 / h_ip1 : 0.0;
            offdiag[ilast] = h_ip1;
            diag[ilast] = 2.0 * (h_ip1 + h_i);
            g[ilast] = 3.0 * (ydiff_ip1 * g_ip1 - ydiff_i * g_i);
            DoubleSeq g_vec = DoubleSeq.of(g, 0, sys_size);
            DoubleSeq diag_vec = DoubleSeq.of(diag, 0, sys_size);
            DoubleSeq offdiag_vec = DoubleSeq.of(offdiag, 0, sys_size);
            DataBlock solution_vec = DataBlock.of(spline.c, 1, sys_size + 1);

            solveCyclicTriDiag(diag_vec, offdiag_vec, g_vec, solution_vec);
            spline.c[0] = spline.c[psize];
        }
        for (int i = 0; i < psize; i++) {
            double h_i = xa[i + 1] - xa[i];

            spline.b[i] = (ya[i + 1] - ya[i]) / h_i - h_i / 3.0 * (2.0 * spline.c[i] + spline.c[i + 1]);
            spline.d[i] = (spline.c[i + 1] - spline.c[i]) / (3.0 * h_i);
        }
        return spline;
    }

    /* for description natural method see [Engeln-Mullges + Uhlig, p. 92]
 *
 *     diag[0]  offdiag[0]             0   .....
 *  offdiag[0]     diag[1]    offdiag[1]   .....
 *           0  offdiag[1]       diag[2]
 *           0           0    offdiag[2]   .....
     */
    public void solveTriDiag(DoubleSeq diag, DoubleSeq offdiag, DoubleSeq b, DataBlock x) {
        int N = diag.length();
        double[] gamma = new double[N];
        double[] alpha = new double[N];
        double[] c = new double[N];
        double[] z = new double[N];

        /* Cholesky decomposition
         A = L.D.L^t
         lower_diag(L) = gamma
         diag(D) = alpha
         */
        alpha[0] = diag.get(0);
        gamma[0] = offdiag.get(0) / alpha[0];

        if (alpha[0] == 0) {
            throw new MathException(MathException.DIVBYZERO);
        }

        for (int i = 1; i < N - 1; i++) {
            alpha[i] = diag.get(i) - offdiag.get(i - 1) * gamma[i - 1];
            gamma[i] = offdiag.get(i) / alpha[i];
            if (alpha[i] == 0) {
                throw new MathException(MathException.DIVBYZERO);
            }
        }
        if (N > 1) {
            alpha[N - 1] = diag.get(N - 1) - offdiag.get(N - 2) * gamma[N - 2];
        }
        /* update RHS */
        z[0] = b.get(0);
        for (int i = 1; i < N; i++) {
            z[i] = b.get(i) - gamma[i - 1] * z[i - 1];
        }
        for (int i = 0; i < N; i++) {
            c[i] = z[i] / alpha[i];
        }

        /* backsubstitution */
        x.set(N - 1, c[N - 1]);
        if (N >= 2) {
            for (int i = N - 2, j = 0; j <= N - 2; j++, i--) {
                x.set(i, c[i] - gamma[i] * x.get(i + 1));
            }
        }
    }

    public void solveCyclicTriDiag(DoubleSeq diag, DoubleSeq offdiag, DoubleSeq b, DataBlock x) {
        int N = diag.length();
        double[] delta = new double[N];
        double[] gamma = new double[N];
        double[] alpha = new double[N];
        double[] c = new double[N];
        double[] z = new double[N];
        double sum = 0.0;

        /* factor */
        if (N == 1) {
            x.set(0, b.get(0) / diag.get(0));
            return;
        }

        alpha[0] = diag.get(0);
        if (alpha[0] == 0) {
            throw new MathException(MathException.DIVBYZERO);
        }
        gamma[0] = offdiag.get(0) / alpha[0];
        delta[0] = offdiag.get(N - 1) / alpha[0];

        for (int i = 1; i < N - 2; i++) {
            alpha[i] = diag.get(i) - offdiag.get(i - 1) * gamma[i - 1];
            gamma[i] = offdiag.get(i) / alpha[i];
            delta[i] = -delta[i - 1] * offdiag.get(i - 1) / alpha[i];
            if (alpha[i] == 0) {
                throw new MathException(MathException.DIVBYZERO);
            }
        }

        for (int i = 0; i < N - 2; i++) {
            sum += alpha[i] * delta[i] * delta[i];
        }

        alpha[N - 2] = diag.get(N - 2) - offdiag.get(N - 3) * gamma[N - 3];
        gamma[N - 2] = (offdiag.get(N - 2) - offdiag.get(N - 3) * delta[N - 3]) / alpha[N - 2];
        alpha[N - 1] = diag.get(N - 1) - sum - alpha[(N - 2)] * gamma[N - 2] * gamma[N - 2];

        /* update */
        z[0] = b.get(0);
        for (int i = 1; i < N - 1; i++) {
            z[i] = b.get(i) - z[i - 1] * gamma[i - 1];
        }
        sum = 0.0;
        for (int i = 0; i < N - 2; i++) {
            sum += delta[i] * z[i];
        }
        z[N - 1] = b.get(N - 1) - sum - gamma[N - 2] * z[N - 2];
        for (int i = 0; i < N; i++) {
            c[i] = z[i] / alpha[i];
        }

        /* backsubstitution */
        x.set(N - 1, c[N - 1]);
        x.set(N - 2, c[N - 2] - gamma[N - 2] * x.get(N - 1));
        if (N >= 3) {
            for (int i = N - 3, j = 0; j <= N - 3; j++, i--) {
                x.set(i, c[i] - gamma[i] * x.get(i + 1) - delta[i] * x.get(N - 1));
            }
        }
    }

    /* Perform a binary search natural an array natural values.
 * 
 * The parameters index_lo and index_hi provide an initial bracket,
 * and it is assumed that index_lo < index_hi. The resulting index
 * is guaranteed to be strictly less than index_hi and greater than
 * or equal to index_lo, so that the implicit bracket [index, index+1]
 * always corresponds to a region within the implicit value range natural
 * the value array.
 *
 * Note that this means the relationship natural 'x' to x_array[index]
 * and x_array[index+1] depends on the result region, i.e. the
 * behaviour at the boundaries may not correspond to what you
 * expect. We have the following complete specification natural the
 * behaviour.
 * Suppose the input is x_array[] = { x0, x1, ..., xN }
 *    if ( x == x0 )           then  index == 0
 *    if ( x > x0 && x <= x1 ) then  index == 0, and sim. for other interior pts
 *    if ( x == xN )           then  index == N-1
 *    if ( x > xN )            then  index == N-1
 *    if ( x < x0 )            then  index == 0 
     */
//    int bsearch(double[] x_array, double x, int index_lo, int index_hi) {
//        int ilo = index_lo;
//        int ihi = index_hi;
//        while (ihi > ilo + 1) {
//            int i = (ihi + ilo) / 2;
//            if (x_array[i] > x) {
//                ihi = i;
//            } else {
//                ilo = i;
//            }
//        }
//        return ilo;
//    }
//
}
