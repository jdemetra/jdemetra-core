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
package ec.tstoolkit.modelling;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.regression.AbstractTsVariableBox;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.IUserTsVariable;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class PreadjustmentVariable {

    public static PreadjustmentVariable calendarVariable(ICalendarVariable s, double[] c) {
        return new PreadjustmentVariable(s, ComponentType.CalendarEffect, c);
    }

    public static PreadjustmentVariable tdVariable(ITsVariable s, double[] c) {
        return new PreadjustmentVariable(AbstractTsVariableBox.tradingDays(s), ComponentType.CalendarEffect, c);
    }

    public static PreadjustmentVariable lpVariable(ITsVariable s, double c) {
        return new PreadjustmentVariable(AbstractTsVariableBox.leapYear(s), ComponentType.CalendarEffect, new double[]{c});
    }

    public static PreadjustmentVariable movingHolidayVariable(IMovingHolidayVariable s, double[] c) {
        return new PreadjustmentVariable(s, ComponentType.CalendarEffect, c);
    }

    public static PreadjustmentVariable movingHolidayVariable(ITsVariable s, double[] c) {
        return new PreadjustmentVariable(AbstractTsVariableBox.movingHoliday(s), ComponentType.CalendarEffect, c);
    }

    public static PreadjustmentVariable userVariable(IUserTsVariable s, ComponentType cmp, double[] c) {
        return new PreadjustmentVariable(s, cmp, c);
    }

    public static PreadjustmentVariable userVariable(ITsVariable s, ComponentType cmp, double[] c) {
        UserVariable user = new UserVariable(s, cmp);
        return new PreadjustmentVariable(user, cmp, c);
    }

    public static PreadjustmentVariable outlier(IOutlierVariable o, double[] c) {
        return new PreadjustmentVariable(o, DeterministicComponent.getType(o), c);
    }
    
    public static PreadjustmentVariable fix(Variable var, double[] c){
        return new PreadjustmentVariable(var.getVariable(), var.type, c);
    }

    private PreadjustmentVariable(ITsVariable var, ComponentType cmp, double[] coeff) {
        variable = var;
        type = cmp;
        this.coefficients = coeff;
    }

    public ITsVariable getVariable() {
        return variable;
    }

    public ITsVariable getRootVariable() {
        return TsVariableList.getRoot(variable);
    }

    public <T extends ITsVariable> boolean isCompatible(Class<T> tclass) {
        return tclass.isAssignableFrom(TsVariableList.getRoot(variable).getClass());
    }

    public boolean isUser() {
        return variable instanceof IUserTsVariable;
    }

    public boolean isCalendar() {
        return variable instanceof ICalendarVariable;
    }

    public boolean isMovingHoliday() {
        return variable instanceof IMovingHolidayVariable;
    }

    public boolean isOutlier() {
        return variable instanceof IOutlierVariable;
    }

    public void addEffect(DataBlock cumulator, TsDomain domain) {
        Matrix m = RegressionUtilities.matrix(variable, domain);
        for (int i = 0; i < coefficients.length; ++i) {
            cumulator.addAY(coefficients[i], m.column(i));
        }
    }

    public static DataBlock regressionEffect(Stream<PreadjustmentVariable> regs, TsDomain domain, Predicate<PreadjustmentVariable> pred) {
        DataBlock z = new DataBlock(domain.getLength());
        regs.filter(reg -> pred.test(reg)).forEach(reg -> {
            Matrix m = RegressionUtilities.matrix(reg.getVariable(), domain);
            for (int i = 0; i < reg.coefficients.length; ++i) {
                z.addAY(reg.coefficients[i], m.column(i));
            }
        });
        return z;
    }

    public static int countRegressors(Stream<PreadjustmentVariable> regs, Predicate<PreadjustmentVariable> pred) {
        return regs.filter(pred).mapToInt(var->var.getVariable().getDim()).sum();
    }

    public static int countVariables(Stream<PreadjustmentVariable> regs, Predicate<PreadjustmentVariable> pred) {
        return (int) regs.filter(pred).count();
    }

    public static DataBlock regressionEffect(Stream<PreadjustmentVariable> regs, TsDomain domain, ComponentType type) {
        return regressionEffect(regs, domain, reg -> reg.getType() == type);
    }

    public static DataBlock regressionEffect(Stream<PreadjustmentVariable> regs, TsDomain domain) {
        return regressionEffect(regs, domain, reg -> true);
    }

    public static <T extends ITsVariable> DataBlock regressionEffect(Stream<PreadjustmentVariable> regs, TsDomain domain, Class<T> tclass) {
        return regressionEffect(regs, domain, reg -> tclass.isInstance(reg.variable));
    }

    private final ITsVariable variable;
    private final ComponentType type;
    private final double[] coefficients;

    /**
     * @return the type
     */
    public ComponentType getType() {
        return type;
    }

    /**
     * @return the coefficients
     */
    public double[] getCoefficients() {
        return coefficients;
    }
}
