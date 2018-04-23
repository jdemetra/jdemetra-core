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
package demetra.utilities;

import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class PrimitiveEnumTest {

    enum MyEnum implements IntSupplier {
        ONE(1), TWO(2);

        int value;

        private MyEnum(int value) {
            this.value = value;
        }

        @Override
        public int getAsInt() {
            return value;
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testOfInt1() {
        assertThatNullPointerException().isThrownBy(() -> PrimitiveEnum.ofInt(null));

        IntFunction<MyEnum> func = PrimitiveEnum.ofInt(MyEnum.class);
        assertThat(func.apply(1)).isEqualTo(MyEnum.ONE);
        assertThat(func.apply(2)).isEqualTo(MyEnum.TWO);
        assertThat(func.apply(7)).isNull();
        assertThat(func.apply(-1)).isNull();
    }

    @Test
    @SuppressWarnings("null")
    public void testOfInt2() {
        assertThatNullPointerException().isThrownBy(() -> PrimitiveEnum.ofInt(null, o -> 1));
        assertThatNullPointerException().isThrownBy(() -> PrimitiveEnum.ofInt(MyEnum.class, null));

        IntFunction<MyEnum> func = PrimitiveEnum.ofInt(MyEnum.class, MyEnum::getAsInt);
        assertThat(func.apply(1)).isEqualTo(MyEnum.ONE);
        assertThat(func.apply(2)).isEqualTo(MyEnum.TWO);
        assertThat(func.apply(7)).isNull();
        assertThat(func.apply(-1)).isNull();
    }
}
