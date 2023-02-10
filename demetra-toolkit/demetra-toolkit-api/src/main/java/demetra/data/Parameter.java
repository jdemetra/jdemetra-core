/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.data;

import java.util.Formatter;
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

    public boolean isDefined() {
        return type != ParameterType.Undefined;
    }

    /**
     * Free parameters are either undefined or initial parameters. They should
     * be estimated
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
     * All the non fixed parameters are set to undefined
     *
     * @param spec
     * @return
     */
    public static Parameter[] resetParameters(Parameter[] spec) {
        if (spec == null || spec.length == 0) {
            return spec;
        }
        return Parameter.make(spec.length);
    }

    /**
     * All the parameters are set to undefined, except those that are fixed in
     * the reference (which are put in the current array)
     *
     * @param spec
     * @param ref
     * @return
     */
    public static Parameter[] resetParameters(Parameter[] spec, Parameter[] ref) {
        if (spec == null || spec.length == 0) {
            return spec;
        }
        Parameter[] nspec = spec.clone();
        for (int i = 0; i < spec.length; ++i) {
            if (ref == null || i >= ref.length || !ref[i].isFixed()) {
                nspec[i] = UNDEFINED;
            } else {
                nspec[i] = ref[i];
            }
        }
        return nspec;
    }

    /**
     * All the fixed parameters are relaxed (as initial values)
     *
     * @param spec
     * @return
     */
    public static Parameter[] freeParameters(Parameter[] spec) {
        if (spec == null || spec.length == 0) {
            return spec;
        }
        Parameter[] nspec = spec.clone();
        for (int i = 0; i < nspec.length; ++i) {
            nspec[i] = new Parameter(nspec[i].value, ParameterType.Initial);
        }
        return nspec;
    }

    /**
     * All the fixed parameters are relaxed (as initial values), except those
     * that are fixed in the reference (which are put in the current array)
     *
     * @param spec
     * @param ref
     * @return
     */
    public static Parameter[] freeParameters(Parameter[] spec, Parameter[] ref) {
        if (spec == null || spec.length == 0) {
            return spec;
        }
        Parameter[] nspec = spec.clone();
        for (int i = 0; i < nspec.length; ++i) {
            if (ref == null || i >= ref.length || !ref[i].isFixed()) {
                nspec[i] = new Parameter(spec[i].value, ParameterType.Initial);
            } else {
                nspec[i] = ref[i];
            }
        }
        return nspec;
    }

    /**
     * All the defined parameters are fixed (keeping the current value)
     *
     * @param spec
     * @return
     */
    public static Parameter[] fixParameters(Parameter[] spec) {
        if (spec == null || spec.length == 0) {
            return spec;
        }
        Parameter[] nspec = spec.clone();
        for (int i = 0; i < nspec.length; ++i) {
            if (nspec[i].isDefined() && !nspec[i].isFixed()) {
                nspec[i] = new Parameter(nspec[i].value, ParameterType.Fixed);
            }
        }
        return nspec;
    }

    public static Parameter of(double val, ParameterType t) {
        if (t == ParameterType.Undefined) {
            return UNDEFINED;
        } else {
            return new Parameter(val, t);
        }
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
        if (type == ParameterType.Undefined) {
            return make(values.length);
        }
        Parameter[] p = new Parameter[values.length];
        for (int i = 0; i < p.length; ++i) {
            p[i] = new Parameter(values[i], type);
        }
        return p;
    }

    public static Parameter[] of(DoubleSeq values, ParameterType type) {
        if (type == ParameterType.Undefined) {
            return make(values.length());
        }
        Parameter[] p = new Parameter[values.length()];
        DoubleSeqCursor cur = values.cursor();
        for (int i = 0; i < p.length; ++i) {
            p[i] = new Parameter(cur.getAndNext(), type);
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

    public static Parameter[] zero(int n) {
        Parameter[] all = new Parameter[n];
        for (int i = 0; i < n; ++i) {
            all[i] = ZERO;
        }
        return all;
    }

    public static Parameter zero() {
        return ZERO;
    }

    /**
     * Checks that all the parameters are free. Derived parameters are not
     * considered null is considered as free (no defined parameters)
     *
     * @param spec
     * @return
     */
    public static boolean isFree(Parameter[] spec) {
        if (spec == null) {
            return true;
        }
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

    public static double[] values(Parameter[] spec, double defvalue) {
        if (spec.length == 0) {
            return Doubles.EMPTYARRAY;
        } else {
            double[] val = new double[spec.length];
            for (int i = 0; i < val.length; ++i) {
                if (spec[i].getType() == ParameterType.Undefined) {
                    val[i] = defvalue;
                } else {
                    val[i] = spec[i].value;
                }
            }
            return val;
        }
    }

    /**
     * Checks that all the parameters in an array are uninitialized. Opposite of
     * isDefined except for empty or null arrays.
     *
     * @param p The array of parameters. May be null;
     * @return True if all parameters are undefined (or null)
     */
    public static boolean isDefault(Parameter[] p) {
        if (p == null) {
            return true;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i] != UNDEFINED) {
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
            if (p[i] == UNDEFINED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasFixedParameters(Parameter[] p) {
        if (p == null) {
            return false;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i].isFixed()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasDefinedParameters(Parameter[] p) {
        if (p == null) {
            return false;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i].isDefined()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFixed(Parameter p) {
        if (p == null) {
            return false;
        } else {
            return p.isFixed();
        }
    }

    /**
     * Checks that a parameter is defined. A parameter is defined if it is non
     * null and if its type is not undefined
     *
     * @param p
     * @return
     */
    public static boolean isDefined(Parameter p) {
        return p != null && p != UNDEFINED;
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
     * @return True if some defined parameters are different from 0. An empty
     * array is considered as a 0-array (returns true).
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
    
    @Override
    public String toString(){
        if (type == ParameterType.Undefined)
            return "...";
        else{
            StringBuilder builder=new StringBuilder();
            builder.append(new Formatter().format("%6g", value));
            switch (type){
                case Fixed: builder.append('f'); break;
                case Estimated: builder.append('e'); break;
            }
            return builder.toString();
        }
    }

    private static final Parameter UNDEFINED = new Parameter(0, ParameterType.Undefined), ZERO = new Parameter(0, ParameterType.Fixed);
}
