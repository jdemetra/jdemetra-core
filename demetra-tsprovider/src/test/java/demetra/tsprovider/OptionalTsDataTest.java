/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.tsprovider;

import demetra.data.DoubleSequence;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.simplets.TsData;
import static demetra.tsprovider.OptionalTsData.absent;
import static demetra.tsprovider.OptionalTsData.present;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static demetra.timeseries.TsUnit.MONTH;

/**
 *
 * @author Philippe Charles
 */
public class OptionalTsDataTest {

    @Test
    @SuppressWarnings("null")
    public void testFactoryPresent() {
        TsData example = TsData.of(TsPeriod.monthly(2010, 1), DoubleSequence.ofInternal(10));

        assertThat(present(example))
                .isEqualTo(data(MONTH, 2010, 10))
                .isNotEqualTo(data(MONTH, 2010, 10, 20))
                .isNotEqualTo(data(MONTH, 2010, 10, 10))
                .extracting(OptionalTsData::isPresent, OptionalTsData::get)
                .containsExactly(true, example);

        assertThatThrownBy(() -> present(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> present(example).getCause())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFactoryAbsent() {
        String example = "Some reason";

        assertThat(absent(example))
                .isEqualTo(absent(example))
                .isNotEqualTo(absent("Other"))
                .extracting(OptionalTsData::isPresent, OptionalTsData::getCause)
                .containsExactly(false, example);

        assertThatThrownBy(() -> absent(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> absent(example).get())
                .isInstanceOf(IllegalStateException.class);
    }

    private static OptionalTsData data(TsUnit unit, int year, double... values) {
        return present(TsData.of(TsPeriod.of(unit, LocalDate.of(year, 1, 1)), DoubleSequence.ofInternal(values)));
    }
}
