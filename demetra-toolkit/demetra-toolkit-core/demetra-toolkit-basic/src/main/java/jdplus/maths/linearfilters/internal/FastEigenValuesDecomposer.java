/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters.internal;

import demetra.math.Complex;
import java.util.function.IntToDoubleFunction;
import jdplus.data.DataBlock;
import jdplus.math.ComplexUtility;
import jdplus.maths.linearfilters.BackFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.maths.polynomials.FastEigenValuesSolver;
import jdplus.maths.polynomials.LeastSquaresDivision;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.UnitRoots;
import jdplus.maths.polynomials.UnitRootsSolver;

/**
 * Decomposition based on the eigen values
 *
 * @author Jean Palate
 */
public class FastEigenValuesDecomposer {

    private double fac;
    private BackFilter bf;

    public BackFilter getBFilter() {
        return bf;
    }

    public double getFactor() {
        return fac;
    }

    private static final double EPS = 1e-9;

    public boolean decompose(SymmetricFilter sf) {
        try {
            IntToDoubleFunction weights = sf.weights();
            double var = weights.applyAsDouble(0);
            if (var < 0) {
                return false;
            }
            if (sf.length() == 1) {
                bf = BackFilter.ONE;
                fac = var;
            } else {
                // first, we remove possible unit roots (otherwise, we will get into trouble)
                double[] w = sf.weightsToArray();
                for (int i = 0; i < w.length; ++i) {
                    w[i] /= var;
                }
                Polynomial P = Polynomial.of(w);
                P = removeUnitRoots(P);
                int n = P.degree();
                if (n > 0) {
                    FastEigenValuesSolver solver=new FastEigenValuesSolver();
                    solver.factorize(P);
                    Complex[] vals = solver.roots();
                    Complex[] nvals = new Complex[vals.length / 2];
                    for (int i = 0, j = 0; i < vals.length; ++i) {
                        Complex cur = vals[i];
                        if (cur.equals(Complex.ZERO, 1e-9))
                            continue;
                        if (cur.abs() > 1) {
                            nvals[j++] = vals[i];
                        }
                    }
                    ComplexUtility.lejaOrder(nvals);
                    Polynomial Z = Polynomial.fromComplexRoots(nvals);
                    Z = Z.times(1 / Z.get(0));
                    bf = bf.times(new BackFilter(Z));
                }
            }
            fac = var / bf.asPolynomial().coefficients().ssq();
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    private Polynomial removeUnitRoots(Polynomial P) {
        UnitRootsSolver urs = new UnitRootsSolver(0);
        if (urs.factorize(P)) {
            UnitRoots ur = urs.getUnitRoots();
            UnitRoots sur = ur.sqrt();
            if (sur != null) {
                Polynomial urp = sur.asPolynomial();
                Polynomial ur2 = urp.times(urp);
                bf = new BackFilter(urp);
                // ensure symmetry
                LeastSquaresDivision lsd = new LeastSquaresDivision();
                lsd.divide(P, ur2);
                P = lsd.getQuotient();
                double[] c = P.toArray();
                int d = P.degree();
                int n = d / 2;
                for (int i = 0; i < n; ++i) {
                    double q = (c[i] + c[d - i]) / 2;
                    c[i] = q;
                    c[d - i] = q;
                }
                P = Polynomial.of(c);
            }
        }
        if (bf == null) {
            bf = BackFilter.ONE;
        }
        return P;
    }
}
