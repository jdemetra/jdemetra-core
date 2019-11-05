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

import demetra.math.Constants;

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
        final int n = p.length - 2;
        DoubleDoubleComputer re=new DoubleDoubleComputer(p[n], p[n+1]);
        DoubleDoubleComputer im=new DoubleDoubleComputer();
        for (int i = n - 2; i >= 0; i-=2) {
            double reHigh=re.getHigh(), reLow=re.getLow();
            re=re.mul(xr).subXY(xi, im).add(p[i], p[i+1]);
            im=im.mul(xr).addXY(reHigh, reLow, xi.getHigh(), xi.getLow());
        }
        f = DoubleComplex.cart(re, im);
        return this;
    }

    public DoublePolynomialComputer computeAll(DoubleComplex x) {
        final DoubleDouble xr = x.getRe(), xi = x.getIm();
        if (Math.abs(xi.asDouble()) < Constants.getEpsilon()) {
            return computeAll(xr);
        }
        final int n = p.length - 2;
        DoubleDoubleComputer fr = new DoubleDoubleComputer(p[n], p[n+1]);
        DoubleDoubleComputer dfr = new DoubleDoubleComputer();
        DoubleDoubleComputer fi = new DoubleDoubleComputer();
        DoubleDoubleComputer dfi = new DoubleDoubleComputer();
        for (int i = n - 2; i >= 0; i-=2) {
            double dfrHigh=dfr.getHigh(), dfrLow=dfr.getLow();
            dfr=dfr.mul(xr).subXY(xi, dfi).add(fr);
            dfi=dfi.mul(xr).addXY(dfrHigh, dfrLow, xi.getHigh(), xi.getLow()).add(fi);
            double frHigh=fr.getHigh(), frLow=fr.getLow();
            fr=fr.mul(xr).subXY(xi, fi).add(p[i], p[i+1]);
            fi=fi.mul(xr).addXY(frHigh, frLow, xi.getHigh(), xi.getLow());
        }
        df = DoubleComplex.cart(dfr, dfi);
        f = DoubleComplex.cart(fr, fi);
        return this;
    }

    public DoublePolynomialComputer compute(DoubleDouble x) {
        df = null;
        final int n = p.length - 2;
        DoubleDoubleComputer r = new DoubleDoubleComputer(p[n], p[n+1]);
        for (int i = n - 2; i >= 0; i-=2) {
            r.mul(x).add(p[i], p[i+1]);
        }
        f = DoubleComplex.cart(r, DoubleDouble.ZERO);
        return this;
    }

    public DoublePolynomialComputer computeAll(DoubleDouble x) {
        final int n = p.length - 2;
        DoubleDoubleComputer fr = new DoubleDoubleComputer(p[n], p[n+1]);
        DoubleDoubleComputer dfr = new DoubleDoubleComputer();
        for (int i = n - 2; i >= 0; i-=2) {
            dfr.mul(x).add(fr);
            fr.mul(x).add(p[i], p[i+1]);
        }
        df =DoubleComplex.cart(dfr, DoubleDouble.ZERO);
        f = DoubleComplex.cart(fr, DoubleDouble.ZERO);
        return this;
    }


    public DoubleComplex f() {
        return f;
    }

    public DoubleComplex df() {
        return df;
    }

    
    public DoubleDouble df(int n, DoubleDoubleType x) {
        if (n >= p.length - 0) {
            return DoubleDouble.ZERO;
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
        DoublePolynomialComputer cpt=new DoublePolynomialComputer(P).compute(x);
        return cpt.f;
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
