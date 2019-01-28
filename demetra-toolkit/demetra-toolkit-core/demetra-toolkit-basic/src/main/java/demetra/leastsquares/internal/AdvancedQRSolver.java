/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares.internal;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.Matrix;
import demetra.maths.MatrixException;
import demetra.maths.matrices.decomposition.IQRDecomposition;
import demetra.data.accumulator.NeumaierAccumulator;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.leastsquares.QRSolver;
import demetra.data.DoubleCell;
import demetra.design.AlgorithmImplementation;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@AlgorithmImplementation(algorithm = QRSolver.class)
public class AdvancedQRSolver implements QRSolver {

    @BuilderPattern(QRSolver.class)
    public static class Builder {

        private final IQRDecomposition qr;
        private int niter = 1;
        private boolean simple;

        private Builder(IQRDecomposition qr) {
            this.qr = qr;
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

        public AdvancedQRSolver build() {
            return new AdvancedQRSolver(qr, niter, simple);
        }
    }

    public static Builder builder(IQRDecomposition qr) {
        return new Builder(qr);
    }
    private double ssqerr;
    private double[] b, res;
    private Matrix R;
    private int[] used;
    private int n, m;
    private final IQRDecomposition qr;
    private final boolean simple;
    private final int niter;
    
    public AdvancedQRSolver(){
        this(new Householder(), 1, false);
    }
    
    private AdvancedQRSolver(IQRDecomposition qr, int niter, boolean simple) {
        this.qr = qr;
        this.niter = niter;
        this.simple = simple;
    }

    @Override
    public boolean solve(DoubleSequence y, Matrix x) {
        try {
            clear();
            compute(y, x);
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    private void clear() {
        ssqerr = 0;
        R = null;
        b = null;
        res = null;
    }

    private void compute(DoubleSequence y, Matrix x) {

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
                iterativeEstimation2(DataBlock.of(y), x);
            } else {
                iterativeEstimation(DataBlock.of(y), x);
            }
        } else {
            res = new double[n - qr.rank()];
            DataBlock B = DataBlock.ofInternal(b), E = DataBlock.ofInternal(res);
            qr.leastSquares(y, B, E);
            ssqerr = E.ssq();
        }
    }

    @Override
    public DoubleSequence coefficients() {
        if (used.length == m) {
            return DoubleSequence.ofInternal(b);
        } else {
            // expand the coefficients
            double[] c = new double[m];
            for (int i = 0; i < used.length; ++i) {
                c[used[i]] = b[i];
            }
            return DoubleSequence.ofInternal(c);
        }
    }

    public DoubleSequence residuals() {
        return DoubleSequence.ofInternal(res);
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
    private void iterativeEstimation(DoubleSequence Y, Matrix X) {
        DataBlock F = DataBlock.make(n);
        DataBlock G = DataBlock.make(m);

        DataBlock B = DataBlock.ofInternal(b);
        DataBlock E = DataBlock.make(n);
        // step 1
        int iter = 0;
        do {
            DoubleCell f = F.cells();
            DoubleCell g = G.cells(), e = E.cells();
            DoubleReader y = Y.reader();
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
            // solve in place QtF
            qr.applyQt(F);

            // solve in place R'h=g or h'R=g'
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

    private void iterativeEstimation2(DoubleSequence Y, Matrix X) {

        DataBlock B = DataBlock.ofInternal(b), E = DataBlock.ofInternal(res);
        DoubleSequence W = Y;
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
            DoubleCell err = Err.cells();
            DoubleReader y = Y.reader();
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

    /**
     * @return the R
     */
    @Override
    public Matrix R() {
        return qr.r(false);
    }
}
