/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tsprovider.util;

import com.google.common.collect.ImmutableMap;
import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.legacy.LegacyHandler;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Philippe Charles
 */
public class DataSetConverterTest {

    @SuppressWarnings("null")
    private <T> void assertBehavior(DataSource.Converter<T> param, T defaultValue, T newValue, Map<String, String> keyValues) {
        DataSource.Builder builder = DataSource.builder("", "");

        // NPE
        assertThatThrownBy(() -> param.get(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> param.set(null, defaultValue)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> param.set(null, newValue)).isInstanceOf(NullPointerException.class);

        // default value
        assertThat(param.getDefaultValue()).isEqualTo(defaultValue);

        // keys absent => default value
        DataSource emptyConfig = builder.clearParameters().build();
        assertThat(param.get(emptyConfig)).isEqualTo(defaultValue);

        // keys present => new value
        builder.clearParameters();
        keyValues.forEach(builder::parameter);
        DataSource normalConfig = builder.build();
        if (newValue instanceof double[]) {
            assertThat((double[]) param.get(normalConfig)).containsExactly((double[]) newValue);
        } else {
            assertThat(param.get(normalConfig)).isEqualTo(newValue);
        }

        // new value => keys present
        builder.clearParameters();
        param.set(builder, newValue);
        DataSource newConfig = builder.build();
        assertThat(newConfig.getParameters())
                .containsExactlyInAnyOrderEntriesOf(keyValues);
        //keyValues.forEach((k, v) -> assertThat(newConfig.getParameter(k)).isEqualTo(v));

        // default value => keys absent
        builder.clearParameters();
        param.set(builder, defaultValue);
        assertThat(builder.build().getParameters()).isEmpty();
    }

    @Test
    public void testOnDataFormat() {
        ObsFormat defaultFormat = ObsFormat.builder().locale(null).dateTimePattern("yyyy-MM").build();
        DataSource.Converter<ObsFormat> x = LegacyHandler.onObsFormat("locale", "date", "number", defaultFormat).asDataSourceConverter();

        ObsFormat format1 = ObsFormat.builder().locale(null).dateTimePattern("dd-MM-yyyy").build();
        assertBehavior(x, defaultFormat, format1, ImmutableMap.of( "date", "dd-MM-yyyy"));

        ObsFormat format2 = ObsFormat.builder().locale(null).dateTimePattern("dd-MM-yyyy").numberPattern("#").build();
        assertBehavior(x, defaultFormat, format2, ImmutableMap.of("date", "dd-MM-yyyy", "number", "#"));
    }

    @Test
    @SuppressWarnings("null")
    public void testOnObsGathering() {
        ObsGathering defaultValue = ObsGathering.DEFAULT;
        ObsGathering newValue = ObsGathering.builder().unit(TsUnit.YEAR).aggregationType(AggregationType.Average).includeMissingValues(true).build();
        assertBehavior(LegacyHandler.onObsGathering("f", "a", "s", defaultValue).asDataSourceConverter(), defaultValue, newValue, ImmutableMap.of("f", "Yearly", "a", "Average", "s", "false"));
        assertThatThrownBy(() -> LegacyHandler.onObsGathering("f", "a", "s", null).asDataSourceConverter()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> LegacyHandler.onObsGathering(null, "a", "s", defaultValue).asDataSourceConverter()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> LegacyHandler.onObsGathering("f", null, "s", defaultValue).asDataSourceConverter()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> LegacyHandler.onObsGathering("f", "a", null, defaultValue).asDataSourceConverter()).isInstanceOf(NullPointerException.class);
    }
}
