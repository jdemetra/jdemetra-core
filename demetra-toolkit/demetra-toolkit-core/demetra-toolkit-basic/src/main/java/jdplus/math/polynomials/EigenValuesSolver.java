/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.polynomials;

import demetra.math.Complex;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.decomposition.EigenSystem;
import jdplus.math.matrices.decomposition.IEigenSystem;

/**
 *
 * @author Jean Palate
 */
public class EigenValuesSolver implements RootsSolver {

    private Complex[] roots;

    @Override
    public boolean factorize(Polynomial p) {
        int n = p.degree();
        roots = new Complex[n];
        switch (n) {
            case 0:
                return true;
            case 1:
                monic(p);
                return true;
            case 2:
                quadratic(p);
                return true;
            default:
                double pn = p.get(n);
                FastMatrix M = FastMatrix.square(n + 1);
                M.subDiagonal(-1).drop(0, 1).set(1);
                DataBlock col = M.column(n - 1).drop(0, 1);
                col.setAY(-1 / pn, p.coefficients().drop(0, 1));
                M.set(0, n, 1);
                IEigenSystem es = EigenSystem.create(M, false);
                Complex[] ev = es.getEigenValues();
                System.arraycopy(ev, 0, roots, 0, n);
                return true;
        }
    }

    @Override
    public Polynomial remainder() {
        return Polynomial.ZERO;
    }

    @Override
    public Complex[] roots() {
        return roots;
    }

    private void monic(Polynomial p) {
        double a = p.get(1), b = p.get(0);
        roots[0] = Complex.cart(-b / a);
    }

    private void quadratic(Polynomial p) {
        /* discr = p1^2-4*p2*p0 */
        double a = p.get(2), b = p.get(1), c = p.get(0), aa = 2 * a;
        double rdiscr = b * b - 4 * a * c;
        if (rdiscr < 0) {
            double z = Math.sqrt(-rdiscr);
            Complex r = Complex.cart(-b / aa, +z / aa);
            roots[0] = r;
            roots[1] = r.conj();
        } else {
            double z = Math.sqrt(rdiscr);
            roots[0] = Complex.cart((-b + z) / aa);
            roots[1] = Complex.cart((-b - z) / aa);
        }

    }
}
