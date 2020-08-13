/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.regression;

import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Variable {

    private String name;
    private ITsVariable variable;
    private boolean prespecified;
    private Parameter[] coefficients;
    
    public int dim(){
        return variable.dim();
    }
    
    public Parameter getCoefficient(int i){
        return coefficients == null ? Parameter.undefined() : coefficients[i];
    }

    /**
     *
     * @param variable Actual variable
     * @param name
     * @return
     */
    public static Variable variable(@NonNull final String name, @NonNull final ITsVariable variable) {
        return new Variable(name, variable, false, null);
    }

    /**
     *
     * @param variable Actual variable
     * @param name
     * @return
     */
    public static Variable prespecifiedVariable(@NonNull final String name, @NonNull final ITsVariable variable) {
        return new Variable(name, variable, true, null);
    }

    /**
     *
     * @param variable Actual variable
     * @param name
     * @param coeff
     * @return
     */
    public static Variable preadjustmentVariable(@NonNull final String name, @NonNull final ITsVariable variable, double coeff) {
        if (variable.dim() != 1) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, variable, true, new Parameter[]{Parameter.fixed(coeff)});
    }
    
   /**
     *
     * @param variable Actual variable
     * @param name
     * @param coeff
     * @return
     */
    public static Variable preadjustmentVariable(@NonNull final String name, @NonNull final ITsVariable variable, @NonNull double[] coeff) {
        if (variable.dim() != coeff.length) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, variable, true, Parameter.of(coeff, ParameterType.Fixed));
    }

    public static Variable of(@NonNull final String name, @NonNull final ITsVariable variable, @NonNull Parameter[] coeff) {
        if (variable.dim() != coeff.length) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, variable, true, coeff);
    }

    public Variable rename(String name) {
        if (name.equals(this.name)) {
            return this;
        } else {
            return new Variable(name, variable, prespecified, coefficients);
        }
    }

    public Variable withCoefficient(Parameter coefficient) {
        if (variable.dim() != 1) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, variable, prespecified, new Parameter[]{coefficient});
    }

    public Variable withCoefficient(Parameter[] coefficients) {
        if (coefficients != null && variable.dim() != coefficients.length) {
            throw new IllegalArgumentException();
        }
        return new Variable(name, variable, prespecified, coefficients);
    }
    
    public int freeCoefficientsCount(){
        if (coefficients == null)
            return variable.dim();
        else{
            return Parameter.freeParametersCount(coefficients);
        }
    }

    // main types
    
    /**
     * 
     * @return True if all coefficients are fixed, false otherwise
     */
    public boolean isPreadjustment(){
        return coefficients != null && !Parameter.hasFreeParameters(coefficients);
    }
    
    /**
     * 
     * @return True if all coefficients are free, false otherwise
     */
    public boolean isFree(){
        return coefficients == null || Parameter.isFree(coefficients);
    }
    
    public boolean isUser() {
        return variable instanceof IUserTsVariable;
    }

    /**
     * Detected outliers  (prespecified=false) or preadjusted/prespecified outliers
     * @param prespecified
     * @return 
     */
    public boolean isOutlier(boolean prespecified) {
        return variable instanceof IOutlier
                && (this.prespecified == prespecified || this.isPreadjustment());
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
