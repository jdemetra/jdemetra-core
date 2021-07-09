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
import org.junit.Test;

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
        keyValues.forEach((k, v) -> assertThat(newConfig.getParameter(k)).isEqualTo(v));

        // default value => keys absent
        builder.clearParameters();
        param.set(builder, defaultValue);
        assertThat(builder.build().getParameters()).isEmpty();
    }

    @Test
    public void testOnDataFormat() {
        ObsFormat d = ObsFormat.of(null, "yyyy-MM", null);
        ObsFormat n1 = ObsFormat.of(null, "dd-MM-yyyy", null);
        assertBehavior(TsProviders.onObsFormat(d, "k1", "k2", "k3"), d, n1, ImmutableMap.of("k1", "", "k2", "dd-MM-yyyy"));
        assertBehavior(TsProviders.onObsFormat(d, "k1", "k2", "k3"), d, n1, ImmutableMap.of("k1", "", "k2", "dd-MM-yyyy", "k3", ""));
        ObsFormat n2 = ObsFormat.of(null, "dd-MM-yyyy", "#");
        assertBehavior(TsProviders.onObsFormat(d, "k1", "k2", "k3"), d, n2, ImmutableMap.of("k1", "", "k2", "dd-MM-yyyy", "k3", "#"));
    }

    @Test
    @SuppressWarnings("null")
    public void testOnObsGathering() {
        ObsGathering defaultValue = ObsGathering.DEFAULT;
        ObsGathering newValue = ObsGathering.builder().unit(TsUnit.YEAR).aggregationType(AggregationType.Average).includeMissingValues(true).build();
        assertBehavior(TsProviders.onObsGathering(defaultValue, "f", "a", "s"), defaultValue, newValue, ImmutableMap.of("f", "Yearly", "a", "Average", "s", "false"));
        assertThatThrownBy(() -> TsProviders.onObsGathering(null, "f", "a", "s")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsProviders.onObsGathering(defaultValue, null, "a", "s")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsProviders.onObsGathering(defaultValue, "f", null, "s")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsProviders.onObsGathering(defaultValue, "f", "a", null)).isInstanceOf(NullPointerException.class);
    }
}
