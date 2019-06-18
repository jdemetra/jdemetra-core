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
package demetra.modelling.regression;

import demetra.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class Variable {

    private final String name;
    private final ITsVariable variable;
    private final boolean prespecified;

    /**
     *
     * @param variable Actual variable
     * @param name
     * @param prespecified
     */
    public Variable(@NonNull final ITsVariable variable, @NonNull final String name, final boolean prespecified) {
        this.variable = variable;
        this.name = name;
        this.prespecified = prespecified;
    }

    public ITsVariable getVariable() {
        return variable;
    }

    public String getName() {
        return name;
    }

    public boolean isPrespecified() {
        return prespecified;
    }

    public Variable rename(String name) {
        if (name.equals(this.name)) {
            return this;
        } else {
            return new Variable(variable, name, prespecified);
        }
    }

    // main types
    public boolean isUser() {
        return variable instanceof IUserTsVariable;
    }

    public boolean isOutlier(boolean prespecified) {
        return variable instanceof IOutlier
                && this.prespecified == prespecified;
    }

    public boolean isOutlier() {
        return variable instanceof IOutlier;
    }

    public boolean isCalendar() {
        return variable instanceof ICalendarVariable;
    }

    public boolean isTradingDays() {
        return variable instanceof ITradingDaysVariable;
    }

    public boolean isLengthOfPeriod() {
        return variable instanceof ILengthOfPeriodVariable;
    }

    public boolean isMovingHolidays() {
        return variable instanceof IMovingHolidayVariable;
    }

    public boolean isEaster() {
        return variable instanceof IEasterVariable;
    }

}
