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
package demetra.regarima.ami;

import demetra.data.DataBlock;
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
public final class Variable {

    private final ITsVariable<TsDomain> variable;
    private final boolean prespecified;
    private final Parameter[] coefficients;

    public static Variable fixedVariable(final @Nonnull ITsVariable<TsDomain> variable, final @Nonnull Parameter[] coefficients) {
        return new Variable(variable, true, coefficients);
    }

    public static Variable prespecifiedVariable(final @Nonnull ITsVariable<TsDomain> variable) {
        return new Variable(variable, true, null);
    }

    public static Variable newVariable(final @Nonnull ITsVariable<TsDomain> variable) {
        return new Variable(variable, false, null);
    }

    private Variable(final ITsVariable<TsDomain> variable, final boolean prespecified, final Parameter[] coefficients) {
        this.variable = variable;
        this.prespecified = prespecified;
        this.coefficients = coefficients;
    }

    public ITsVariable<TsDomain> getVariable() {
        return variable;
    }

    /**
     * @return the coefficients
     */
    public Parameter[] getCoefficients() {
        return coefficients;
    }

    public boolean isPrespecified() {
        return prespecified;
    }

    public boolean isFixed() {
        return coefficients != null && !Parameter.hasFreeParameters(coefficients);
    }

    // main types
    public boolean isUser() {
        return variable instanceof IUserTsVariable;
    }

    public boolean isOutlier(boolean prespecified) {
        return variable instanceof IOutlier && this.prespecified == prespecified;
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

    public void addEffect(TsDomain domain, DataBlock buffer) {
        add(domain, buffer, 1);
    }

    public void removeEffect(TsDomain domain, DataBlock buffer) {
        add(domain, buffer, -1);
    }

    private void add(TsDomain domain, DataBlock buffer, double c) {
        int dim = variable.getDim();
        int n = domain.length();
        if (dim == 1) {
            DataBlock x = DataBlock.make(n);
            variable.data(domain, Collections.singletonList(x));
            buffer.addAY(c * coefficients[0].getValue(), x);
        } else {
            Matrix m = Matrix.make(n, dim);
            variable.data(domain, m.columnList());
            for (int i = 0; i < dim; ++i) {
                buffer.addAY(c * coefficients[i].getValue(), m.column(i));
            }
        }
    }

}
