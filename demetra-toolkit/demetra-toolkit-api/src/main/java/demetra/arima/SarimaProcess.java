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
import demetra.maths.PolynomialType;

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
@lombok.Builder(toBuilder=true, builderClassName="Builder")
public class SarimaProcess  {

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
    private PolynomialType phi;
    @lombok.NonNull 
    private PolynomialType theta;
    @lombok.NonNull 
    private PolynomialType bphi;
    @lombok.NonNull 
    private PolynomialType btheta;
    
    /**
     *
     * @return
     */
    public SarimaSpecification specification() {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.setD(d);
        spec.setBd(bd);
        spec.setP(phi.degree());
        spec.setBp(bphi.degree());
        spec.setQ(theta.degree());
        spec.setBq(btheta.degree());
        return spec;
    }

    public static Builder builder(){
        Builder builder=new Builder();
        builder.period=1;
        builder.phi=PolynomialType.ONE;
        builder.theta=PolynomialType.ONE;
        builder.bphi=PolynomialType.ONE;
        builder.btheta=PolynomialType.ONE;
        return builder;
    }
}
