/*
 * Copyright 2017 National Bank of Belgium
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

import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface ComplexParts {

    /**
     * Gets the real part of a Complex as a double.
     *
     * @return the real part
     */
    double getRe();

    /**
     * Gets the Imaginary part of a Complex as a double.
     *
     * @return the imaginary part
     */
    double getIm();

    default double abs() {
        return ComplexMath.abs(getRe(), getIm());
    }

    /**
     * Returns the square of the "length" of a Complex number. Norm(x + i*y) =
     * x*x + y*y. Always non-negative.
     *
     * @return
     */
    default double absSquare() {
        return ComplexMath.absSquare(getRe(), getIm());
    }

    /**
     * Returns the argument of this complex number.
     *
     * @return
     */
    default double arg() {
        return ComplexMath.arg(getIm(), getRe());
    }

    /**
     * Returns true if either the real or imaginary component of this Complex is
     * an infinite value.
     *
     * @return
     */
    default boolean isInfinity() {
        return Double.isInfinite(getRe()) || Double.isInfinite(getIm());
    }

    /**
     * Returns true if either the real or imaginary component of this Complex is
     * a Not-a-Number (NaN) value.
     *
     * @return
     */
    default boolean isNaN() {
        return Double.isNaN(getRe()) || Double.isNaN(getIm());
    }

    default double distance(@Nonnull ComplexParts c) {
        return ComplexMath.abs(getRe() - c.getRe(), getIm() - c.getIm());
    }
}
