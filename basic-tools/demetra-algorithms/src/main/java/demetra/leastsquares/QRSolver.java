/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.data.Cell;
import demetra.data.CellReader;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.IQRDecomposition;
import demetra.data.Doubles;
import demetra.data.NeumaierAccumulator;
import demetra.design.IBuilder;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRSolver implements LeastSquaresSolver {

    public static class Builder implements IBuilder<QRSolver> {

        private final IQRDecomposition qr;
        private boolean normalize;
        private int niter = 1;
        private boolean simple;

        private Builder(IQRDecomposition qr) {
            this.qr = qr;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public Builder iterative(int niter) {
            this.niter = niter;
            return this;
        }

        public Builder iterative() {
            this.niter = 2;
            return this;
        }

        public Builder simpleIteration(boolean s) {
            this.simple = s;
            return this;
        }

        @Override
        public QRSolver build() {
            return new QRSolver(qr, normalize, niter, simple);
        }
    }

    public static Builder builder(IQRDecomposition qr) {
        return new Builder(qr);
    }
    private double ssqerr;
    private double[] b, res;
    private Matrix R, V;
    private int[] used;
    private int n, m;
    private double[] c;
    private final IQRDecomposition qr;
    private final boolean scaling, simple;
    private final int niter;

    private QRSolver(IQRDecomposition qr, boolean scaling, int niter, boolean simple) {
        this.qr = qr;
        this.scaling = scaling;
        this.niter = niter;
        this.simple = simple;
    }

    @Override
    public boolean compute(Doubles y, Matrix x) {
        try {
            clear();
            if (scaling) {
                computeWithScaling(y, x);
            } else {
                computeWithoutScaling(y, x);
            }
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    private void clear() {
        ssqerr = 0;
        R = null;
        c = null;
        b = null;
        res = null;
        V = null;
    }

    private void computeWithScaling(Doubles y, Matrix x) {
        n = y.length();
        m = x.getColumnsCount();
        Matrix xc = x.deepClone();
        c = new double[m];
        int pos = 0;
        DataBlockIterator columns = xc.columnsIterator();
        while (columns.hasNext()) {
            DataBlock col = columns.next();
            double norm = col.norm2();
            col.div(norm);
            c[pos++] = norm;
        }
        qr.decompose(xc);
        int r = qr.rank();
        res = new double[n - qr.rank()];
        b = new double[r];
        R = qr.r(true);
        used = qr.used();
        if (niter > 1) {
            if (simple) {
                iterativeEstimation2(DataBlock.copyOf(y), xc);
            } else {
                iterativeEstimation(DataBlock.copyOf(y), xc);
            }
        } else {
            DataBlock B = DataBlock.of(b), E = DataBlock.of(res);
            qr.leastSquares(y, B, E);
            ssqerr = E.ssq();
        }
        correctforScaling();
    }

    private void correctforScaling() {
        for (int i = 0; i < b.length; ++i) {
            b[i] /= c[i];
        }
    }

    private void computeWithoutScaling(Doubles y, Matrix x) {

        // X'X, X'y
        n = y.length();
        m = x.getColumnsCount();
        qr.decompose(x);
        int r = qr.rank();
        R = qr.r(true);
        b = new double[r];
        res = new double[n - qr.rank()];
        used = qr.used();
        if (niter > 1) {
            if (simple) {
                iterativeEstimation2(DataBlock.copyOf(y), x);
            } else {
                iterativeEstimation(DataBlock.copyOf(y), x);
            }
        } else {
            res = new double[n - qr.rank()];
            DataBlock B = DataBlock.of(b), E = DataBlock.of(res);
            qr.leastSquares(y, B, E);
            ssqerr = E.ssq();
        }
    }

    @Override
    public Matrix covariance() {
        if (V == null) {
            double sig = ssqerr / (n - m);
            Matrix v = null;
            if (!R.isEmpty()) {
                Matrix U = UpperTriangularMatrix.inverse(R);
                v = SymmetricMatrix.UUt(U);
                v.apply(x -> x * sig);
            }
            if (m == used.length) {
                V = v;
            } else {
                V = Matrix.square(m);
                if (v != null) {
                    // expand the matrix
                    for (int i = 0; i < used.length; ++i) {
                        V.set(used[i], used[i], v.get(i, i));
                        for (int j = 0; j < i; ++j) {
                            double d = v.get(i, j);
                            V.set(used[i], used[j], d);
                            V.set(used[j], used[i], d);
                        }
                    }
                }
            }
        }
        return V;
    }

    @Override
    public Doubles coefficients() {
        if (used.length == m) {
            return Doubles.of(b);
        } else {
            // expand the coefficients
            double[] c = new double[m];
            for (int i = 0; i < used.length; ++i) {
                c[used[i]] = b[i];
            }
            return Doubles.of(c);
        }
    }

    @Override
    public Doubles residuals() {
        return Doubles.of(res);
    }

    @Override
    public double ssqerr() {
        return ssqerr;
    }

    /**
     * See Golub - Van Loan, Matrix computation. Least Squares, iterative
     * improvement (5.6.4)
     *
     * @param y
     * @param X
     * @param B
     */
    private void iterativeEstimation(Doubles Y, Matrix X) {
        DataBlock F = DataBlock.make(n);
        DataBlock G = DataBlock.make(m);

        DataBlock B = DataBlock.of(b);
        DataBlock E = DataBlock.make(n);
        // step 1
        int iter = 0;
        do {
            Cell f = F.cells(), g = G.cells(), e = E.cells();
            CellReader y = Y.reader();
            NeumaierAccumulator acc = new NeumaierAccumulator();
            DataBlockIterator rows = X.rowsIterator();
            // f = y - r - X*b
            while (rows.hasNext()) {
                acc.set(-y.next());
                acc.add(e.next());
                rows.next().robustDot(B, acc);
                f.setAndNext(-acc.sum());
            }
            // g = - X'r
            DataBlockIterator cols = X.columnsIterator();
            while (cols.hasNext()) {
                acc.reset();
                cols.next().robustDot(E, acc);
                g.setAndNext(-acc.sum());
            }
            // compute in place QtF
            qr.applyQt(F);

            // compute in place R'h=g or h'R=g'
            UpperTriangularMatrix.lsolve(R, G);

            DataBlock F1 = F.extract(0, m);
            // f1 - h
            F1.sub(G);
            // Rz = f1-h
            UpperTriangularMatrix.rsolve(R, F1);
            B.add(F1);
            F1.copy(G);
            qr.applyQ(F);
            E.add(F);
        } while (++iter < niter);
    }

    private void iterativeEstimation2(Doubles Y, Matrix X) {

        DataBlock B = DataBlock.of(b), E = DataBlock.of(res);
        Doubles W = Y;
        for (int i = 0; i < niter; ++i) {
            DataBlock db = DataBlock.make(b.length), de = DataBlock.make(res.length);
            qr.leastSquares(W, db, de);
            B.add(db);
            E.copy(de);
            double ssq = E.ssq();
            if (ssqerr != 0 && ssq > ssqerr) {
                break;
            }
            ssqerr = ssq;

            DataBlock Err = DataBlock.make(n);
            Cell err = Err.cells();
            CellReader y = Y.reader();
            NeumaierAccumulator acc = new NeumaierAccumulator();
            DataBlockIterator rows = X.rowsIterator();
            // f = y - r - X*b
            while (rows.hasNext()) {
                acc.set(-y.next());
                rows.next().robustDot(B, acc);
                err.setAndNext(-acc.sum());
            }
            W = Err;
        }
    }
}
