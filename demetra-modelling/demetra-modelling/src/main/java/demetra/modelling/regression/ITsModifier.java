/*
 * Copyright 2017 National Bank of Belgium
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

package demetra.modelling.regression;

import demetra.design.Development;
import demetra.timeseries.TimeSeriesDomain;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
@Development(status = Development.Status.Alpha)
public interface ITsModifier<D extends TimeSeriesDomain<?>> extends ITsVariable<D> {

    /**
     * Gets the internal variable
     *
     * @return
     */
    ITsVariable<D> getVariable();

    /**
     * Sets the variable that should be modified.
     * Not supported by default
     *
     * @param var The variable
     * @since 2.2.0
     */
    default void setVariable(ITsVariable<D> var){}

    /**
     * Searches the root of this modifier(s)
     *
     * @return
     * @since 2.2.0
     */
    default ITsVariable<D> getRoot() {
        ITsVariable current = getVariable();
        while (current != null && current instanceof ITsModifier) {
            ITsModifier modifier = (ITsModifier) current;
            current = modifier.getVariable();
        }
        return current;
    }

    /**
     * Verifies that the given variable is in the chain of this modifier.
     * To avoid cycles
     *
     * @param var
     * @return
     * @since 2.2.0
     */
    default boolean dependsOn(ITsVariable<D> var) {
        if (var == this) {
            return true;
        }
        ITsVariable current = getVariable();
        while (current != null && current instanceof ITsModifier) {
            if (var == current) {
                return true;
            }
            ITsModifier modifier = (ITsModifier) current;
            current = modifier.getVariable();
        }
        return current == var;
    }
}
