/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters.internal;

import demetra.maths.Complex;
import demetra.maths.ComplexComputer;
import java.util.function.IntToDoubleFunction;
import jdplus.data.DataBlock;
import jdplus.maths.ComplexMath;
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
public class EigenValuesDecomposer2 {

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
                int n = P.degree() / 2;
                if (n > 0) {
                    Polynomial C = Polynomial.ofInternal(P.coefficients().extract(n, n + 1).toArray());
                    CanonicalMatrix M = CanonicalMatrix.square(n);
                    DataBlock col = M.column(n - 1);
                    double sn = C.get(n);
                    col.set(i -> -C.get(i) / sn);
                    if (n > 1) {
                        M.subDiagonal(-1).set(1);
                        M.set(0, 1, 1);
                        M.subDiagonal(1).add(1);
                    }
                    IEigenSystem es = EigenSystem.create(M, false);
                    Complex[] vals = es.getEigenValues();
                    Complex[] nvals = new Complex[vals.length];
                    double[] uvals = new double[vals.length / 2];
                    int k = 0;
                    for (int i = 0, j = 0; i < vals.length; ++i) {
                        // solve x^2-ux+1=0
                        Complex u = vals[i];
                        if (u.getIm() == 0) {
                            double r = u.getRe();
                            double rho = r * r - 4;
                            if (rho < -EPS) {
                                Complex c = Complex.cart(r / 2, Math.sqrt(-rho) / 2);
                                int l = 0;
                                for (; l < k; ++l) {
                                    if (Math.abs(r - uvals[l]) < 1e-6) {
                                        uvals[l] = 0;
                                        break;
                                    }
                                }
                                if (l == k) {
                                    uvals[k++] = r;
                                    nvals[j++] = c;
                                    nvals[j++] = c.conj();
                                }
                                continue;
                            } else if (rho < 0) {
                                rho = 0;
                                r = r < 0 ? -2 : 2;
                            }
                            double srho = Math.sqrt(rho);
                            if (r < 0) {
                                nvals[j++] = Complex.cart((r - srho) / 2);
                            } else {
                                nvals[j++] = Complex.cart((r + srho) / 2);
                            }
                        } else if (u.getIm() > 0) {
                            ComplexComputer computer = new ComplexComputer(u);
                            computer.mul(u).sub(4);
                            Complex srho = ComplexMath.sqrt(computer);
                            ComplexComputer x1 = new ComplexComputer(u);
                            x1.add(srho).div(2);
                            ComplexComputer x2 = new ComplexComputer(u);
                            x2.sub(srho).div(2);
                            Complex z;
                            if (x1.abs() > x2.abs()) {
                                z = x1.result();
                            } else {
                                z = x2.result();
                            }
                            nvals[j++] = z;
                            nvals[j++] = z.conj();
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
