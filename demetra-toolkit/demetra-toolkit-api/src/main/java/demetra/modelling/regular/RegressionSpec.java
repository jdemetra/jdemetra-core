/*
 * Copyright 2023 National Bank of Belgium
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
package demetra.modelling.regular;

import demetra.data.Parameter;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import demetra.util.Validatable;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class RegressionSpec implements Validatable<RegressionSpec> {

    
    Parameter mean;
    boolean checkMu;
    CalendarSpec calendar;
    
    @lombok.Singular
    List< Variable<IOutlier> > outliers;
    @lombok.Singular
    List< Variable<Ramp> > ramps;
    @lombok.Singular
    List< Variable<InterventionVariable> > interventionVariables;
    @lombok.Singular
    List< Variable<TsContextVariable> > userDefinedVariables;

    public static final RegressionSpec DEFAULT = RegressionSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .calendar(CalendarSpec.builder().build());
    }

    public boolean isUsed() {
        return mean !=null || calendar.isUsed() || !outliers.isEmpty()
                || !ramps.isEmpty() || !interventionVariables.isEmpty() 
                || !userDefinedVariables.isEmpty();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public RegressionSpec validate() throws IllegalArgumentException {
        return this;
    }

    public static class Builder implements Validatable.Builder<RegressionSpec> {

    }
    
    public boolean isSpecified(){
        return ! (calendar.getEaster().isTest() 
                || calendar.getTradingDays().isAutomatic()
                || calendar.getTradingDays().isTest()
                || checkMu
                );
    }
    
    public boolean hasFixedCoefficients(){
        if (! isUsed())
            return false;
        return (mean != null && mean.isFixed()) || calendar.hasFixedCoefficients()
                || outliers.stream().anyMatch(var->! var.isFree())
                || ramps.stream().anyMatch(var->! var.isFree())
                || interventionVariables.stream().anyMatch(var->! var.isFree())
                || userDefinedVariables.stream().anyMatch(var->! var.isFree());
    }
}
