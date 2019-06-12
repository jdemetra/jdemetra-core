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
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Builder(builderClassName = "Builder")
@lombok.Value
public class ArimaModel implements ArimaType{

//    @lombok.Builder.Default
//    private double innovationVariance = 1;
//    @lombok.NonNull
//    @lombok.Builder.Default
//    private PolynomialType ar = PolynomialType.ONE;
//    @lombok.NonNull
//    @lombok.Builder.Default
//    private PolynomialType delta = PolynomialType.ONE;
//    @lombok.NonNull
//    @lombok.Builder.Default
//    private PolynomialType ma = PolynomialType.ONE;
//    private String name;
    private double innovationVariance;
    @lombok.NonNull
    private RealPolynomial ar;
    @lombok.NonNull
    private RealPolynomial delta;
    @lombok.NonNull
    private RealPolynomial ma;
    private String name;
    
    public static ArimaModel of(ArimaType arima){
        return new ArimaModel(arima.getInnovationVariance(), arima.getAr(),
                arima.getDelta(), arima.getMa(), "");
    }
    
    public ArimaModel stationary() {
        if (delta == RealPolynomial.ONE) {
            return this;
        } else {
            String stname = name == null ? null : name + " (stationary)";
            return new ArimaModel(innovationVariance, ar, RealPolynomial.ONE, ma, stname);
        }
    }

    public ArimaModel rename(String nname) {
        return new ArimaModel(innovationVariance, ar, RealPolynomial.ONE, ma, nname);
    }

    public static Builder builder() {
        Builder builder = new Builder();
        builder.innovationVariance = 1;
        builder.ar = RealPolynomial.ONE;
        builder.delta = RealPolynomial.ONE;
        builder.ma = RealPolynomial.ONE;
        return builder;
    }
}
