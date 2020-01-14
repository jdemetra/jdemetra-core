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
package jdplus.math.polynomials;

import demetra.math.Complex;
import demetra.math.Constants;

/**
 *
 * @author Jean Palate
 */
public class PolynomialComputer {

    private final double[] p;
    private Complex f, df;

    public PolynomialComputer(Polynomial x) {
        this.p = x.toArray();
    }
    
    public int getPolynomialDegree(){
        return p.length-1;
    }

    public PolynomialComputer compute(Complex x) {
        final double xr = x.getRe(), xi = x.getIm();
        if (Math.abs(xi) < Constants.getEpsilon()) {
            return compute(xr);
        }
        df = null;
        final int n = p.length - 1;
        double re = p[n], im = 0;
        for (int i = n - 1; i >= 0; i--) {
            double rtmp = xr * re - xi * im + p[i];
            double itmp = xr * im + re * xi;
            re = rtmp;
            im = itmp;
        }
        f = Complex.cart(re, im);
        return this;
    }

    public PolynomialComputer computeAll(Complex x) {
        final double xr = x.getRe(), xi = x.getIm();
        if (Math.abs(xi) < Constants.getEpsilon()) {
            return computeAll(xr);
        }
        final int n = p.length - 1;
        double fr = p[n], fi = 0, dfr = 0, dfi = 0;
        for (int i = n - 1; i >= 0; i--) {
            double tr = xr * dfr - xi * dfi + fr;
            double ti = xr * dfi + dfr * xi + fi;
            dfr = tr;
            dfi = ti;
            tr = xr * fr - xi * fi + p[i];
            ti = xr * fi + fr * xi;
            fr = tr;
            fi = ti;
        }
        df = Complex.cart(dfr, dfi);
        f = Complex.cart(fr, fi);
        return this;
    }

    public PolynomialComputer compute(double x) {
        df = null;
        final int n = p.length - 1;
        double r = p[n];
        for (int i = n - 1; i >= 0; i--) {
            r = x * r + p[i];
        }
        f = Complex.cart(r);
        return this;
    }

    public PolynomialComputer computeAll(double x) {
        final int n = p.length - 1;
        double fr = p[n], dfr = 0;
        for (int i = n - 1; i >= 0; i--) {
            dfr = x * dfr + fr;
            fr = x * fr + p[i];
        }
        df = Complex.cart(dfr);
        f = Complex.cart(fr);
        return this;
    }


    public Complex f() {
        return f;
    }

    public Complex df() {
        return df;
    }

    
    public double df(int n, double x) {
        if (n >= p.length - 0) {
            return 0;
        }
        // Not optimal
        Polynomial P = D(n);
        return P.evaluateAt(x);
    }

    public Complex df(int n, Complex x) {
        if (n >= p.length - 0) {
            return Complex.ZERO;
        }
        // Not optimal
        Polynomial P = D(n);
        return P.evaluateAt(x);
    }

    public Polynomial D(int n) {
        Polynomial P = Polynomial.ofInternal(p);
        int d = 0;
        while (d++ < n) {
            P = P.derivate();
        }
        return P;
    }

}
