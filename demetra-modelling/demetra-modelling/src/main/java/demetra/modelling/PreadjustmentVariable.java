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

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.Parameter;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.modelling.regression.ICalendarVariable;
import demetra.modelling.regression.IEasterVariable;
import demetra.modelling.regression.ILengthOfPeriodVariable;
import demetra.modelling.regression.IMovingHolidayVariable;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.ITradingDaysVariable;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.IUserTsVariable;
import demetra.timeseries.TsDomain;
import java.util.Collections;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class PreadjustmentVariable {

    private final ITsVariable<TsDomain> variable;
    private final double[] coefficients;

    /**
     *
     * @param variable Actual variable
     * @param coefficients Fixed coefficients
     */
    public PreadjustmentVariable(final ITsVariable<TsDomain> variable, final double[] coefficients) {
        this.variable = variable;
        this.coefficients = coefficients;
    }

    public ITsVariable<TsDomain> getVariable() {
        return variable;
    }

    /**
     * @return the coefficients
     */
    public DoubleSequence getCoefficients() {
        return DoubleSequence.ofInternal(coefficients);
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

    public void addTo(DataBlock buffer, TsDomain domain) {
        add(buffer, domain, 1);
    }

    public void removeFrom(DataBlock buffer, TsDomain domain) {
        add(buffer, domain, -1);
    }

    private void add(DataBlock buffer, TsDomain domain, double c) {
        int dim = variable.getDim();
        int n = domain.length();
        if (dim == 1) {
            DataBlock x = DataBlock.make(n);
            variable.data(domain, Collections.singletonList(x));
            buffer.addAY(c * coefficients[0], x);
        } else {
            Matrix m = Matrix.make(n, dim);
            variable.data(domain, m.columnList());
            for (int i = 0; i < dim; ++i) {
                buffer.addAY(c * coefficients[i], m.column(i));
            }
        }
    }

    public PreadjustmentVariable rename(String name) {
        if (name.equals(variable.getName())) {
            return this;
        } else {
            return new PreadjustmentVariable(variable.rename(name), coefficients);
        }
    }

}
