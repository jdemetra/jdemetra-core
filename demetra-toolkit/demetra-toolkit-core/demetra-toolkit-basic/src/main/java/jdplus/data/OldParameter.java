/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package jdplus.data;

import java.util.Formatter;
import java.util.Objects;
import nbbrd.design.Development;
import demetra.data.ParameterType;

/**
 * Describes a parameter. A parameter contains its type (the way values 
 * should be interpreted), its value and (in some case) its standard deviation.
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Deprecated
public final class OldParameter implements Cloneable, Comparable<OldParameter> {

    private double value;
    private ParameterType type;

    private static final String EMPTY="";
    /**
     * Makes a copy of an array of parameters
     * @param value The parameters being copied
     * @return A new array of parameters is returned (deep copy)
     */
    public static OldParameter[] clone(OldParameter[] value) {
        if (value==null) {
            return null;
        }
        OldParameter[] p = new OldParameter[value.length];
        for (int i = 0; i < value.length; ++i) {
            if (value[i] != null) {
                p[i] = value[i].clone();
            }
        }
        return p;
    }
    
    public static OldParameter clone(OldParameter p){
        return p == null ? null : p.clone();
    }

    /**
     * Creates an array of parameters
     * @param n The size of the new array
     * @return An array of n parameters. They are all set to the default value
     * (undefined type). Returns null for n = 0.
     */
    public static OldParameter[] create(int n) {
        if (n == 0) {
            return null;
        }
        OldParameter[] p = new OldParameter[n];
        for (int i = 0; i < n; ++i) {
            p[i] = new OldParameter();
        }
        return p;
    }

    /**
     * Checks that an array of parameters contains fixed values
     * @param p The array of parameters. May be null.
     * @return True if some item of the array is fixed
     */
    public static boolean hasFixedParameters(OldParameter[] p) {
        if (p == null) {
            return false;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i] != null && p[i].type == ParameterType.Fixed) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that an array of parameters contains free values
     * @param p The array of parameters. May be null.
     * @return True if some item of the array is free (or null)
     */
    public static boolean hasFreeParameters(OldParameter[] p) {
        if (p == null) {
            return false;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i] == null || p[i].type != ParameterType.Fixed) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Counts the number of fixed parameters in an array
     * @param p The considered array of parameters. May be null. 
     * @return The number of fixed parameters (in [0, p.length[)
     */
    public static int countFixedParameters(OldParameter[] p) {
        if (p == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < p.length; ++i) {
            if (p[i] != null && p[i].type == ParameterType.Fixed) {
                ++n;
            }
        }
        return n;
    }

     /**
     * Counts the number of free(= not fixed) parameters in an array
     * @param p The considered array of parameter s. May be null.
     * @return The number of fixed parameters (in [0, p.length[)
     */
   public static int countFreeParameters(OldParameter[] p) {
        if (p == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < p.length; ++i) {
            if (p[i] == null || p[i].type != ParameterType.Fixed) {
                ++n;
            }
        }
        return n;
    }

    /**
     * Checks that all the parameters in an array are uninitialized.
     * Opposite of isDefined except for empty or null arrays.
     * @param p The array of parameters. May be null;
     * @return True if all parameters are undefined (or null)
     */
    public static boolean isDefault(OldParameter[] p) {
        if (p == null) {
            return true;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i] != null && p[i].type != ParameterType.Undefined) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDefault(OldParameter p) {
        if (p == null) {
            return true;
        }
        return p.type == ParameterType.Undefined;
    }
    /**
     * Checks that all the parameters in an array are defined. Opposite of isDefault
     * except for empty (or null) arrays.
     * @param p The array of parameters. May be null;
     * @return True if all parameters are defined (or null)
     */
    public static boolean isDefined(OldParameter[] p) {
        if (p == null) {
            return true;
        }
        for (int i = 0; i < p.length; ++i) {
            if (p[i] == null || p[i].type == ParameterType.Undefined) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that a parameter is defined. A parameter is defined if it is non 
     * null and if its type is not undefined
     * @param p
     * @return 
     */
    public static boolean isDefined(OldParameter p) {
        return p != null && p.type != ParameterType.Undefined;
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
     * @param p The considered array of parameters.
     * @return True if some defined parameters are different from 0.
     * An empty array is considered as a 0-array (returns true).
     */
    public static boolean isZero(OldParameter[] p) {
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

    /**
     * Creates a new undefined parameter
     */
    public OldParameter() {
        type = ParameterType.Undefined;
    }

    /**
     * Creates and initializes a new Parameter
     * @param value The value of the parameter
     * @param type The type of the parameter
     */
    public OldParameter(double value, ParameterType type) {
        this.type = type;
        this.value = value;
    }

    @Override
    public OldParameter clone() {
        try {
            return (OldParameter) super.clone();
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    public void copy(OldParameter mu) {
        this.value=mu.value;
        this.type=mu.type;
    }

    @Override
    public int compareTo(OldParameter p) {
        int r = Double.compare(value, p.value);
        if (r != 0) {
            return r;
        } else {
            return type.compareTo(p.type);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OldParameter && equals((OldParameter) obj));
    }
    
    public boolean equals(OldParameter other) {
        return value == other.value && type == other.type;
    }

    /**
     * Gets the type of the parameter. See ParameterType for further explanations.
     * @return The type of the parameter
     */
    public ParameterType getType() {
        return type;
    }

    /**
     * Gets the value of the parameter
     * @return Value of the parameter. 0 by default.
     */
    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Double.hashCode(this.value);
        hash = 59 * hash + Objects.hashCode(this.type);
        return hash;
    }

    /**
     * Sets the type of the parameter. Settings the type of a parameter can change 
     * its value (set to 0 if undefined) or its standard deviation 
     * (set to 0 if not estimated)
     * @param value The type of the parameter.
     */
    public void setType(ParameterType value) {
        type = value;
        if (type == ParameterType.Undefined) {
            this.value = 0;
        } 
    }

    /**
     * Sets the value of a parameter.
     * @param value The value of the parameter.
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Checks that a parameter is fixed. 
     * @return True if the type is fixed, false otherwise.
     */
    public boolean isFixed(){
        return type == ParameterType.Fixed;
    }

    @Override
    public String toString() {
        return toString("%4g");
    }

    /**
     * Literal representation of a parameter, using a pre-specified format.
     * @param fmt The format used to express the values (if any)
     * @return The literal presentation of the parameter.
     * For instance: 
     * "" for undefined parameter
     * "1.2345" for general parameter
     * "1.2345f for fixed parameter
     * "1.2345 (0.0001) for estimated parameter (with standard deviation)
     */
    public String toString(String fmt) {
        if (type == ParameterType.Undefined)
            return EMPTY;
        StringBuilder builder = new StringBuilder();

        builder.append(new Formatter().format(fmt, value).toString());
        if (type == ParameterType.Fixed)
            builder.append('f');
        return builder.toString();
    }


}
