/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.functions;

import demetra.data.DoubleSeq;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.math.matrices.FastMatrix;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author PALATEJ
 */
public class GenericCubicSpline implements DoubleUnaryOperator {

    @lombok.Value
    public static class BoundaryConstraints {

        private double f, df, d2f;

        /**
         *
         * @param level True if we take into account the level condition
         * (usually false)
         * @return
         */
        public int count(boolean level) {
            int n = 0;
            if (level && Double.isFinite(f)) {
                ++n;
            }
            if (Double.isFinite(df)) {
                ++n;
            }
            if (Double.isFinite(d2f)) {
                ++n;
            }
            return n;
        }
    }

    public static final BoundaryConstraints NATURAL = new BoundaryConstraints(Double.NaN, Double.NaN, 0),
            STABLE = new BoundaryConstraints(Double.NaN, 0, Double.NaN),
            FREE =new BoundaryConstraints(Double.NaN, Double.NaN, Double.NaN);

    static void fillConstraints(double[] xi, double[] fxi, FastMatrix C, double[] P,
            BoundaryConstraints left, BoundaryConstraints right) {
        int r = 0, n = xi.length - 1;
        DataBlockIterator rows = C.rowsIterator();
        // left conditions 
        double df = left.getDf();
        if (Double.isFinite(df)) {
            rows.next().set(1, 1);
            P[r++] = df;
        }
        double d2f = left.getD2f();
        if (Double.isFinite(d2f)) {
            rows.next().set(2, 1);
            P[r++] = d2f;
        }
        // level conditions on Pi
        // P0(0)=fxi(0), P0(xi)=fxi(1)
        for (int i = 0; i < n; ++i) {
            int k = 4 * i;
            rows.next().set(k, 1);
            P[r++] = fxi[i];
            DataBlock row = rows.next();
            row.set(k, 1);
            double dx = xi[i + 1] - xi[i];
            row.set(k + 1, dx);
            row.set(k + 2, dx * dx);
            row.set(k + 3, dx * dx * dx);
            P[r++] = fxi[i + 1];
        }
        // limit conditions for node i
        for (int i = 0; i < n - 1; ++i) {
            int k = 4 * i;
            // P'
            DataBlock row = rows.next();
            double dx = xi[i + 1] - xi[i];
            row.set(k + 1, 1);
            row.set(k + 2, 2 * dx);
            row.set(k + 3, 3 * dx * dx);
            row.set(k + 5, -1);
            // P''
            row = rows.next();
            row.set(k + 2, 1);
            row.set(k + 3, 3 * dx);
            row.set(k + 6, -1);
        }
        r += 2 * n - 2;
        // right limit condition Pn'' = 0: 2c + 6d(dx) = 0 
        df = right.getDf();
        if (Double.isFinite(df)) {
            double dx = xi[n] - xi[n - 1], dx2 = dx * dx;
            DataBlock row = rows.next();
            int k = 4 * (n - 1);
            row.set(k + 1, 1);
            row.set(k + 2, 2 * dx);
            row.set(k + 3, 3 * dx2);
            P[r++] = df;
        }
        d2f = right.getD2f();
        if (Double.isFinite(d2f)) {
            double dx = xi[n] - xi[n - 1];
            DataBlock row = rows.next();
            int k = 4 * (n - 1);
            row.set(k + 2, 2);
            row.set(k + 3, 6 * dx);
            P[r] = d2f;
        }
    }

    private static void fillAggregationConstraints(double[] xi, double[] fi, double fn, FastMatrix C, double[] P,
            BoundaryConstraints left, BoundaryConstraints right) {
        int r = 0, n = xi.length - 1;
        DataBlockIterator rows = C.rowsIterator();
        // left conditions 
        double f=left.getF();
        if (Double.isFinite(f)) {
            rows.next().set(0, 1);
            P[r++] = f;
        }
        double df = left.getDf();
        if (Double.isFinite(df)) {
            rows.next().set(1, 1);
            P[r++] = df;
        }
        double d2f = left.getD2f();
        if (Double.isFinite(d2f)) {
            rows.next().set(2, 1);
            P[r++] = d2f;
        }
        // aggregation conditions on Pi (n)
        // Integral(Pi)=fi
        for (int i = 0; i < n; ++i) {
            int k = 4 * i;
            DataBlock row = rows.next();
            double dx = xi[i + 1] - xi[i];
            row.set(k, dx);
            double dxn = dx * dx;
            row.set(k + 1, dxn / 2);
            dxn *= dx;
            row.set(k + 2, dxn / 3);
            dxn *= dx;
            row.set(k + 3, dxn / 4);
            P[r++] = fi[i];
        }

        // Continuity conditions (3*n-3)
        // Pi(xi)=Pi+1(0)
        // Pi'(xi)=Pi+1'(0)
        // Pi''(xi)=Pi+1''(0)
        for (int i = 0; i < n - 1; ++i) {
            int k = 4 * i;
            // P
            double dx = xi[i + 1] - xi[i], dx2 = dx * dx, dx3 = dx2 * dx;
            DataBlock row = rows.next();
            row.set(k, 1);
            row.set(k + 1, dx);
            row.set(k + 2, dx2);
            row.set(k + 3, dx3);
            row.set(k + 4, -1);
            // P'
            row = rows.next();
            row.set(k + 1, 1);
            row.set(k + 2, 2 * dx);
            row.set(k + 3, 3 * dx2);
            row.set(k + 5, -1);
            // P''
            row = rows.next();
            row.set(k + 2, 1);
            row.set(k + 3, 3 * dx);
            row.set(k + 6, -1);
        }
        r+=3*n-3;

        f = right.getF();
        if (Double.isFinite(f)) {
            double dx = xi[n] - xi[n - 1], dx2 = dx * dx, dx3=dx2*dx;
            DataBlock row = rows.next();
            int k = 4 * (n - 1);
            row.set(k, 1);
            row.set(k + 1, dx);
            row.set(k + 2, dx2);
            row.set(k + 3, dx3);
            P[r++] = f;
        }
        df = right.getDf();
        if (Double.isFinite(df)) {
            double dx = xi[n] - xi[n - 1], dx2 = dx * dx;
            DataBlock row = rows.next();
            int k = 4 * (n - 1);
            row.set(k + 1, 1);
            row.set(k + 2, 2 * dx);
            row.set(k + 3, 3 * dx2);
            P[r++] = df;
        }
        d2f = right.getD2f();
        if (Double.isFinite(d2f)) {
            double dx = xi[n] - xi[n - 1];
            DataBlock row = rows.next();
            int k = 4 * (n - 1);
            row.set(k + 2, 2);
            row.set(k + 3, 6 * dx);
            P[r] = d2f;
        }
    }

