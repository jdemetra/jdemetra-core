/*
 * Copyright 2020 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data;

import nbbrd.design.Development;

/**
 * // TODO Rename to Parameter (which should disappear)
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Development(status = Development.Status.Preliminary)
public class Parameter {

    /**
     * Value of the parameter
     */
    private double value;
    /**
     * Type of the parameter. 
     */
    private ParameterType type;

    public boolean isFixed() {
        return type == ParameterType.Fixed;
    }

    public boolean isEstimated() {
        return type == ParameterType.Estimated;
    }

//    public boolean isDerived() {
//        return type == ParameterType.Derived;
//    }
//
    /**
     * Free parameters are either undefined or initial parameters.
     * They should be estimated
     *
     * @return
     */
    public boolean isFree() {
        return type != ParameterType.Fixed;
    }

    /**
     * Check the some parameters are free
     *
     * @param spec
     * @return Similar to freeParametersCount(spec) = 0 (but faster)
     */
    public static boolean hasFreeParameters(Parameter[] spec) {
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isFree()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the some parameters have the specified type
     *
     * @param spec
     * @param type
     * @return
     */
    public static boolean hasParameters(Parameter[] spec, ParameterType type) {
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].type == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the number of free parameters
     *
     * @param spec
     * @return
     */
    public static int freeParametersCount(Parameter[] spec) {
        int n = 0;
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isFree()) {
                ++n;
            }
        }
        return n;
    }

    public static int fixedParametersCount(Parameter[] spec) {
        int n = 0;
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isFixed()) {
                ++n;
            }
        }
        return n;
    }

    /**
     * All the estimated parameters are set to undefined
     *
     * @param spec
     */
    public static void resetParameters(Parameter[] spec) {
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isEstimated()) {
                spec[i] = UNDEFINED;
            }
        }
    }

    /**
     * All the estimated parameters are set to initial (keeping the current
     * value)
     *
     * @param spec
     */
    public static void freeParameters(Parameter[] spec) {
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isEstimated()) {
                spec[i] = new Parameter(spec[i].value, ParameterType.Initial);
            }
        }
    }
    
    public static Parameter of(double val, ParameterType t){
        if (t == ParameterType.Undefined)
            return UNDEFINED;
        else
            return new Parameter(val, t);
    }

    public static Parameter undefined() {
        return UNDEFINED;
    }

    public static Parameter fixed(double value) {
        return new Parameter(value, ParameterType.Fixed);
    }

    public static Parameter initial(double value) {
        return new Parameter(value, ParameterType.Initial);
    }

    public static Parameter estimated(double value) {
        return new Parameter(value, ParameterType.Estimated);
    }

    public static Parameter[] of(double values[], ParameterType type) {
        if (type == ParameterType.Undefined)
            return make(values.length);
        Parameter[] p = new Parameter[values.length];
        for (int i = 0; i < p.length; ++i) {
            p[i] = new Parameter(values[i], type);
        }
        return p;
    }

    public static Parameter[] make(int n) {
        Parameter[] all = new Parameter[n];
        for (int i = 0; i < n; ++i) {
            all[i] = UNDEFINED;
        }
        return all;
    }

    /**
     * Checks that all the parameters are free. Derived parameters are not
     * considered
     *
     * @param spec
     * @return
     */
    public static boolean isFree(Parameter[] spec) {
        for (int i = 0; i < spec.length; ++i) {
            if (!spec[i].isFree()) {
//            if (!spec[i].isFree() && !spec[i].isDerived()) {
                return false;
            }
        }
        return true;
    }

    public static double[] values(Parameter[] spec) {
        if (spec.length == 0) {
            return Doubles.EMPTYARRAY;
        } else {
            double[] val = new double[spec.length];
            for (int i = 0; i < val.length; ++i) {
                val[i] = spec[i].value;
            }
            return val;
        }
    }

    /**
     * Checks that all the parameters in an array are uninitialized.
     * Opposite of isDefined except for empty or null arrays.
     * @param p The array of parameters. May be null;
     * @return True if all parameters are undefined (or null)
     */
    public static boolean isDefault(Parameter[] p) {
        if (p == null) {
            return true;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i]!=UNDEFINED) {
                return false;
            }
        }
        return true;
    }
    /*
     * Checks that all the parameters in an array are defined. Opposite of isDefault
     * except for empty (or null) arrays.
     * @param p The array of parameters. May be null;
     * @return True if all parameters are defined (or null)
     */
    public static boolean isDefined(Parameter[] p) {
        if (p == null) {
            return true;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i]== UNDEFINED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that a parameter is defined. A parameter is defined if it is non
     * null and if its type is not undefined
     *
     * @param p
     * @return
     */
    public static boolean isDefined(Parameter p) {
        return p != UNDEFINED;
    }

    // / <summary></summary>
    // / <returns>
    // / False if the array contains at least 1 non null parameter, whose value
    // is 0. True
    // / in all the other cases.
    // / </returns>
    // / <param name="p">The array of parameters</param>
    /**
     * Checks that an array of parameters contains only zero values.
     *
     * @param p The considered array of parameters.
     * @return True if some defined parameters are different from 0.
     * An empty array is considered as a 0-array (returns true).
     */
    public static boolean isZero(Parameter[] p) {
        if (p == null) {
            return true;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i] != null && p[i].value != 0) {
                return false;
            }
        }
        return true;
    }

    public Parameter withType(ParameterType type) {
        if (type == ParameterType.Undefined) {
            return UNDEFINED;
        } else {
            return new Parameter(value, type);
        }
    }

    private static final Parameter UNDEFINED = new Parameter(0, ParameterType.Undefined);
}
