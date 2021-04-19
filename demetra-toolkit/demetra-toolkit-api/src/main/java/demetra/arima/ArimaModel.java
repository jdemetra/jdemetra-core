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
 * Generic ARIMA model (defined by its stationary AR, non-stationary AR, 
 * MA polynomials and innovation variance).
 * 
 * ar(B)delta(b)y(t)=ma(b)e(t), 
 * where B is the backshift operator and e(t)~N(0,innovationVariance) 
 *  
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Builder
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ArimaModel {

    /**
     * Innovation variance. 1 by default
     */
    private double innovationVariance;
    @lombok.NonNull
    /**
     * Stationary auto-regressive polynomial (1+ar[0]B...); True signs. 
     * Doesn't contain the constant term (always 1)
     * All the roots of the stationary polynomial should be outside the unit circle (not checked)
     */
    private double[] ar;
    @lombok.NonNull
    /**
     * Non-stationary auto-regressive polynomial (1, delta(1)...); True signs. 
     * Doesn't contain the constant term (always 1)
     * All the roots of the non-stationary polynomial should be on the unit circle (not checked)
     */
    private double[] delta;
    /**
     * Moving-average polynomial (1, theta(1)...); True signs.
     * Doesn't contain the constant term (always 1)
     */
    @lombok.NonNull
    private double[] ma;
    /**
     * Name of the model (optional); null by default
     */
    private String name;

    /**
     * Rename the model. 
     * @param nname
     * @return 
     */
    public ArimaModel rename(String nname) {
        return new ArimaModel(innovationVariance, ar, delta, ma, nname);
    }

    public static Builder builder() {
        Builder builder = new Builder();
        builder.innovationVariance = 1;
        builder.ar = DoubleSeq.EMPTYARRAY;
        builder.delta = DoubleSeq.EMPTYARRAY;
        builder.ma = DoubleSeq.EMPTYARRAY;
        return builder;
    }
}
