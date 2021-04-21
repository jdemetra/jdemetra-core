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
package demetra.regarima;

import demetra.timeseries.TimeSelector;
import demetra.util.Validatable;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class BasicSpec implements Validatable<BasicSpec> {

    public static final BasicSpec DEFAULT_ENABLED = BasicSpec.builder().build();
    public static final BasicSpec DEFAULT_DISABLED = BasicSpec.builder().preprocessing(false).build();

    public static final boolean DEF_PREPROCESSING = true, DEF_PRELIMINARYCHECK = true;

    @lombok.NonNull
    private TimeSelector span;
    private boolean preprocessing;
    private boolean preliminaryCheck;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .span(TimeSelector.all())
                .preprocessing(DEF_PREPROCESSING)
                .preliminaryCheck(DEF_PRELIMINARYCHECK);
    }

    @Override
    public BasicSpec validate() throws IllegalArgumentException {
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_ENABLED);
    }

    public static class Builder implements Validatable.Builder<BasicSpec> {
    }
}
