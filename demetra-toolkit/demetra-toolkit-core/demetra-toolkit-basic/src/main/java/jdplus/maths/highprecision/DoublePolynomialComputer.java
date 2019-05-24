/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.maths.highprecision;

import demetra.maths.Constants;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DoublePolynomialComputer {
    
    private final double[] p;
    private DoubleComplex f, df;

    public DoublePolynomialComputer(DoublePolynomial x) {
        this.p = x.storage();
    }
    
    public int getPolynomialDegree(){
        return (p.length>>1)-1;
    }

    public DoublePolynomialComputer compute(DoubleComplex x) {
        final DoubleDouble xr = x.getRe(), xi = x.getIm();
        if (Math.abs(xi.asDouble()) < Constants.getEpsilon()) {
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

    public DoublePolynomialComputer computeAll(DoubleComplex x) {
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
        df = DoubleComplex.cart(dfr, dfi);
        f = DoubleComplex.cart(fr, fi);
        return this;
    }

    public DoublePolynomialComputer compute(DoubleDouble x) {
        df = null;
        final int n = p.length - 1;
        double r = p[n];
        for (int i = n - 1; i >= 0; i--) {
            r = x * r + p[i];
        }
        f = DoubleComplex.cart(r);
        return this;
    }

    public DoublePolynomialComputer computeAll(double x) {
        final int n = p.length - 1;
        double fr = p[n], dfr = 0;
        for (int i = n - 1; i >= 0; i--) {
            dfr = x * dfr + fr;
            fr = x * fr + p[i];
        }
        df =DoubleComplex.cart(dfr);
        f = DoubleComplex.cart(fr);
        return this;
    }


    public DoubleComplex f() {
        return f;
    }

    public DoubleComplex df() {
        return df;
    }

    
    public double df(int n, double x) {
        if (n >= p.length - 0) {
            return 0;
        }
        // Not optimal
        DoublePolynomial P = D(n);
        return P.evaluateAt(x);
    }

    public DoubleComplex df(int n, DoubleComplex x) {
        if (n >= p.length - 0) {
            return DoubleComplex.ZERO;
        }
        // Not optimal
        DoublePolynomial P = D(n);
        return P.evaluateAt(x);
    }

    public DoublePolynomial D(int n) {
        DoublePolynomial P = DoublePolynomial.ofInternal(p);
        int d = 0;
        while (d++ < n) {
            P = P.derivate();
        }
        return P;
    }
    
}
