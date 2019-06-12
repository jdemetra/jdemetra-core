/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package demetra.arima;

import demetra.design.Development;
import demetra.maths.RealPolynomial;

/**
 * Box-Jenkins seasonal arima model AR(B)* SAR(B)*D(B)*SD(B) y(t) =
 * MA(B)*SMA(B)e(t), e~N(0, var) AR(B) = 1+a(1)B+...+a(p)B^p, regular
 * auto-regressive polynomial SAR(B) = 1+b(1)B^s+...+b(bp)B^s*bp, seasonal
 * auto-regressive polynomial D(B) = 1+e(1)B+...+e(d)B^d, regula differencing
 * polynomial SD(B) = 1+f(1)B^s+...+f(bd)B^s*bd, seasonal differencing
 * polynomial MA(B) = 1+c(1)B+...+c(q)B^q, regular moving average polynomial
 * SMA(B) = 1+d(1)B^s+...+d(bq)B^s*bq, seasonal moving average polynomial
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class SarimaModel implements ArimaType {

//    @lombok.Builder.Default
//    private int period=1;
//    private int d, bd;
//    @lombok.Builder.Default
//    @lombok.NonNull 
//    private PolynomialType phi=PolynomialType.ONE;
//    @lombok.Builder.Default
//    @lombok.NonNull 
//    private PolynomialType theta=PolynomialType.ONE;
//    @lombok.Builder.Default
//    @lombok.NonNull 
//    private PolynomialType bphi=PolynomialType.ONE;
//    @lombok.Builder.Default
//    @lombok.NonNull 
//    private PolynomialType btheta=PolynomialType.ONE;
    private int period;
    private int d, bd;
    @lombok.NonNull
    private double[] phi;
    @lombok.NonNull
    private double[] theta;
    @lombok.NonNull
    private double[] bphi;
    @lombok.NonNull
    private double[] btheta;
    private String name;

    public int getP(){
        return phi.length;
    }
    
    public int getBp(){
        return bphi.length;
    }
    
    public int getQ(){
        return theta.length;
    }

    public int getBq(){
        return btheta.length;
    }

    public double phi(int i) {
        return phi[i-1];
    }

    public double bphi(int i) {
        return bphi[i-1];
    }

    public double theta(int i) {
        return theta[i-1];
    }

    public double btheta(int i) {
        return btheta[i-1];
    }

    /**
     *
     * @return
     */
    public SarimaSpecification specification() {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.setD(d);
        spec.setBd(bd);
        spec.setP(phi.length);
        spec.setBp(bphi.length);
        spec.setQ(theta.length);
        spec.setBq(btheta.length);
        return spec;
    }

    static final double[] E = new double[0];

    public static Builder builder() {

        Builder builder = new Builder();
        builder.period = 1;
        builder.phi = E;
        builder.theta = E;
        builder.bphi = E;
        builder.btheta = E;
        return builder;
    }

    @Override
    public double getInnovationVariance() {
        return 1;
    }

    @Override
    public RealPolynomial getAr() {
        RealPolynomial rslt = RealPolynomial.of(1, phi);
        if (bphi.length>0){
            double[] p=new double[bphi.length*period+1];
            p[0]=1;
            for (int i=0, j=period; i<bphi.length; ++i, j+=period)
                p[j]=bphi[i];
            rslt=rslt.times(RealPolynomial.ofInternal(p));
        }
        return rslt;
    }

    @Override
    public RealPolynomial getDelta() {
        return D(d).times(BD(period, bd));
    }

    @Override
    public RealPolynomial getMa() {
        RealPolynomial rslt = RealPolynomial.of(1, theta);
        if (btheta.length>0){
            double[] p=new double[btheta.length*period+1];
            p[0]=1;
            for (int i=0, j=period; i<btheta.length; ++i, j+=period)
                p[j]=btheta[i];
            rslt=rslt.times(RealPolynomial.ofInternal(p));
        }
        return rslt;
    }

    private static final RealPolynomial D1 = RealPolynomial.ofInternal(new double[]{1, -1});
    private static final RealPolynomial D4 = RealPolynomial.ofInternal(new double[]{1, 0, 0, 0, -1});
    private static final RealPolynomial D12 = RealPolynomial.ofInternal(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1});

    private static RealPolynomial D(int n) {
        RealPolynomial rslt = RealPolynomial.ONE;
        for (int i = 0; i < n; ++i) {
            rslt = rslt.times(D1);
        }
        return rslt;
    }

    private static RealPolynomial BD(int period, int n) {
        RealPolynomial rslt = RealPolynomial.ONE;
        if (n > 0) {
            RealPolynomial Q;
            switch (period) {
                case 4:
                    Q = D4;
                    break;
                case 12:
                    Q = D12;
                    break;
                default:
                    double[] q = new double[period + 1];
                    q[0] = 1;
                    q[period] = -1;
                    Q = RealPolynomial.ofInternal(q);
            }
            rslt = Q;
            for (int i = 1; i < n; ++i) {
                rslt = rslt.times(Q);
            }
        }
        return rslt;
    }

}
