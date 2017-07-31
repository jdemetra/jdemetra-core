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
package demetra.maths.linearfilters.internal;

import demetra.data.DataBlock;
import demetra.linearsystem.ILinearSystemSolver;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.polynomials.Polynomial;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
public class SymmetricFilterAlgorithms {

    public static SymmetricFilter.Decomposer decomposer(ILinearSystemSolver solver) {
        return (SymmetricFilter filter, final BackFilter Q) -> {
            if (Q.length() == 1) {
                double[] data = filter.coefficientsAsPolynomial().toArray();
                data[0] /= 2;
                Polynomial tmp = Polynomial.ofInternal(data);
                double q0 = Q.get(0);
                if (q0 != 1) {
                    tmp = tmp.divide(q0);
                }
                return new BackFilter(tmp);
            }

            Polynomial q = Q.asPolynomial();
            Polynomial c = filter.coefficientsAsPolynomial();

            int nq = q.length() - 1;
            int nc = c.length() - 1;
            int r = nq > nc ? nq : nc;

            Matrix a = Matrix.square(r + 1);
            double[] mc = new double[r + 1];
            for (int i = 0; i <= r; ++i) {
                mc[r - i] = i <= nc ? c.get(i) : 0;
                for (int j = 0; j <= i; ++j) {
                    if (i - j <= nq) {
                        double a1 = q.get(i - j);
                        a.set(i, j, a.get(i, j) + a1);
                    }
                    if (r - i + j <= nq) {
                        double a2 = q.get(r - i + j);
                        a.set(i, r - j, a.get(i, r - j) + a2);
                    }
                }
            }
            DataBlock g = DataBlock.ofInternal(mc);
            if (solver == null) {
                ILinearSystemSolver.robustSolver().solve(a, g);
            } else {
                solver.solve(a, g);
            }
            return BackFilter.ofInternal(g.reverse().toArray());
        };
    }

    public static SymmetricFilter.Factorizer fastFactorizer() {
        return (SymmetricFilter filter) -> {
            SymmetricFrequencyResponseDecomposer decomposer = new SymmetricFrequencyResponseDecomposer();
            if (!decomposer.decompose(filter)) {
                return null;
            } else {
                return new SymmetricFilter.Factorization(decomposer.getBFilter(), decomposer.getFactor());
            }
        };
    }

    public static SymmetricFilter.Factorizer robustFactorizer() {
        return (SymmetricFilter filter) -> {
            RobustSymmetricFrequencyResponseDecomposer decomposer = new RobustSymmetricFrequencyResponseDecomposer();
            if (!decomposer.decompose(filter)) {
                return null;
            } else {
                return new SymmetricFilter.Factorization(decomposer.getBFilter(), decomposer.getFactor());
            }
        };
    }

    private static final int ROBUST_LIMIT = 5;

    public static SymmetricFilter.Factorizer factorizer() {
        return (SymmetricFilter filter) -> filter.length() > ROBUST_LIMIT ? robustFactorizer().factorize(filter)
                : fastFactorizer().factorize(filter);

    }
}
