/*
 * Copyright 2019 National Bank of Belgium.
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
package jdplus.maths.highprecision;

/**
 * Immutable, extended-precision floating-point numbers which maintain 106 bits
 * (approximately 30 decimal digits) of precision.
 * <p>
 * A DoubleDouble uses a representation containing two double-precision values.
 * A number x is represented as a pair of doubles, x.hi and x.lo, such that the
 * number represented by x is x.hi + x.lo, where
 *
 * <pre>
 *    |x.lo| <= 0.5*ulp(x.hi)
 * </pre>
 *
 * and ulp(y) means "unit in the last place of y". The basic arithmetic
 * operations are implemented using convenient properties of IEEE-754
 * floating-point arithmetic.
 * <p>
 * The range of values which can be represented is the same as in IEEE-754. The
 * precision of the representable numbers is twice as great as IEEE-754 double
 * precision.
 * <p>
 * The correctness of the arithmetic algorithms relies on operations being
 * performed with standard IEEE-754 double precision and rounding. This is the
 * Java standard arithmetic model, but for performance reasons Java
 * implementations are not constrained to using this standard by default. Some
 * processors (notably the Intel Pentium architecure) perform floating point
 * operations in (non-IEEE-754-standard) extended-precision. A JVM
 * implementation may choose to use the non-standard extended-precision as its
 * default arithmetic mode. To prevent this from happening, this code uses the
 * Java <tt>strictfp</tt> modifier, which forces all operations to take place in
 * the standard IEEE-754 rounding model.
 * <p>
 * This implementation uses algorithms originally designed variously by Knuth,
 * Kahan, Dekker, and Linnainmaa. Douglas Priest developed the first C
 * implementation of these techniques. Other more recent C++ implementation are
 * due to Keith M. Briggs and David Bailey et al.
 *
 * <h3>References</h3>
 * <ul>
 * <li>Priest, D., <i>Algorithms for Arbitrary Precision Floating Point
 * Arithmetic</i>, in P. Kornerup and D. Matula, Eds., Proc. 10th Symposium on
 * Computer Arithmetic, IEEE Computer Society Press, Los Alamitos, Calif., 1991.
 * <li>Yozo Hida, Xiaoye S. Li and David H. Bailey, <i>Quad-Double Arithmetic:
 * Algorithms, Implementation, and Application</i>, manuscript, Oct 2000;
 * Lawrence Berkeley National Laboratory Report BNL-46996.
 * <li>David Bailey, <i>High Precision Software Directory</i>;
 * <tt>http://crd.lbl.gov/~dhbailey/mpdist/index.html</tt>
 * </ul>
 *
 * @author Martin Davis
 *
 *
 */
