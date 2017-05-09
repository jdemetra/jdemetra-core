/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.maths;

import demetra.design.UtilityClass;

/**
 *
 * @author Jean Palate
 */
@UtilityClass(Double.class)
public final class Utilities {
    
    private Utilities(){}
    
    /**
     * Computes the hypothenuse of two numbers (h = sqrt(a*a + b*b) in a
     * numerically stable way
     *
     * @param a The first number
     * @param b The second number
     * @return The hypothenuse. A positive real number.
     */
    public static double hypotenuse(double a, double b) {
        double xa = Math.abs(a), xb = Math.abs(b);
        double w, z;
        if (xa > xb) {
            w = xa;
            z = xb;
        } else {
            w = xb;
            z = xa;
        }
        if (z == 0.0) {
            return w;
        } else {
            double zw = z / w;
            return w * Math.sqrt(1.0 + zw * zw);
        }
    }

    public static double jhypotenuse(double x, double y) {
        double xabs = Math.abs(x);
        double yabs = Math.abs(y);
        double w = Math.max(xabs, yabs);
        double z = Math.min(xabs, yabs);
        if (z == 0) {
            return w;
        } else {
            double zw = z / w;
            return w * Math.sqrt(1 - zw * zw);
        }
    }
    
}
