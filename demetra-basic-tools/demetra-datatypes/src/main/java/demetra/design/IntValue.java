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
package demetra.design;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Defines a class that can be represented by an int.<br>
 * This int can be just a sequence or have a special meaning. It is often used
 * to convert an enum to an int.<br>
 * Be aware that there is no point in comparing two ints from two different
 * classes. For example, <code>Frequency.intValue() > Gender.intValue()</code>
 * would compile and give a result but would not have a real meaning. Also note
 * that this class should have a constructor or a factory that uses an int as
 * sole parameter. For example, <code>Frequency.valueOf(int)</code>.
 *
 * @author Philippe Charles
 */
public interface IntValue {

    /**
     * Returns the value of this Object as an int.<br>
     * This method is similar to {@link Integer#intValue()}.
     *
     * @return
     */
    int intValue();

    /**
     * Returns the enum constant of the specified enum type with the specified
     * int value.
     *
     * @param <T> The enum type whose constant is to be returned
     * @param enumType the {@code Class} object of the enum type from which to
     * return a constant
     * @param value the int value of the constant to return
     * @return the enum constant of the specified enum type with the specified
     * int value
     *
     * @since 2.2.0
     */
    static <T extends Enum<T> & IntValue> Optional<T> valueOf(Class<T> enumType, int value) {
        return EnumSet.allOf(enumType).stream()
                .filter(o -> o.intValue() == value)
                .findFirst();
    }
}