@lombok.Value
public strictfp class DoubleDouble implements DoubleDoubleType, Comparable<DoubleDouble> {

    private double high, low;

    /**
     * The value nearest to the constant Pi.
     */
    public static final DoubleDouble PI = new DoubleDouble(
            3.141592653589793116e+00,
            1.224646799147353207e-16);

    /**
     * The value nearest to the constant 2 * Pi.
     */
    public static final DoubleDouble TWO_PI = new DoubleDouble(
            6.283185307179586232e+00,
            2.449293598294706414e-16);

    /**
     * The value nearest to the constant Pi / 2.
     */
    public static final DoubleDouble PI_2 = new DoubleDouble(
            1.570796326794896558e+00,
            6.123233995736766036e-17);

    /**
     * The value nearest to the constant e (the natural logarithm base).
     */
    public static final DoubleDouble E = new DoubleDouble(
            2.718281828459045091e+00,
            1.445646891729250158e-16);

    /**
     * A value representing the result of an operation which does not return a
     * valid number.
     */
    public static final DoubleDouble NaN = new DoubleDouble(
            Double.NaN, 0);

    /**
     * The smallest representable relative difference between two {link @
     * DoubleDouble} values
     */
    public static final double EPS = 1.23259516440783e-32;
    /* = 2^-106 */

    public static final DoubleDouble TEN = new DoubleDouble(10.0, 0);

    public static final DoubleDouble ONE = new DoubleDouble(1.0, 0);

    private static final int MAX_PRINT_DIGITS = 32;

    private static final String SCI_NOT_EXPONENT_CHAR = "E";

    private static final String SCI_NOT_ZERO = "0.0E0";

    /**
     * Converts a string representation of a real number into a DoubleDouble
     * value. The format accepted is similar to the standard Java real number
     * syntax. It is defined by the following regular expression:
     *
     * <pre>
     * [<tt>+</tt>|<tt>-</tt>] {<i>digit</i>} [ <tt>.</tt> {<i>digit</i>} ] [ ( <tt>e</tt> | <tt>E</tt> ) [<tt>+</tt>|<tt>-</tt>
     * ] {<i>digit</i>}+
     *
     * </pre>
     *
     * @param str the string to parse
     * @return the value of the parsed number
     * @throws NumberFormatException if <tt>str</tt>
     * is not a valid representation of a number
     */
    public static DoubleDouble parse(String str) throws NumberFormatException {
        int i = 0;
        int strlen = str.length();

        // skip leading whitespace
        while (Character.isWhitespace(str.charAt(i))) {
            i++;
        }

        // check for sign
        boolean isNegative = false;
        if (i < strlen) {
            char signCh = str.charAt(i);
            if (signCh == '-' || signCh == '+') {
                i++;
                if (signCh == '-') {
                    isNegative = true;
                }
            }
        }

        // scan all digits and accumulate into an integral value
        // Keep track of the location of the decimal point (if any) to allow
        // scaling later
        DoubleDoubleComputer val = new DoubleDoubleComputer();

        int numDigits = 0;
        int numBeforeDec = 0;
        int exp = 0;
        while (true) {
            if (i >= strlen) {
                break;
            }
            char ch = str.charAt(i);
            i++;
            if (Character.isDigit(ch)) {
                double d = ch - '0';
                val.mul(10, 0);
                // MD: need to optimize this
                val.add(d, 0);
                numDigits++;
                continue;
            }
            if (ch == '.') {
                numBeforeDec = numDigits;
                continue;
            }
            if (ch == 'e' || ch == 'E') {
                String expStr = str.substring(i);
                // this should catch any format problems with the exponent
                try {
                    exp = Integer.parseInt(expStr);
                } catch (NumberFormatException ex) {
                    throw new NumberFormatException("Invalid exponent "
                            + expStr + " in string "
                            + str);
                }
                break;
            }
            throw new NumberFormatException("Unexpected character '" + ch
                    + "' at position " + i
                    + " in string " + str);
        }
        DoubleDouble val2;

        // scale the number correctly
        int numDecPlaces = numDigits - numBeforeDec - exp;
        if (numDecPlaces == 0) {
            val2 = val.result();
        } else if (numDecPlaces > 0) {
            DoubleDouble scale = TEN.pow(numDecPlaces);
            val2 = val.div(scale).result();
        } else {
            DoubleDouble scale = TEN.pow(-numDecPlaces);
            val2 = val.mul(scale).result();
        }
        // apply leading sign, if any
        if (isNegative) {
            return val.chs().result();
        }
        return val2;

    }

    /**
     * Converts the <tt>double</tt> argument to a DoubleDouble number.
     *
     * @param x a numeric value
     * @return the extended precision version of the value
     */
    public static DoubleDouble valueOf(double x) {
        return new DoubleDouble(x, 0);
    }

    /**
     * Converts the string argument to a DoubleDouble number.
     *
     * @param str a string containing a representation of a numeric value
     * @return the extended precision version of the value
     * @throws NumberFormatException if <tt>s</tt> is not a valid representation
     * of a number
     */
    public static DoubleDouble valueOf(String str) throws NumberFormatException {
        return parse(str);
    }

    @Override
    public double asDouble() {
        return high + low;
    }

    /**
     * Returns the smallest (closest to negative infinity) value that is not
     * less than the argument and is equal to a mathematical integer. Special
     * cases:
     * <ul>
     * <li>If this value is NaN, returns NaN.
     * </ul>
     *
     * @return the smallest (closest to negative infinity) value that is not
     * less than the argument and is equal to a mathematical integer.
     */
    public DoubleDouble ceil() {
        if (isNaN()) {
            return NaN;
        }
        double fhi = Math.ceil(high);
        double flo = 0.0;
        // Hi is already integral. Ceil the low word
        if (fhi == high) {
            flo = Math.ceil(low);
            // do we need to renormalize here?
        }
        return new DoubleDouble(fhi, flo);
    }

    /**
     * Compares two DoubleDouble objects numerically.
     *
     * @param other
     * @return -1,0 or 1 depending on whether this value is less than, equal to
     * or greater than the value of <tt>o</tt>
     */
    @Override
    public int compareTo(DoubleDouble other) {
        if (high < other.high) {
            return -1;
        }
        if (high > other.high) {
            return 1;
        }
        if (low < other.low) {
            return -1;
        }
        if (low > other.low) {
            return 1;
        }
        return 0;
    }

    /**
     * Tests whether this value is NaN.
     *
     * @return true if this value is NaN
     */
    public boolean isNaN() {
        return Double.isNaN(high);
    }

    /**
     * Tests whether this value is less than 0.
     *
     * @return true if this value is less than 0
     */
    @Override
    public boolean isNegative() {
        return high < 0.0 || high == 0.0 && low < 0.0;
    }

    /**
     * Tests whether this value is greater than 0.
     *
     * @return true if this value is greater than 0
     */
    @Override
    public boolean isPositive() {
        return high > 0.0 || high == 0.0 && low > 0.0;
    }

    /**
     * Tests whether this value is equal to 0.
     *
     * @return true if this value is equal to 0
     */
    @Override
    public boolean isZero() {
        return high == 0.0 && low == 0.0;
    }

    /**
     * Tests whether this value is less than or equal to another
     * <tt>DoubleDouble</tt> value.
     *
     * @param y a DoubleDouble value
     * @return true if this value <= y
     */
    public boolean le(DoubleDouble y) {
        return high < y.high || high == y.high && low <= y.low;
    }

    /**
     * Tests whether this value is less than another <tt>DoubleDouble</tt>
     * value.
     *
     * @param y a DoubleDouble value
     * @return true if this value < y
     */
    public boolean lt(DoubleDouble y) {
        return high < y.high || high == y.high && low < y.low;
    }

    public String dump() {
        return "DD<" + high + ", " + low + ">";
    }

    /**
     * Returns the largest (closest to positive infinity) value that is not
     * greater than the argument and is equal to a mathematical integer. Special
     * cases:
     * <ul>
     * <li>If this value is NaN, returns NaN.
     * </ul>
     *
     * @return the largest (closest to positive infinity) value that is not
     * greater than the argument and is equal to a mathematical integer.
     */
    public DoubleDouble floor() {
        if (isNaN()) {
            return NaN;
        }
        double fhi = Math.floor(high);
        double flo = 0.0;
        // Hi is already integral. Floor the low word
        if (fhi == high) {
            flo = Math.floor(low);
        }
        // do we need to renormalize here?
        return new DoubleDouble(fhi, flo);
    }

    /**
     * Tests whether this value is greater than or equals to another
     * <tt>DoubleDouble</tt> value.
     *
     * @param y a DoubleDouble value
     * @return true if this value >= y
     */
    public boolean ge(DoubleDouble y) {
        return high > y.high || high == y.high && low >= y.low;
    }

    /**
     * Tests whether this value is greater than another <tt>DoubleDouble</tt>
     * value.
     *
     * @param y a DoubleDouble value
     * @return true if this value > y
     */
    public boolean gt(DoubleDouble y) {
        return high > y.high || high == y.high && low > y.low;
    }

    /**
     * Converts this value to the nearest integer.
     *
     * @return the nearest integer to this value
     */
    public int intValue() {
        return (int) high;
    }

    public DoubleDouble plus(DoubleDouble dd) {
        if (isNan()) {
            return this;
        }
        if (dd.isNaN()) {
            return dd;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.add(dd);
        return computer.result();
    }

    public DoubleDouble minus(DoubleDouble dd) {
        if (isNan()) {
            return this;
        }
        if (dd.isNaN()) {
            return dd;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.sub(dd);
        return computer.result();
    }

    public DoubleDouble times(DoubleDouble dd) {
        if (isNan()) {
            return this;
        }
        if (dd.isNaN()) {
            return dd;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.mul(dd);
        return computer.result();
    }

    public DoubleDouble divide(DoubleDouble dd) {
        if (isNan()) {
            return this;
        }
        if (dd.isNaN()) {
            return dd;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.div(dd);
        return computer.result();
    }

    public DoubleDouble times(double d) {
        if (isNan()) {
            return this;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.mul(d);
        return computer.result();
    }

    public DoubleDouble divide(double d) {
        if (isNan()) {
            return this;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.div(d);
        return computer.result();
    }

    public DoubleDouble inv() {
        if (isNan()) {
            return this;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.inv();
        return computer.result();
    }

    public DoubleDouble sqrt() {
        if (isNan()) {
            return this;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.sqrt();
        return computer.result();
    }

    public DoubleDouble square() {
        if (isNan()) {
            return this;
        }
        DoubleDoubleComputer computer = new DoubleDoubleComputer(this);
        computer.mul(this);
        return computer.result();
    }

    /**
     * Returns a DoubleDouble whose value is <tt>-this</tt>.
     *
     * @return <tt>-this</tt>
     */
    public DoubleDouble negate() {
        if (isNaN()) {
            return this;
        }
        return new DoubleDouble(-high, -low);
    }

    /**
     * Returns the absolute value of this value. Special cases:
     * <ul>
     * <li>If this value is NaN, it is returned.
     * </ul>
     *
     * @return the absolute value of this value
     */
    public DoubleDouble abs() {
        if (isNaN()) {
            return NaN;
        }
        if (isNegative()) {
            return negate();
        } else {
            return this;
        }
    }

    /**
     * Computes the value of this number raised to an integral power. Follows
     * semantics of Java Math.pow as closely as possible.
     *
     * @param exp the integer exponent
     * @return x raised to the integral power exp
     */
    public DoubleDouble pow(int exp) {
        if (exp == 0.0) {
            return valueOf(1.0);
        }

        DoubleDoubleComputer r = new DoubleDoubleComputer(this);
        DoubleDoubleComputer s = new DoubleDoubleComputer(1);
        int n = Math.abs(exp);

        if (n > 1) {
            /* Use binary exponentiation */
            while (n > 0) {
                if (n % 2 == 1) {
                    s = s.mul(r);
                }
                n /= 2;
                if (n > 0) {
                    r = r.square();
                }
            }
        } else {
            s = r;
        }

        /* Compute the reciprocal if n is negative. */
        if (exp < 0) {
            s.inv();
        }
        return s.result();
    }

    /**
     * Rounds this value to the nearest integer. The value is rounded to an
     * integer by adding 1/2 and taking the floor of the result. Special cases:
     * <ul>
     * <li>If this value is NaN, returns NaN.
     * </ul>
     *
     * @return this value rounded to the nearest integer
     */
    public DoubleDouble rint() {
        if (isNaN()) {
            return this;
        }
        // may not be 100% correct
        DoubleDouble plus5 = plus(new DoubleDouble(0.5, 0));
        return plus5.floor();
    }

    /**
     * Returns an integer indicating the sign of this value.
     * <ul>
     * <li>if this value is > 0, returns 1
     * <li>if this value is < 0, returns -1 <li>if this value is = 0, returns 0
     * <li>if this value is NaN, returns 0
     * </ul>
     *
     * @return an integer indicating the sign of this value
     */
    public int signum() {
        if (isPositive()) {
            return 1;
        }
        if (isNegative()) {
            return -1;
        }
        return 0;
    }

    /**
     * Determines the decimal magnitude of a number. The magnitude is the
     * exponent of the greatest power of 10 which is less than or equal to the
     * number.
     *
     * @param x the number to find the magnitude of
     * @return the decimal magnitude of x
     */
    private static int magnitude(double x) {
        double xAbs = Math.abs(x);
        double xLog10 = Math.log(xAbs) / Math.log(10);
        int xMag = (int) Math.floor(xLog10);
        /**
         * Since log computation is inexact, there may be an off-by-one error in
         * the computed magnitude. Following tests that magnitude is correct,
         * and adjusts it if not
         */
        double xApprox = Math.pow(10, xMag);
        if (xApprox * 10 <= xAbs) {
            xMag += 1;
        }

        return xMag;
    }

    /**
     * Returns the string for this value if it has a known representation. (E.g.
     * NaN or 0.0)
     *
     * @return the string for this special number
     * @return null if the number is not a special number
     */
    private String getSpecialNumberString() {
        if (isZero()) {
            return "0.0";
        }
        if (isNaN()) {
            return "NaN ";
        }
        return null;
    }

    /**
     * Extracts the significant digits in the decimal representation of the
     * argument. A decimal point may be optionally inserted in the string of
     * digits (as long as its position lies within the extracted digits - if
     * not, the caller must prepend or append the appropriate zeroes and decimal
     * point).
     *
     * @param y the number to extract ( >= 0)
     * @param decimalPointPos the position in which to insert a decimal point
     * @return the string containing the significant digits and possibly a
     * decimal point
     */
    private String extractSignificantDigits(boolean insertDecimalPoint,
            int[] magnitude) {
        DoubleDouble y = abs();
        // compute *correct* magnitude of y
        int mag = magnitude(y.high);
        DoubleDouble scale = TEN.pow(mag);
        y = y.divide(scale);

        // fix magnitude if off by one
        if (y.gt(TEN)) {
            y = y.divide(TEN);
            mag += 1;
        } else if (y.lt(ONE)) {
            y = y.times(TEN);
            mag -= 1;
        }

        int decimalPointPos = mag + 1;
        StringBuilder buf = new StringBuilder();
        int numDigits = MAX_PRINT_DIGITS - 1;
        for (int i = 0; i <= numDigits; i++) {
            if (insertDecimalPoint && i == decimalPointPos) {
                buf.append('.');
            }
            int digit = (int) y.high;
            // System.out.println("printDump: [" + i + "] digit: " + digit +
            // "  y: " + y.dump() + "  buf: " + buf);

            /**
             * This should never happen, due to heuristic checks on remainder
             * below
             */
            if (digit < 0 || digit > 9) {
                // System.out.println("digit > 10 : " + digit);
                // throw new
                // IllegalStateException("Internal errror: found digit = " +
                // digit);
            }
            /**
             * If a negative remainder is encountered, simply terminate the
             * extraction. This is robust, but maybe slightly inaccurate. My
             * current hypothesis is that negative remainders only occur for
             * very small lo components, so the inaccuracy is tolerable
             */
            if (digit < 0) {
                break;
                // throw new
                // IllegalStateException("Internal errror: found digit = " +
                // digit);
            }
            boolean rebiasBy10 = false;
            char digitChar = 0;
            if (digit > 9) {
                // set flag to re-bias after next 10-shift
                rebiasBy10 = true;
                // output digit will end up being '9'
                digitChar = '9';
            } else {
                digitChar = (char) ('0' + digit);
            }
            buf.append(digitChar);
            y = y.minus(DoubleDouble.valueOf(digit)).times(TEN);
            if (rebiasBy10) {
                y = y.plus(TEN);
            }

            boolean continueExtractingDigits = true;
            /**
             * Heuristic check: if the remaining portion of y is non-positive,
             * assume that output is complete
             */
            // if (y.hi <= 0.0)
            // if (y.hi < 0.0)
            // continueExtractingDigits = false;
            /**
             * Check if remaining digits will be 0, and if so don't output them.
             * Do this by comparing the magnitude of the remainder with the
             * expected precision.
             */
            int remMag = magnitude(y.high);
            if (remMag < 0 && Math.abs(remMag) >= numDigits - i) {
                continueExtractingDigits = false;
            }
            if (!continueExtractingDigits) {
                break;
            }
        }
        magnitude[0] = mag;
        return buf.toString();
    }

    /**
     * Returns the string representation of this value in scientific notation.
     *
     * @return the string representation in scientific notation
     */
    public String toSciNotation() {
        // special case zero, to allow as
        if (isZero()) {
            return SCI_NOT_ZERO;
        }

        String specialStr = getSpecialNumberString();
        if (specialStr != null) {
            return specialStr;
        }

        int[] magnitude = new int[1];
        String digits = extractSignificantDigits(false, magnitude);
        String expStr = SCI_NOT_EXPONENT_CHAR + magnitude[0];

        // should never have leading zeroes
        // MD - is this correct? Or should we simply strip them if they are
        // present?
        if (digits.charAt(0) == '0') {
            throw new IllegalStateException("Found leading zero: " + digits);
        }

        // add decimal point
        String trailingDigits = "";
        if (digits.length() > 1) {
            trailingDigits = digits.substring(1);
        }
        String digitsWithDecimal = digits.charAt(0) + "." + trailingDigits;

        if (isNegative()) {
            return "-" + digitsWithDecimal + expStr;
        }
        return digitsWithDecimal + expStr;
    }

    /**
     * Returns the string representation of this value in standard notation.
     *
     * @return the string representation in standard notation
     */
    public String toStandardNotation() {
        String specialStr = getSpecialNumberString();
        if (specialStr != null) {
            return specialStr;
        }

        int[] magnitude = new int[1];
        String sigDigits = extractSignificantDigits(true, magnitude);
        int decimalPointPos = magnitude[0] + 1;

        String num = sigDigits;
        // add a leading 0 if the decimal point is the first char
        if (sigDigits.charAt(0) == '.') {
            num = "0" + sigDigits;
        } else if (decimalPointPos < 0) {
            num = "0." + stringOfChar('0', -decimalPointPos) + sigDigits;
        } else if (sigDigits.indexOf('.') == -1) {
            // no point inserted - sig digits must be smaller than magnitude of
            // number
            // add zeroes to end to make number the correct size
            int numZeroes = decimalPointPos - sigDigits.length();
            String zeroes = stringOfChar('0', numZeroes);
            num = sigDigits + zeroes + ".0";
        }

        if (isNegative()) {
            return "-" + num;
        }
        return num;
    }

    /**
     * Creates a string of a given length containing the given character
     *
     * @param ch the character to be repeated
     * @param len the len of the desired string
     * @return the string
     */
    private static String stringOfChar(char ch, int len) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < len; i++) {
            buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * Returns a string representation of this number, in either standard or
     * scientific notation. If the magnitude of the number is in the range [
     * 10<sup>-3</sup>, 10<sup>8</sup> ] standard notation will be used.
     * Otherwise, scientific notation will be used.
     *
     * @return a string representation of this number
     */
    @Override
    public String toString() {
        int mag = magnitude(high);
        if (mag >= -3 && mag <= 20) {
            return toStandardNotation();
        }
        return toSciNotation();
    }

}
