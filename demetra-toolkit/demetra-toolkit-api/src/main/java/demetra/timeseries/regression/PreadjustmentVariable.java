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
package demetra.timeseries.regression;

import demetra.design.Development;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class PreadjustmentVariable {

    private final ITsVariable variable;
    private final String name;
    private final double[] coefficients;

    /**
     *
     * @param variable Actual variable
     * @param name
     * @param coefficients Fixed coefficients
     */
    public PreadjustmentVariable(final ITsVariable variable, final String name, final double[] coefficients) {
        this.variable = variable;
        this.name=name;
        this.coefficients = coefficients;
    }

    public ITsVariable getVariable() {
        return variable;
    }
    
    public String getName(){
        return name;
    }

    /**
     * @return the coefficients
     */
    public DoubleSeq getCoefficients() {
        return DoubleSeq.of(coefficients);
    }

    // main types
    public boolean isUser() {
        return variable instanceof IUserTsVariable;
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

    public PreadjustmentVariable rename(String newName) {
        if (newName.equals(this.name)) {
            return this;
        } else {
            return new PreadjustmentVariable(variable, newName, coefficients);
        }
    }
   
}