    final int n;
    final double[] xi;
    final double[] P;

    public static GenericCubicSpline of(@NonNull double[] xi, @NonNull double[] fxi, BoundaryConstraints left, BoundaryConstraints right) {
        if (xi.length != fxi.length) {
            throw new IllegalArgumentException("Invalid xi/fxi");
        }
        if (left == null) {
            left = NATURAL;
        }
        if (right == null) {
            right = NATURAL;
        }
        // check the number of constraints
        if (left.count(false) + right.count(false) != 2) {
            throw new IllegalArgumentException("Invalid number of constraints");
        }
        int n = xi.length - 1;
        double[] P = new double[4 * n];
        FastMatrix C = FastMatrix.square(P.length);
        fillConstraints(xi, fxi, C, P, left, right);
        LinearSystemSolver.robustSolver().solve(C, DataBlock.of(P));
        return new GenericCubicSpline(P, xi);
    }

    public static GenericCubicSpline ofAggregation(double[] xi, double[] fxi, BoundaryConstraints left, BoundaryConstraints right) {
        if (xi.length-1 != fxi.length) {
            throw new IllegalArgumentException("Invalid xi/fxi");
        }
        if (left == null) {
            left = NATURAL;
        }
        if (right == null) {
            right = NATURAL;
        }
        // check the number of constraints
        if (left.count(true) + right.count(true) != 3) {
            throw new IllegalArgumentException("Invalid number of constraints");
        }
        int n = xi.length - 1;
        double[] P = new double[4 * n];
        FastMatrix C = FastMatrix.square(P.length);
        double fn = fxi[n - 1] / (xi[n] - xi[n - 1]);
        fillAggregationConstraints(xi, fxi, fn, C, P, left, right);
        LinearSystemSolver.robustSolver().solve(C, DataBlock.of(P));
        return new GenericCubicSpline(P, xi);

    }

    /**
     * Return the i-th polynomial
     *
     * @param i Index of the polynomial (starting in 0)
     * @return
     */
    public DoubleSeq polynomial(int i) {
        return DoubleSeq.of(P, i * 4, 4);
    }

    /**
     * Return the i-th node
     *
     * @param i position of the node (in [0, getPolynomialsCount()])
     * @return
     */
    public double x(int i) {
        return xi[i];
    }

    public int getPolynomialsCount() {
        return n;
    }

    private GenericCubicSpline(final double[] P, final double[] xi) {
        this.xi = xi;
        this.P = P;
        this.n = xi.length - 1;
    }

    private int find(double x) {
        if (x <= xi[0]) {
            return -1;
        } else if (x >= xi[n]) {
            return n;
        }
        int pos = Arrays.binarySearch(xi, x);
        if (pos >= 0) {
            return pos;
        } else {
            return -pos - 2;
        }
    }

    private double compute(double x, int p) {
        double dx = x - xi[p], dx2 = dx * dx, dx3 = dx2 * dx;
        int j = p * 4;
        return P[j] + P[j + 1] * dx + P[j + 2] * dx2 + P[j + 3] * dx3;
    }

    private double compute0(double x) {
        double df = P[1];
        return P[0] + (x - xi[0]) * df;
    }

    private double computen(double x) {
        double dx = xi[n] - xi[n - 1], dx2 = dx * dx;
        int j = (n - 1) * 4;
        double df = P[j + 1] + 2 * P[j + 2] * dx + 3 * P[j + 3] * dx2;
        double f = P[j] + P[j + 1] * dx + P[j + 2] * dx2 + P[j + 3] * dx2 * dx;
        return f + (x - xi[n]) * df;
    }

    @Override
    public double applyAsDouble(double value) {
        int pos = find(value);
        if (pos < 0) {
            return compute0(value);
        } else if (pos >= n) {
            return computen(value);
        } else {
            return compute(value, pos);
        }
    }

}
