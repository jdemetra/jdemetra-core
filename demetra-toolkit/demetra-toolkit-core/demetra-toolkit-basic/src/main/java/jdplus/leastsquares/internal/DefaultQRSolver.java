/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.leastsquares.internal;

import jdplus.data.DataBlock;
import jdplus.math.matrices.decomposition.Householder;
import jdplus.leastsquares.QRSolver;
import demetra.design.AlgorithmImplementation;
import demetra.data.DoubleSeq;
import jdplus.leastsquares.QRSolution;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.QRDecomposer;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@AlgorithmImplementation(algorithm = QRSolver.class)
public class DefaultQRSolver implements QRSolver {

    private final QRDecomposer.Processor processor;

    public DefaultQRSolver() {
        this(new Householder.Processor());
    }

    public DefaultQRSolver(QRDecomposer.Processor qr) {
        this.processor = qr;
    }

    @Override
    public QRSolution solve(DoubleSeq y, Matrix X) {
        // X'X, X'y
        int n = y.length();
        int m = X.getColumnsCount();
        QRDecomposer qr = processor.decompose(X);
        int r = qr.rank();
        Matrix R = qr.r(false);
        double[] b = new double[r];
        double[] res = new double[n - qr.rank()];
        int[] used = qr.used();
        res = new double[n - qr.rank()];
        DataBlock B = DataBlock.of(b), E = DataBlock.of(res);
        qr.leastSquares(y, B, E);
        double ssqerr = E.ssq();
        if (used.length != m) {
            // expand the coefficients
            double[] c = new double[m];
            for (int i = 0; i < used.length; ++i) {
                c[used[i]] = b[i];
            }
            b = c;
        }
        return new QRSolution(DoubleSeq.of(b), DoubleSeq.of(res), ssqerr, R);
    }

}
