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
package demetra.util;

import java.util.EnumSet;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class PrimitiveEnum {

    @Nonnull
    public <T extends Enum<T> & IntSupplier> IntFunction<T> ofInt(@NonNull Class<T> enumType) {
        EnumSet<T> enumValues = EnumSet.allOf(enumType);
        return value -> get(enumValues, o -> o.getAsInt(), value);
    }

    @Nonnull
    public <T extends Enum<T>> IntFunction<T> ofInt(@NonNull Class<T> enumType, @NonNull ToIntFunction<T> toInt) {
        EnumSet<T> enumValues = EnumSet.allOf(enumType);
        return value -> get(enumValues, toInt, value);
    }

    private <T extends Enum<T>> T get(EnumSet<T> enumValues, ToIntFunction<T> toInt, int value) {
        for (T o : enumValues) {
            if (toInt.applyAsInt(o) == value) {
                return o;
            }
        }
        return null;
    }
}
