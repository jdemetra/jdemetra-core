/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.arima;

import demetra.data.DoubleSeq;
import nbbrd.design.Development;

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
public class SarimaModel implements ISarimaModel {
    
    /**
     * Period of the seasonal model
     */
    int period;
    /**
     * Regular differencing order
     */
    int d;
    /**
     * Seasonal differencing order
     */
    int bd;
    /**
     * Regular auto-regressive parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    DoubleSeq phi;
    /**
     * Regular moving average parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    DoubleSeq theta;
    /**
     * Seasonal auto-regressive parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    private DoubleSeq bphi;
    /**
     * Seasonal moving average parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    private DoubleSeq btheta;

    /**
     * Name of the model. Optional (null by default)
     */
    private String name;

    @Override
    public int getP() {
        return phi.length();
    }

    @Override
    public int getBp() {
        return bphi.length();
    }

    @Override
    public int getQ() {
        return theta.length();
    }

    @Override
    public int getBq() {
        return btheta.length();
    }

    /**
     * Gets the underlying specification
     *
     * @return
     */
    @Override
    public SarimaOrders orders() {
        SarimaOrders spec = new SarimaOrders(period);
        spec.setD(d);
        spec.setBd(bd);
        spec.setP(phi.length());
        spec.setBp(bphi.length());
        spec.setQ(theta.length());
        spec.setBq(btheta.length());
        return spec;
    }

    @Override
    public DoubleSeq parameters() {
        int n = phi.length() + bphi.length() + theta.length() + btheta.length();
        int pos = 0;
        double[] all = new double[n];
        phi.copyTo(all, pos);
        pos += phi.length();
        bphi.copyTo(all, pos);
        pos += bphi.length();
        theta.copyTo(all, pos);
        pos += theta.length();
        btheta.copyTo(all, pos);
        return DoubleSeq.of(all);
    }

    public static Builder builder() {
        return new Builder()
                .period(1)
                .phi(DoubleSeq.empty())
                .bphi(DoubleSeq.empty())
                .theta(DoubleSeq.empty())
                .btheta(DoubleSeq.empty());
    }
}
