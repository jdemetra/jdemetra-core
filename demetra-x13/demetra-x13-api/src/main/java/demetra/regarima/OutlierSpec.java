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
import demetra.timeseries.TimeSelector;
import demetra.util.Validatable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class OutlierSpec implements Validatable<OutlierSpec> {

    public static enum Method {
        AddOne,
        AddAll
    }

    public static final OutlierSpec DEFAULT_UNUSED = OutlierSpec.builder().build();

    public static final double DEF_TCRATE = .7;
    public static final int DEF_NMAX = 30;

    @lombok.Singular
    private List<SingleOutlierSpec> types;
    private int lsRun;
    private Method method;
    private double monthlyTCRate, defaultCriticalValue;
    @lombok.NonNull
    private TimeSelector span;
    private int maxIter;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .lsRun(0)
                .method(Method.AddOne)
                .monthlyTCRate(DEF_TCRATE)
                .defaultCriticalValue(0)
                .span(TimeSelector.all())
                .maxIter(DEF_NMAX);
    }

    @Override
    public OutlierSpec validate() throws IllegalArgumentException {
        return this;
    }

    public boolean isUsed() {
        return !types.isEmpty();
    }

    public int getTypesCount() {
        return types.size();
    }

    public SingleOutlierSpec search(String type) {
        for (SingleOutlierSpec s : types) {
            if (s.getType().equals(type)) {
                return s;
            }
        }
        return null;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_UNUSED);
    }

    public static class Builder implements Validatable.Builder<OutlierSpec> {

        /**
         * When the default critical value is changed, all the current outliers'
         * critical values are accordingly modified
         *
         * @param defaultCriticalValue New critical value
         * @return Builder with the new critical value applied
         */
        public Builder defaultCriticalValue(double defaultCriticalValue) {
            if (types == null) {
                types = new ArrayList<>();
            }

            for (int i = 0; i < types.size(); i++) {
                types.set(i, new SingleOutlierSpec(types.get(i).getType(), defaultCriticalValue));
            }

            this.defaultCriticalValue = defaultCriticalValue;
            return this;
        }
    }
}
