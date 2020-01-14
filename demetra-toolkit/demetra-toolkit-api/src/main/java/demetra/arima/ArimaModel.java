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
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Builder(builderClassName = "Builder")
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ArimaModel {

    private static double[] ONE = new double[]{1};

    /**
     * Innovation variance. 1 by default
     */
    private double innovationVariance;
    @lombok.NonNull
    /**
     * Stationary auto-regressive polynomial (1, phi(1)...); True signs. 1 by default
     */
    private double[] ar;
    @lombok.NonNull
    /**
     * Non-stationary auto-regressive polynomial (1, delta(1)...); True signs. 1 by default
     */
    private double[] delta;
    /**
     * Moving-average polynomial (1, theta(1)...); True signs. 1 by default
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
        builder.ar = ONE;
        builder.delta = ONE;
        builder.ma = ONE;
        return builder;
    }
}
