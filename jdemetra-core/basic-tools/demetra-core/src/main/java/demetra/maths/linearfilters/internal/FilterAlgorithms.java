/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.linearfilters.internal;

import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.linearsystem.ILinearSystemSolver;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.CroutDoolittle;
import demetra.maths.polynomials.Polynomial;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FilterAlgorithms {

    public static SymmetricFilter.Decomposer symmetricFilterDecomposer(final ILinearSystemSolver solver) {
        return (SymmetricFilter filter, BackFilter Q) -> {
            if (Q.length() == 1) {

                double[] data = filter.coefficients().toArray();
                data[0] /= 2;
                Polynomial tmp = Polynomial.ofInternal(data);
                double q0 = Q.get(0);
                if (q0 != 1) {
                    tmp = tmp.divide(q0);
                }
                return new BackFilter(tmp);
            }

            Polynomial q = Q.asPolynomial();
            Doubles c = filter.coefficients();

            int nq = q.length();
            int nc = c.length();
            int r = nq > nc ? nq : nc;

            Matrix a = Matrix.square(r);
            double[] mc = new double[r];
            for (int i = 0; i < r; ++i) {
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

}
