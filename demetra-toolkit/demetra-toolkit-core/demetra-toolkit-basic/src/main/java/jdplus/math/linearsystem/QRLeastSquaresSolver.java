/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.math.linearsystem;

import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.math.matrices.decomposition.Householder2;
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import jdplus.math.matrices.decomposition.QRDecomposition;

/**
 * Solves a least squares problem by means of the QR algorithm.
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class QRLeastSquaresSolver {

    @FunctionalInterface
    public interface Processor {

        QRLeastSquaresSolution solve(DoubleSeq y, FastMatrix X);
    }

    /**
     * QR least squares without pivoting
     *
     * @param y
     * @param X
     * @return
     */
    public QRLeastSquaresSolution fastLeastSquares(DoubleSeq y, FastMatrix X) {
        Householder2 h = new Householder2();
        QRDecomposition qr = h.decompose(X);
        return leastSquares(qr, y, 0);
    }

    public QRLeastSquaresSolution robustLeastSquares(DoubleSeq y, FastMatrix X) {
        HouseholderWithPivoting h = new HouseholderWithPivoting();
        QRDecomposition qr = h.decompose(X, 0);
        return leastSquares(qr, y, 1e-9);
    }

    public QRLeastSquaresSolution leastSquares(QRDecomposition qr, DoubleSeq x, double rcond) {
        int rank = UpperTriangularMatrix.rank(qr.rawR(), rcond);
        double[] y = x.toArray();
        qr.applyQt(y);
        int m = qr.m(), n = qr.n();
        DoubleSeq e = DoubleSeq.of(y, rank, m - rank);
        // Solve R*X = Y;
        UpperTriangularMatrix.solveUx(qr.rawR().extract(0, rank, 0, rank), DataBlock.of(y));
        int[] pivot = qr.pivot();
        DoubleSeq b;
        if (pivot == null) {
            if (rank == n) {
                b = DoubleSeq.of(y, 0, rank);
            } else {
                double[] tmp = new double[n];
                for (int i = 0; i < rank; ++i) {
                    tmp[i] = y[i];
                }
                b = DoubleSeq.of(tmp);
            }
        } else {
            double[] tmp = new double[n];
            for (int i = 0; i < rank; ++i) {
                tmp[pivot[i]] = y[i];
            }
            b = DoubleSeq.of(tmp);
        }

        return new QRLeastSquaresSolution(qr, rank, b, e, e.ssq());
    }

}
