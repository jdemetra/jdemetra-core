/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.leastsquares.internal;

import jdplus.data.DataBlock;
import jdplus.maths.matrices.Matrix;
import jdplus.maths.matrices.MatrixException;
import jdplus.maths.matrices.decomposition.Householder;
import jdplus.leastsquares.QRSolver;
import demetra.design.AlgorithmImplementation;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.decomposition.QRDecomposition;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@AlgorithmImplementation(algorithm = QRSolver.class)
public class DefaultQRSolver implements QRSolver {

    private double ssqerr;
    private double[] b, res;
    private Matrix R;
    private int[] used;
    private int n, m;
    private QRDecomposition qr;
    private final QRDecomposition.Processor processor;

    public DefaultQRSolver() {
        this(new Householder.Processor());
    }

    public DefaultQRSolver(QRDecomposition.Processor qr) {
        this.processor = qr;
    }

    @Override
    public boolean solve(DoubleSeq y, FastMatrix x) {
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

    private void compute(DoubleSeq y, FastMatrix x) {

        // X'X, X'y
        n = y.length();
        m = x.getColumnsCount();
        qr = processor.decompose(x);
        int r = qr.rank();
        R = qr.r(true);
        b = new double[r];
        res = new double[n - qr.rank()];
        used = qr.used();
        res = new double[n - qr.rank()];
        DataBlock B = DataBlock.of(b), E = DataBlock.of(res);
        qr.leastSquares(y, B, E);
        ssqerr = E.ssq();
    }

    @Override
    public DoubleSeq coefficients() {
        if (used.length == m) {
            return DoubleSeq.of(b);
        } else {
            // expand the coefficients
            double[] c = new double[m];
            for (int i = 0; i < used.length; ++i) {
                c[used[i]] = b[i];
            }
            return DoubleSeq.of(c);
        }
    }

    @Override
    public DoubleSeq residuals() {
        return DoubleSeq.of(res);
    }

    @Override
    public double ssqerr() {
        return ssqerr;
    }

    /**
     * @return the R
     */
    @Override
    public Matrix R() {
        return qr.r(false);
    }
}
