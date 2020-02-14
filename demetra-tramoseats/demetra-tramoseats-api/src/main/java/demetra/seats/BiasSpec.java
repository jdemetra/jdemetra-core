/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.seats;

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.util.Validatable;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public final class BiasSpec {

    public static enum BiasCorrection{
        None, Legacy
    }


    public static final double DEF_EPSPHI = 2, DEF_RMOD = .5, DEF_SMOD1 = .8, DEF_SMOD = .8, DEF_XL = .95;

    private BiasCorrection biasCorrection;
    private boolean logCorrection;

    public static final BiasSpec DEFAULT = BiasSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .logCorrection(false)
                .biasCorrection(BiasCorrection.Legacy);
                
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }
}
