/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters.internal;

import demetra.maths.Complex;
import java.util.function.IntToDoubleFunction;
import jdplus.data.DataBlock;
import jdplus.maths.ComplexUtility;
import jdplus.maths.linearfilters.BackFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.decomposition.EigenSystem;
import jdplus.maths.matrices.decomposition.IEigenSystem;
import jdplus.maths.polynomials.LeastSquaresDivision;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.UnitRoots;
import jdplus.maths.polynomials.UnitRootsSolver;

/**
 * Decomposition based on the eigen values
 *
 * @author Jean Palate
 */
public class EigenValuesDecomposer {

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
                w = P.toArray();
                int n = P.degree();
                if (n > 0) {
                    CanonicalMatrix M = CanonicalMatrix.square(n+1);
                    M.subDiagonal(-1).drop(0,1).set(1);
                    DataBlock col = M.column(n - 1).drop(0,1);
                    col.setAY(-1 / w[n], DataBlock.of(w, 0, n));
                    M.set(0, n,1);
                    IEigenSystem es = EigenSystem.create(M, false);
                    Complex[] vals = es.getEigenValues();
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
                Polynomial urp = sur.toPolynomial();
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
