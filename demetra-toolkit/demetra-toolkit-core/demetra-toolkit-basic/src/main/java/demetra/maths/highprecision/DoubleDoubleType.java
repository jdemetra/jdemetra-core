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
package demetra.maths.highprecision;

/**
 *
 * @author Jean Palate
 */
public interface DoubleDoubleType {

    double getHigh();

    double getLow();

    double asDouble();

    default boolean isNan() {
        return Double.isNaN(getHigh());
    }

        /**
     * Tests whether this value is less than 0.
     *
     * @return true if this value is less than 0
     */
    default boolean isNegative() {
        double high=getHigh(), low=getLow();
        return high < 0.0 || high == 0.0 && low < 0.0;
    }

    /**
     * Tests whether this value is greater than 0.
     *
     * @return true if this value is greater than 0
     */
    default boolean isPositive() {
        double high=getHigh(), low=getLow();
        return high > 0.0 || high == 0.0 && low > 0.0;
    }

    /**
     * Tests whether this value is equal to 0.
     *
     * @return true if this value is equal to 0
     */
    default boolean isZero() {
        return getHigh() == 0.0 && getLow() == 0.0;
    }

}
