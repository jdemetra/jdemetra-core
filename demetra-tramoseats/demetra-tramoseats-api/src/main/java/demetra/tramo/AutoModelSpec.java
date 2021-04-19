/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.tramo;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class AutoModelSpec implements Validatable<AutoModelSpec> {

    public static final AutoModelSpec DEFAULT_DISABLED = AutoModelSpec.builder().build();
    public static final AutoModelSpec DEFAULT_ENABLED = AutoModelSpec.builder().enabled(true).build();

    public static final double DEF_CANCEL = .05, DEF_PCR = .95, DEF_UB1 = .97, DEF_UB2 = .91, DEF_TSIG = 1, DEF_PC = .12;
    public static final boolean DEF_FAL = false, DEF_AMICOMPARE = false;

    private double cancel, ub1, ub2, pcr, pc, tsig;
    private boolean enabled, acceptDefault, amiCompare;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .cancel(DEF_CANCEL)
                .ub1(DEF_UB1)
                .ub2(DEF_UB2)
                .pcr(DEF_PCR)
                .pc(DEF_PC)
                .tsig(DEF_TSIG)
                .enabled(false)
                .acceptDefault(DEF_FAL)
                .amiCompare(DEF_AMICOMPARE);
    }

    @Override
    public AutoModelSpec validate() throws IllegalArgumentException {
        if (pcr < .8 || pcr > 1) {
            throw new IllegalArgumentException("PCR should belong to [0.8, 1.0]");
        }
        if (ub1 < .8 || ub1 > 1) {
            throw new IllegalArgumentException("UB1 should belong to [0.8, 1.0]");
        }
        if (ub2 < .8 || ub2 > 1) {
            throw new IllegalArgumentException("UB2 should belong to [0.8, 1.0]");
        }
        if (cancel < 0 || cancel > .3) {
            throw new IllegalArgumentException("Cancelation limit should belong to [0, 0.3]");
        }
        if (tsig <= .5) {
            throw new IllegalArgumentException("TSIG should be higher than 0.5");
        }
        if (pc < .1 || pc > 0.3) {
            throw new IllegalArgumentException("PC should belong to [0.1, 0.3]");
        }
        return this;
    }

    /**
     * The default is enabled, with default parameters
     * @return 
     */
    public boolean isDefault() {
        return this.equals(DEFAULT_ENABLED);
    }

    public static class Builder implements Validatable.Builder<AutoModelSpec> {
    }
}
