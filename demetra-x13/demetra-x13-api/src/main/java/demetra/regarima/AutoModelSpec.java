/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.regarima;

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class AutoModelSpec implements Validatable<AutoModelSpec> {

    private static final AutoModelSpec DEFAULT = AutoModelSpec.builder().build();

    public static final double DEF_LJUNGBOX = .95, DEF_TSIG = 1, DEF_PREDCV = .14286, DEF_UBFINAL = 1.05, DEF_UB1 = 1 / .96, DEF_UB2 = .88,
            DEF_CANCEL = 0.1, DEF_FCT = 1 / .9875;
    public static final boolean DEF_ACCEPTDEF = false, DEF_CHECKMU = true, DEF_MIXED = true,
            DEF_BALANCED = false, DEF_HR = false;

    private boolean enabled;
    private OrderSpec diff, order;
    private boolean acceptDefault, checkMu, mixed, balanced, hannanRissannen;
    private double cancel, percentRSE, ljungBoxLimit, predcv, ub1, ub2, ubfinal;

    /**
     * Limit of Arma T-value (tsig)
     */
    private double armaSignificance;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .enabled(false)
                .acceptDefault(DEF_ACCEPTDEF)
                .checkMu(DEF_CHECKMU)
                .mixed(DEF_MIXED)
                .balanced(DEF_BALANCED)
                .hannanRissannen(DEF_HR)
                .cancel(DEF_CANCEL)
                .percentRSE(DEF_FCT)
                .ljungBoxLimit(DEF_LJUNGBOX)
                .predcv(DEF_PREDCV)
                .armaSignificance(DEF_TSIG)
                .ub1(DEF_UB1)
                .ub2(DEF_UB2)
                .ubfinal(DEF_UBFINAL);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public AutoModelSpec validate() throws IllegalArgumentException {
        if (armaSignificance < 0.5) {
            throw new IllegalArgumentException("Arma T-value limit must be greater than .5");
        }

        if (percentRSE < 1) {
            throw new IllegalArgumentException("Must be greater than .5");
        }

        if (predcv < 0.05 || predcv > .3) {
            throw new IllegalArgumentException("Percent reduction of critical value must be in [0.05, .3]");
        }

        if (ub1 <= 1) {
            throw new IllegalArgumentException("Initial unit root limit must be greater than 1");
        }

        if (ub2 >= 1) {
            throw new IllegalArgumentException("Final unit root limit must be less than 1");
        }
        
        if (cancel < 0 || cancel > .2) {
            throw new IllegalArgumentException("Cancelation limit must be in [0, .2]");
        }
        
        if (ubfinal < 1) {
            throw new IllegalArgumentException("Unit root limit must be greater than 1");
        }
        return this;
    }

    public static class Builder implements Validatable.Builder<AutoModelSpec> {
    }
}
