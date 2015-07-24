/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class ElementaryTransformations {

    public static void transform(IVectorTransformation transformation, DataBlockIterator vectors) {
        DataBlock cur = vectors.getData();
        do {
            transformation.transform(cur);
        } while (vectors.next());
    }

    public static void rowHouseholder(SubMatrix m) {
        householder(m.rows());
    }

    public static boolean givensTriangularize(final SubMatrix X) {
        try {
            int r = X.getRowsCount(), c = X.getColumnsCount();
            SubMatrix L = X;
            do {
                //ElementaryTransformations.rowHouseholder(L);
                rowGivens(L);
                L = L.extract(1, r, 1, c);
                --r;
                --c;
            } while (!L.isEmpty());
            return true;
        } catch (BaseException err) {
            return false;
        }
    }

    public static boolean rawGivensTriangularize(final SubMatrix X) {
        try {
            int nr = X.m_nrows, nc = X.m_ncols, rinc = X.m_row_inc, cinc = X.m_col_inc, beg = X.m_start;
            int dinc = rinc + cinc;
            double[] x = X.m_data;
            for (int r = 0, idiag = beg; r < nr; ++r, idiag += dinc) {
                for (int c = r + 1, cur = idiag + cinc; c < nc; ++c, cur += cinc) {
                    double a = x[idiag];
                    double b = x[cur];
                    if (b != 0) {
                        // compute the rotation
                        double h, ro, d;
                        if (a != 0) {
                            h = ElementaryTransformations.hypotenuse(a, b);
                            ro = b / h;
                            d = a / h;
                        } else {
                            d = 0;
                            ro = 1;
                            h = b;
                        }
                        x[cur] = 0;
                        a = h;
                        x[idiag] = a;
                        // update the next rows
                        for (int s = r + 1, sdiag = idiag + rinc, scur = cur + rinc; s < nr; ++s, sdiag += rinc, scur += rinc) {
                            a = x[sdiag];
                            b = x[scur];
                            x[sdiag] = d * a + ro * b;
                            x[scur] = -ro * a + d * b;
                        }
                    }
                }
            }
            return true;
        } catch (BaseException err) {
            return false;
        }
    }

    public static boolean householderTriangularize(final SubMatrix X) {
        try {
            int r = X.getRowsCount(), c = X.getColumnsCount();
            SubMatrix L = X;
            do {
                rowHouseholder(L);
                L = L.extract(1, r, 1, c);
                --r;
                --c;
            } while (!L.isEmpty());
            return true;
        } catch (BaseException err) {
            return false;
        }
    }

    // apply givens rotations on the first row and transform the next rows.
    public static void rowGivens(SubMatrix m) {
        givens(m.rows(), m.getColumnsCount());
//        DataBlock r0=m.row(0);
//        SubMatrix rest=m.extract(1, m.getRowsCount(), 0, m.getColumnsCount());
//         for (int i = 1; i < m.getColumnsCount(); ++i) {
//            if (r0.get(i) != 0) {
//                GivensRotation rotation = new GivensRotation(r0, i);
//                rotation.ctransform(rest);
//            }
//        }
    }

    public static void columnHouseholder(SubMatrix m) {
        householder(m.columns());
    }

    private static void householder(DataBlockIterator vectors) {
        DataBlock cur = vectors.getData();
        HouseholderReflection reflection = HouseholderReflection.from(cur);
        while (vectors.next()) {
            reflection.transform(cur);
        }
    }

    private static void givens(DataBlockIterator vectors, int n) {
        DataBlock cur = vectors.getData();
        for (int i = 1; i < n; ++i) {
            vectors.begin();
            if (cur.get(i) != 0) {
                GivensRotation rotation = new GivensRotation(cur, i);
                while (vectors.next()) {
                    rotation.transform(cur);
                }
            }
        }
    }

    /**
     * Returns sqrt(x**2+y**2), taking care not to cause unnecessary overflow.
     *
     * @param x
     * @param y
     * @return
     */
    public static double hypotenuse(double x, double y) {
        // Purpose
        // =======
        // DLAPY2
        // Arguments
        // =========
        // X (input) DOUBLE PRECISION
        // Y (input) DOUBLE PRECISION
        // X and Y specify the values x and y.
        // =====================================================================
        double xabs = Math.abs(x);
        double yabs = Math.abs(y);
        double w = Math.max(xabs, yabs);
        double z = Math.min(xabs, yabs);
        if (z == 0) {
            return w;
        } else {
            double zw = z / w;
            return w * Math.sqrt(1 + zw * zw);
        }
    }
}
