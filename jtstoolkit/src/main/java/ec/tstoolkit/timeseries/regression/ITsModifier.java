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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ITsModifier extends ITsVariable {

    /**
     * Gets the internal variable
     *
     * @return
     */
    ITsVariable getVariable();

    /**
     * Sets the variable that should be modified.
     * Not supported by default
     *
     * @param var The variable
     * @since 2.2.0
     */
    default void setVariable(ITsVariable var){}

    /**
     * Searches the root of this modifier(s)
     *
     * @return
     * @since 2.2.0
     */
    default ITsVariable getRoot() {
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
    default boolean dependsOn(ITsVariable var) {
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
