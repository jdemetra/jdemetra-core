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

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.timeseries.TimeSelector;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class BasicSpec implements Validatable<BasicSpec> {

    private static final BasicSpec DEFAULT = BasicSpec.builder().build();

    @lombok.NonNull
    private TimeSelector span;
    private boolean preProcessing;
    private boolean preliminaryCheck;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .span(TimeSelector.all())
                .preProcessing(true)
                .preliminaryCheck(true);
    }

    @Override
    public BasicSpec validate() throws IllegalArgumentException {
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<BasicSpec> {
    }
}
