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
package demetra.arima.internal;

import demetra.arima.ArimaException;
import demetra.arima.AutoCovarianceFunction;
import demetra.data.DataBlock;
import demetra.linearsystem.internal.QRLinearSystemSolver;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.maths.matrices.internal.Householder;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;
import demetra.linearsystem.LinearSystemSolver;

/**
 *
 * @author Jean Palate
 */
public class AutoCovarianceComputers {

    public static AutoCovarianceFunction.Computer defaultComputer(LinearSystemSolver solver) {
        return (Polynomial ma, Polynomial ar, int rank) -> {
            int p = ar.degree();
            int q = ma.degree();
            int r0 = Math.max(p, q)+1;
            if (rank < r0) {
                rank = r0;
            }
            double[] c = new double[rank + 1];
            RationalFunction rfe = RationalFunction.of(ma, ar);
            double[] cr = rfe.coefficients(q+1);

            CanonicalMatrix M = CanonicalMatrix.square(r0);
            DataBlock x = DataBlock.of(c, 0, r0);
            for (int i = 0; i <= q; ++i) {
                double s = 0;
                for (int j = i; j <= q; ++j) {
                    s += ma.get(j) * cr[j - i];
                }
                x.set(i, s);
            }

            for (int i = 0; i < r0; ++i) {
                for (int j = 0; j <= p; ++j) {
                    double w = ar.get(j);
                    if (w != 0) {
                        M.add(i, i < j ? j - i : i - j, w);
                    }
                }
            }
            try {
                if (solver == null) {
                    QRLinearSystemSolver.builder(new Householder()).build().solve(M, x);
                } else {
                    solver.solve(M, x);
                }
            } catch (Exception err) {
                throw new ArimaException(ArimaException.NONSTATIONARY);
            }

            for (int r = r0; r <= rank; ++r) {
                double s = 0;
                for (int j = 1; j <= p; ++j) {
                    s += ar.get(j) * c[r - j];
                }
                c[r] = -s;
            }
            return c;
        };
    }

    public static AutoCovarianceFunction.SymmetricComputer defaultSymmetricComputer(LinearSystemSolver solver) {
        return (SymmetricFilter sma, Polynomial ar, int rank) -> {
            int p = ar.degree();
            int q = sma.getUpperBound();
            int r0 = Math.max(p, q)+1;
            if (rank < r0) {
                rank = r0;
            }
            double[] c;
            if (p == 0) {
                // pure moving average...
                c = sma.coefficientsAsPolynomial().toArray();
            } else {
                c = new double[rank + 1];
                BackFilter g = sma.decompose(new BackFilter(ar));
                double[] tmp = RationalFunction.of(g.asPolynomial(), ar).coefficients(rank + 1);

                System.arraycopy(tmp, 0, c, 0, tmp.length);
                c[0] *= 2;
            }

            if (rank < c.length) {
                return c;
            }

            int k0 = c.length;
            double[] tmp = new double[rank];
            System.arraycopy(c, 0, tmp, 0, k0);
            c = tmp;
            if (p > 0) {
                for (int r = k0; r < rank; ++r) {
                    double s = 0;
                    for (int x = 1; x <= p; ++x) {
                        s += ar.get(x) * c[r - x];
                    }
                    c[r] = -s;
                }
            }
            return c;
        };
    }
}
