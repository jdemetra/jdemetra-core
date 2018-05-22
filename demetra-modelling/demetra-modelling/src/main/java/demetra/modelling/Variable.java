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
package demetra.modelling;

import demetra.design.Development;
import demetra.modelling.regression.ICalendarVariable;
import demetra.modelling.regression.IEasterVariable;
import demetra.modelling.regression.ILengthOfPeriodVariable;
import demetra.modelling.regression.IMovingHolidayVariable;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.ITradingDaysVariable;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.IUserTsVariable;
import demetra.timeseries.TsDomain;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class Variable {

    private final ITsVariable<TsDomain> variable;
    private final boolean prespecified;

    /**
     *
     * @param variable Actual variable
     * @param prespecified
     */
    public Variable(final ITsVariable<TsDomain> variable, final boolean prespecified) {
        this.variable = variable;
        this.prespecified = prespecified;
    }

    public ITsVariable<TsDomain> getVariable() {
        return variable;
    }

    public boolean isPrespecified() {
        return prespecified;
    }

    public Variable rename(String name) {
        if (name.equals(variable.getName())) {
            return this;
        } else {
            return new Variable(variable.rename(name), prespecified);
        }
    }

    // main types
    public boolean isUser() {
        return variable instanceof IUserTsVariable;
    }

    public boolean isOutlier(boolean prespecified) {
        return variable instanceof IOutlier && 
                this.prespecified == prespecified;
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
