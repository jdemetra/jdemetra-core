package demetra.timeseries.calendars;

import demetra.timeseries.TsUnit;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class RegularFrequencyTest {

    @Test
    public void testIntRepresentation() {
        for (RegularFrequency x : RegularFrequency.values()) {
            assertThat(RegularFrequency.parse(x.toInt()))
                    .isEqualTo(x);
        }
        assertThatIllegalArgumentException()
                .isThrownBy(() -> RegularFrequency.parse(42));
    }

    @Test
    public void testTsUnitRepresentation() {
        for (RegularFrequency x : RegularFrequency.values()) {
            assertThat(RegularFrequency.parseTsUnit(x.toTsUnit()))
                    .isEqualTo(x);
        }
        assertThatIllegalArgumentException()
                .isThrownBy(() -> RegularFrequency.parseTsUnit(TsUnit.MINUTE));
    }
}
