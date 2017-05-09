/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.matrices.impl;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.spi.SymmetricMatrixAlgorithms;
import demetra.random.IRandomNumberGenerator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = SymmetricMatrixAlgorithms.class)
public final class FastSymmetricMatrixAlgorithms implements SymmetricMatrixAlgorithms {

    @Override
    public void randomize(Matrix M, IRandomNumberGenerator rng) {
        if (!M.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = M.getRowsCount();
        if (n == 1) {
            return;
        }
        int rinc = M.getRowIncrement(), cinc = M.getColumnIncrement(), start = M.getStartPosition();
        double[] x = M.getStorage();
        for (int c = 0, id = start; c < n; ++c, id += rinc + cinc) {
            x[id] = rng.nextDouble();
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                il += rinc;
                iu += cinc;
                double q = rng.nextDouble();
                x[iu] = q;
                x[il] = q;
            }
        }
    }

    @Override
    public void lcholesky(Matrix M, double zero) {
        if (M.getRowIncrement() == 1) {
            lcholesky_1(M, zero);
        } else {
            lcholesky_def(M, zero);
        }
    }

    private void lcholesky_1(Matrix M, double zero) {
        double[] data = M.getStorage();
        int n = M.getRowsCount(), cinc = M.getColumnIncrement(), dinc = 1 + cinc;
        int start = M.getStartPosition(), end = start + n * dinc;
        for (int idiag = start, irow = start, cend = start + n; idiag != end; ++irow, idiag += dinc, cend += cinc) {
            // compute aii;
            double aii = data[idiag];
            for (int j = irow; j != idiag; j += cinc) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii < -zero) { // negative
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (aii <= zero) { // quasi-zero
                data[idiag] = 0;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += cinc) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    if (Math.abs(data[iy]) > zero) {
                        throw new MatrixException(MatrixException.CHOLESKY);
                    } else {
                        data[iy] = 0;
                    }
                }
            } else {
                aii = Math.sqrt(aii);
                data[idiag] = aii;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += cinc) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    data[iy] /= aii;
                }
            }
        }
        LowerTriangularMatrix.toLower(M);
    }

    private void lcholesky_def(Matrix M, double zero) {
        double[] data = M.getStorage();
        int n = M.getRowsCount(), rinc = M.getRowIncrement(), cinc = M.getColumnIncrement(), dinc = rinc + cinc;
        int start = M.getStartPosition(), end = start + n * dinc;
        for (int idiag = start, irow = start, cend = start + n * rinc; idiag != end; irow += rinc, idiag += dinc, cend += cinc) {
            // compute aii;
            double aii = data[idiag];
            for (int j = irow; j != idiag; j += cinc) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii < -zero) { // negative
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (aii <= zero) { // quasi-zero
                data[idiag] = 0;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += cinc) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + rinc, iy = idiag + rinc; iy != cend; ia += rinc, iy += rinc) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + rinc; iy != cend; iy += rinc) {
                    if (Math.abs(data[iy]) > zero) {
                        throw new MatrixException(MatrixException.CHOLESKY);
                    } else {
                        data[iy] = 0;
                    }
                }
            } else {
                aii = Math.sqrt(aii);
                data[idiag] = aii;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += cinc) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + rinc, iy = idiag + rinc; iy != cend; ia += rinc, iy += rinc) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + rinc; iy != cend; iy += rinc) {
                    data[iy] /= aii;
                }
            }
        }
        LowerTriangularMatrix.toLower(M);
    }

    @Override
    public void xxt(DataBlock x, Matrix M) {
        int nr = x.length(), xinc = x.getIncrement();
        int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
        double[] px = x.getStorage(), pm = M.getStorage();
        if (xinc == 1) {
            for (int i = 0, ix = x.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ++ix, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, ++kx, km += mrinc, ks += mcinc) {
                    double z = px[ix] * px[kx];
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        } else {
            for (int i = 0, ix = x.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += xinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xinc, km += mrinc, ks += mcinc) {
                    double z = px[ix] * px[kx];
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        }
    }

    @Override
    public void XXt(final Matrix X, final Matrix M) {
        int nr = X.getRowsCount(), nc = X.getColumnsCount(), xcinc = X.getColumnIncrement(), xrinc = X.getRowIncrement();
        int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
        double[] px = X.getStorage(), pm = M.getStorage();
        if (xcinc != 1) {
            for (int i = 0, ix = X.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += xrinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xrinc, km += mrinc, ks += mcinc) {
                    double z = 0;
                    for (int c = 0, jx = ix, lx = kx; c < nc; ++c, jx += xcinc, lx += xcinc) {
                        z += px[jx] * px[lx];
                    }
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        } else {
            for (int i = 0, ix = X.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += xrinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xrinc, km += mrinc, ks += mcinc) {
                    double z = 0;
                    for (int c = 0, jx = ix, lx = kx; c < nc; ++c, ++jx, ++lx) {
                        z += px[jx] * px[lx];
                    }
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        }
    }

    @Override
    public void LLt(Matrix L, Matrix M) {
        int nr = L.getRowsCount(), nc = L.getColumnsCount(), lcinc = L.getColumnIncrement(), lrinc = L.getRowIncrement();
        int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
        double[] pl = L.getStorage(), pm = M.getStorage();
        if (lcinc == 1) {
            for (int i = 0, ix = L.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += lrinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += lrinc, km += mrinc, ks += mcinc) {
                    double z = 0;
                    for (int jx = ix, lx = kx; jx <= im; ++jx, ++lx) {
                        z += pl[jx] * pl[lx];
                    }
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        } else {
            for (int i = 0, ix = L.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += lrinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += lrinc, km += mrinc, ks += mcinc) {
                    double z = 0;
                    int max = im + lcinc;
                    for (int jx = ix, lx = kx; jx != max; jx += lcinc, lx += lcinc) {
                        z += pl[jx] * pl[lx];
                    }
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        }
    }

    @Override
    public void UUt(Matrix L, Matrix M) {
        int nr = L.getRowsCount(), nc = L.getColumnsCount(), lcinc = L.getColumnIncrement(), lrinc = L.getRowIncrement();
        int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
        double[] pl = L.getStorage(), pm = M.getStorage();
        for (int i = 0, ix = L.getStartPosition(), imax = ix + nc * lcinc, im = M.getStartPosition(); i < nr; ++i, ix += lrinc + lcinc, im += mrinc + mcinc, imax += lrinc) {
            // ix = position of the first item of row i, imax = end of row i
            for (int j = i, kx = ix, ixc = ix, km = im, ks = im; j < nr; ++j, kx += lrinc + lcinc, km += mrinc, ks += mcinc, ixc += lcinc) {
                // kx = position of the first item of column k ixc first used item of row i
                double z = 0;
                for (int jx = ixc, lx = kx; jx != imax; jx += lcinc, lx += lcinc) {
                    z += pl[jx] * pl[lx];
                }
                pm[km] = z;
                if (ks != km) {
                    pm[ks] = z;
                }
            }
        }
    }

    @Override
    public void XtSX(Matrix S, Matrix X, Matrix M) {
        Matrix SX = S.times(X);
        DataBlockIterator rows = SX.columnsIterator(), cols = X.columnsIterator(), mcols = M.columnsIterator();
        int c = 0;
        while (cols.hasNext()) {
            int idx = c;
            rows.reset(c++);
            DataBlock mcol = mcols.next();
            DataBlock col = cols.next();
            while (rows.hasNext()) {
                mcol.set(idx++, rows.next().dot(col));
            }
        }
        SymmetricMatrix.fromLower(M);
    }

    @Override
    public Matrix inverse(Matrix S) {
        try {
            Matrix lower = S.deepClone();
            lcholesky(lower);
            lower = LowerTriangularMatrix.inverse(lower);
            return LtL(lower);
        } catch (MatrixException e) {
            CroutDoolittle cr = new CroutDoolittle();
            cr.decompose(S);
            Matrix I = Matrix.identity(S.getRowsCount());
            cr.solve(I);
            return I;
        }
    }
}
