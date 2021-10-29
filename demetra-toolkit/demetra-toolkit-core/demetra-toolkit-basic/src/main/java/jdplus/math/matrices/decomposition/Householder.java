/*
 * Copyright 2016 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.matrices.decomposition;

import demetra.data.DoubleSeq;
import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixException;

/**
 *
 * @author Jean Palate
 */
public class Householder{
    
    private double[] qr, rdiag;
    private int n, m;
    
    public Householder(final FastMatrix A){
        init(A);
        householder(true, Constants.getEpsilon());
    }

    public Householder(final FastMatrix A, final boolean fast, final double eps){
        init(A);
        householder(fast, eps);
    }
    
    public DoubleSeq rDiagonal(){
        return DoubleSeq.of(rdiag);
    }
    
//    public QRDecomposition qr(){
//        return new QRDecomposition();
//    }
//
    private void householder(boolean fast, double eps) {
        // Main loop.
        int len = qr.length;
        for (int k = 0, k0 = 0, k1 = m; k < n; ++k) {
            // Compute 2-norm of k-th column .
            DataBlock col = DataBlock.of(qr, k0, k1, 1);
            double nrm = fast ? col.fastNorm2() : col.norm2();

            if (nrm > eps) {
                // Form k-th Householder vector. v(k)=x(k)+/-norm(x)
                if (qr[k0] < -eps) {
                    nrm = -nrm;
                }
                for (int i = k0; i < k1; ++i) {
                    qr[i] /= nrm;
                }
                qr[k0] += 1.0;
                // rdiag contains the main diagonal of the R matrix
                rdiag[k] = -nrm;
                // in this implementation:
                // if a(k,k) < 0 then a(k,k) = -(a(k,k) - nrm) / nrm, else
                // a(k,k)=( a(k,k) + nrm) / nrm

                // Apply transformation to remaining columns.
                for (int jm = k0 + m; jm < len; jm += m) {
                    double s = 0.0;
                    // i+km in [j+km, m+km], 
                    for (int ik = k0, ij = jm; ik < k1; ++ik, ++ij) {
                        s += qr[ik] * qr[ij];
                    }
                    s /= -qr[k0];
                    for (int ik = k0, ij = jm; ik < k1; ++ik, ++ij) {
                        qr[ij] += s * qr[ik];
                    }
                }
                k0 += m + 1;
                k1 += m;
            } else {
                throw new MatrixException(MatrixException.SINGULAR);
            }
        }
    }

    private void init(FastMatrix M) {
        m = M.getRowsCount();
        n = M.getColumnsCount();
        qr = M.toArray();
        rdiag = new double[n];
    }
}
