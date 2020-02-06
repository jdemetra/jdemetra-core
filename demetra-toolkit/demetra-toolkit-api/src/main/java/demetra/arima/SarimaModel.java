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

/**
 * Box-Jenkins seasonal arima model AR(B)* SAR(B)*D(B)*SD(B) y(t) =
 * MA(B)*SMA(B)e(t), e~N(0, var) AR(B) = 1+a(1)B+...+a(p)B^p, regular
 * auto-regressive polynomial SAR(B) = 1+b(1)B^s+...+b(bp)B^s*bp, seasonal
 * auto-regressive polynomial D(B) = 1+e(1)B+...+e(d)B^d, regular differencing
 * polynomial SD(B) = 1+f(1)B^s+...+f(bd)B^s*bd, seasonal differencing
 * polynomial MA(B) = 1+c(1)B+...+c(q)B^q, regular moving average polynomial
 * SMA(B) = 1+d(1)B^s+...+d(bq)B^s*bq, seasonal moving average polynomial
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SarimaModel {

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
    /**
     * Period of the seasonal model
     */
    private int period;
    /**
     * Regular differencing order
     */
    private int d;
    /**
     * Seasonal differencing order
     */
    private int bd;
    @lombok.NonNull
    /**
     * Regular auto-regressive parameters (true sign, 1 excluded)
     */
    private double[] phi;
    @lombok.NonNull
    /**
     * Regular moving average parameters (true sign, 1 excluded)
     */
    private double[] theta;
    @lombok.NonNull
    /**
     * Seasonal auto-regressive parameters (true sign, 1 excluded)
     */
    private double[] bphi;
    @lombok.NonNull
    /**
     * Seasonal moving average parameters (true sign, 1 excluded)
     */
    private double[] btheta;
    /**
     * Name of the model. Optional (null ny default)
     */
    private String name;

    public int getP() {
        return phi.length;
    }

    public int getBp() {
        return bphi.length;
    }

    public int getQ() {
        return theta.length;
    }

    public int getBq() {
        return btheta.length;
    }

    /**
     * Gets the underlying specification
     * @return
     */
    public SarimaOrders specification() {
        SarimaOrders spec = new SarimaOrders(period);
        spec.setD(d);
        spec.setBd(bd);
        spec.setP(phi.length);
        spec.setBp(bphi.length);
        spec.setQ(theta.length);
        spec.setBq(btheta.length);
        return spec;
    }

    /**
     * Gets all the parameters in the following order
     * phi, bphi, theta, btheta
     * 
     * @param truesign If truesign, the sign of the coefficients corresponds to the
     * sign in the backshift polynomials. Otherwise, the signs of phi, bphi are changed; 
     * it is similar to the X13 or R convention (for instance).
     * @return 
     */
    public double[] parameters(boolean truesign) {
        int n = phi.length + bphi.length + theta.length + btheta.length;
        int pos = 0;
        double[] all = new double[n];
        if (truesign) {
            for (int i = 0; i < phi.length; ++i) {
                all[pos++] = phi[i];
            }
            for (int i = 0; i < bphi.length; ++i) {
                all[pos++] = bphi[i];
            }
        } else {
            for (int i = 0; i < phi.length; ++i) {
                all[pos++] = -phi[i];
            }
            for (int i = 0; i < bphi.length; ++i) {
                all[pos++] = -bphi[i];
            }
        }
        for (int i = 0; i < theta.length; ++i) {
            all[pos++] = -theta[i];
        }
        for (int i = 0; i < btheta.length; ++i) {
            all[pos++] = -btheta[i];
        }
        return all;
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
}
