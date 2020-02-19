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

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ParameterSpec {

    /**
     * Value of the parameter
     */
    private double value;
    /**
     * Type of the parameter. Should be undefined, initial or fixed
     */
    private ParameterType type;

    public boolean isFixed() {
        return type == ParameterType.Fixed;
    }

    public boolean isEstimated() {
        return type == ParameterType.Estimated;
    }
    
    public boolean isDerived() {
        return type == ParameterType.Derived;
    }

    /**
     * Free parameters are either undefined or initial parameters.
     * They should be estimated
     * @return 
     */
    public boolean isFree(){
        return type == ParameterType.Initial || type == ParameterType.Undefined;
    }

    /**
     * Check the some parameters are free
     * @param spec
     * @return Similar to freeParametersCount(spec) = 0 (but faster) 
     */
    public static boolean hasFreeParameters(ParameterSpec[] spec) {
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isFree()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the some parameters have the specified type
     * @param spec
     * @param type
     * @return 
     */
    public static boolean hasParameters(ParameterSpec[] spec, ParameterType type) {
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].type == type) {
                return true;
            }
        }
        return false;
    }
    /**
     * Gets the number of free parameters
     * @param spec
     * @return 
     */
    public static int freeParametersCount(ParameterSpec[] spec) {
        int n=0;
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isFree()) {
                ++n;
            }
        }
        return n;
    }
    
    /**
     * All the estimated parameters are set to undefined
     * @param spec 
     */    
    public static void resetParameters(ParameterSpec[] spec){
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isEstimated()) {
                spec[i]=UNDEFINED;
            }
        }
    }

    /**
     * All the estimated parameters are set to initial (keeping the current value)
     * @param spec 
     */    
    public static void freeParameters(ParameterSpec[] spec){
        for (int i = 0; i < spec.length; ++i) {
            if (spec[i].isEstimated()) {
                spec[i]=new ParameterSpec(spec[i].value, ParameterType.Initial);
            }
        }
    }

    /**
     * Checks that all the parameters are free. Derived parameters are not considered
     * @param spec
     * @return 
     */
    public static boolean isFree(ParameterSpec[] spec) {
        for (int i = 0; i < spec.length; ++i) {
            if (!spec[i].isFree() && !spec[i].isDerived()) {
                return false;
            }
        }
        return true;
    }

    public static double[] values(ParameterSpec[] spec) {
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

    public static ParameterSpec undefined() {
        return UNDEFINED;
    }

    public static ParameterSpec of(double value, ParameterType type) {
        if (type == ParameterType.Undefined) {
            return UNDEFINED;
        } else {
            return new ParameterSpec(value, type);
        }
    }

    public static ParameterSpec[] of(double values[], ParameterType type) {
        ParameterSpec[] p=new ParameterSpec[values.length];
        for (int i=0; i<p.length; ++i){
            p[i]= new ParameterSpec(values[i], type);
        }
        return p;
    }
    public static ParameterSpec[] make(int n) {
        ParameterSpec[] all = new ParameterSpec[n];
        for (int i = 0; i < n; ++i) {
            all[i] = UNDEFINED;
        }
        return all;
    }

    private static final ParameterSpec UNDEFINED = new ParameterSpec(0, ParameterType.Undefined);
}
