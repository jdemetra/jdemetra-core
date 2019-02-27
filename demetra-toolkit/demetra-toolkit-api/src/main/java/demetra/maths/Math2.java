/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.maths;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Math2 {

    /**
     * Computes the greatest common divisor of two integers.
     *
     * @param a
     * @param b
     * @return
     */
    public static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is remainder  
            a = temp;
        }
        return a;
    }

    /**
     * Checks if Math#exp(double) has been intrinsified. For your information:
     * StrictMath insures portability by returning the same results on every
     * platform while Math might be optimized by the VM to improve performance.
     * In some edge cases (and if intrinsified), Math results are slightly
     * different.
     *
     * @return true if Math is currently intrinsified, false otherwise
     */
    public static boolean isMathExpIntrinsifiedByVM() {
        double edgeCase = 0.12585918361184556;
        return Math.exp(edgeCase) != StrictMath.exp(edgeCase);
    }
}
