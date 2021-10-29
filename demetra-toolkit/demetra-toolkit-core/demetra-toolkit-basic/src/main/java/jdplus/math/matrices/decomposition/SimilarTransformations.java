/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.MatrixWindow;

/**
 * SimilarTransformations of matrices 
 * B is similar to A if B=P^-1*A*P
 * Similar matrices share many properties: 
 * same characteristic polynomial (same determinant, trace, eigen values...)
 * same rank
 * same froebenius norm
 * same Jordan normal form
 * ...
 * 
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SimilarTransformations {

    @lombok.experimental.UtilityClass
    public class Balancing {

        public final double RADIX = 2;

        public void balance(FastMatrix A) {
            if (!A.isSquare()) {
                throw new MatrixException(MatrixException.SQUARE);
            }

            double sqrd = RADIX * RADIX;
            int n = A.getColumnsCount();
            double[] a = A.getStorage();
            int last = 0;

            while (last == 0) {
                last = 1;
                for (int i = 0, idxci = 0; i < n; i++, idxci += n) {
                    double r = 0.0;
                    double c = 0.0;
                    for (int j = 0, idxji = idxci, idxij = i; j < n; j++, idxji++, idxij += n) {
                        if (j != i) {
                            c += Math.abs(a[idxji]);
                            r += Math.abs(a[idxij]);
                        }
                    }

                    if (c != 0.0 && r != 0.0) {
                        double g = r / RADIX;
                        double f = 1.0;
                        double s = c + r;
                        while (c < g) {
                            f *= RADIX;
                            c *= sqrd;
                        }

                        g = r * RADIX;
                        while (c > g) {
                            f /= RADIX;
                            c /= sqrd;
                        }

                        if ((c + r) / f < 0.95 * s) {
                            last = 0;
                            g = 1.0 / f;
                            for (int j = 0, idx = i; j < n; j++, idx += n) {
                                a[idx] *= g;
                            }
                            for (int j = 0, idx = idxci; j < n; j++, idx++) {
                                a[idx] *= f;
                            }
                        }
                    }
                }
            }
        }
    }

    @lombok.experimental.UtilityClass
    public class Hessenberg {

        /**
         * Transform a given matrix to a similar upper Hessenberg matrix using
         * Householder reflections
         *
         * @param A The matrix being transformed
         */
        public void householder(FastMatrix A) {
            int n = A.getRowsCount();
            if (!A.isSquare()) {
                throw new MatrixException(MatrixException.SQUARE);
            } else if (n > 2) {
                MatrixWindow wnd = A.all(), rwnd=A.all();
                for (int i = 1; i < n - 1; ++i) {
                    DataBlockIterator cols = wnd.bvshrink().columnsIterator();
                    HouseholderReflection hr = HouseholderReflection.of(cols.next(), true);
                    while (cols.hasNext()) {
                        hr.transform(cols.next());
                    }
                    wnd.bhshrink();
                    DataBlockIterator rows = rwnd.bhshrink().rowsIterator();
                    while (rows.hasNext()) {
                        hr.transform(rows.next());
                    }
                }
            }
        }

        /**
         * Transform a given matrix to a similar upper Hessenberg matrix using
         * Gauss eliminations
         *
         * @param A The matrix being transformed
         */
        public void gauss(FastMatrix A) {
            int n = A.getRowsCount();
            if (!A.isSquare()) {
                throw new MatrixException(MatrixException.SQUARE);
            } else if (n > 2) {
                double[] h = A.getStorage();
                for (int m = 1, idx = 0; m < n - 1; m++, idx += n) {
                    double x = 0.0;
                    int i = m;
                    for (int j = m, idx2 = idx + j; j < n; j++, idx2++) {
                        if (Math.abs(h[idx2]) > Math.abs(x)) {
                            x = h[idx2];
                            i = j;
                        }
                    }
                    int idxi = i * n;
                    int idxm = m * n;
                    if (i != m) {
                        for (int j = m - 1, idx2 = j * n; j < n; j++, idx2 += n) {
                            swap(h, idx2 + i, idx2 + m);
                        }
                        for (int j = 0; j < n; j++) {
                            swap(h, idxi + j, idxm + j);
                        }
                    }
                    if (x != 0.0) {
                        for (int l = m + 1; l < n; l++) {
                            double y = h[idx + l];
                            if (y != 0.0) {
                                y /= x;
                                h[idx + l] = y;
                                for (int j = m, idxj = m * n; j < n; j++, idxj += n) {
                                    h[idxj + l] -= y * h[idxj + m];
                                }
                                for (int j = 0, idxl = l * n; j < n; j++, idxl++) {
                                    h[idxm + j] += y * h[idxl];
                                }
                            }
                        }
                    }
                }

                // zero out all subdiagonal elements except for the first subdiagonal
                for (int i = 0, idx = 0; i < n - 2; i++, idx += n) {
                    for (int j = i + 2, idx2 = idx + j; j < n; j++, idx2++) {
                        h[idx2] = 0.0;
                    }
                }
            }
        }

        /**
         * Check that a given matrix is Hessenberg
         *
         * @param A The considered matrix
         * @return
         */
        public boolean isHessenberg(FastMatrix A) {
            int n = A.getColumnsCount() - 2;
            for (int i = 0; i < n; ++i) {
                if (!A.column(i).drop(i + 2, 0).isZero(0)) {
                    return false;
                }
            }
            return true;
        }

        private void swap(double[] x, int i, int j) {
            double tmp = x[i];
            x[i] = x[j];
            x[j] = tmp;
        }

    }
}
