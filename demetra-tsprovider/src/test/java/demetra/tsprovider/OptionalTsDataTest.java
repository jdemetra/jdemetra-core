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
import demetra.data.AggregationType;
import static demetra.data.AggregationType.Average;
import static demetra.data.AggregationType.First;
import static demetra.data.AggregationType.Last;
import static demetra.data.AggregationType.Max;
import static demetra.data.AggregationType.Min;
import static demetra.data.AggregationType.None;
import static demetra.data.AggregationType.Sum;
import demetra.timeseries.Fixme;
import static demetra.timeseries.Fixme.Undefined;
import demetra.timeseries.TsFrequency;
import static demetra.timeseries.TsFrequency.MONTHLY;
import static demetra.timeseries.TsFrequency.QUARTERLY;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.simplets.TsData;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.util.TsDataBuilder;
import static demetra.tsprovider.OptionalTsData.absent;
import static demetra.tsprovider.OptionalTsData.present;
import static internal.tsprovider.util.InternalTsDataBuilder.DUPLICATION_WITHOUT_AGGREGATION;
import static internal.tsprovider.util.InternalTsDataBuilder.GUESS_DUPLICATION;
import static internal.tsprovider.util.InternalTsDataBuilder.GUESS_SINGLE;
import static internal.tsprovider.util.InternalTsDataBuilder.INVALID_AGGREGATION;
import static internal.tsprovider.util.InternalTsDataBuilder.NO_DATA;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.function.Function;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static java.lang.Double.NaN;
import java.time.LocalDate;
import static java.util.EnumSet.complementOf;
import java.util.function.BiFunction;
import static org.assertj.core.api.Assertions.assertThat;
import static java.util.EnumSet.of;
import java.util.List;

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
                .isEqualTo(data(MONTHLY, 2010, 0, 10))
                .isNotEqualTo(data(MONTHLY, 2010, 0, 10, 20))
                .isNotEqualTo(data(MONTHLY, 2010, 0, 10, 10))
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

    @Test
    public void testBuilderByDate() {
        CustomFactory<Date> factory = new CustomFactory<Date>() {
            @Override
            public TsDataBuilder<Date> builderOf(ObsGathering gathering) {
                return TsDataBuilder.byDate(new GregorianCalendar(), gathering);
            }

            @Override
            public Date dateOf(int year, int month, int dayOfMonth) {
                return new GregorianCalendar(year, month - 1, dayOfMonth).getTime();
            }
        };
        testBuilderAdd(factory);
        testBuilderPresent(factory);
        testBuilderAbsent(factory);
    }

    @Test
    public void testBuilderByLocalDate() {
        CustomFactory<LocalDate> factory = new CustomFactory<LocalDate>() {
            @Override
            public TsDataBuilder<LocalDate> builderOf(ObsGathering gathering) {
                return TsDataBuilder.byLocalDate(gathering);
            }

            @Override
            public LocalDate dateOf(int year, int month, int dayOfMonth) {
                return LocalDate.of(year, month, dayOfMonth);
            }
        };
        testBuilderAdd(factory);
        testBuilderPresent(factory);
        testBuilderAbsent(factory);
    }

    private static OptionalTsData data(TsFrequency freq, int firstyear, int firstperiod, double... values) {
        return present(TsData.of(Fixme.asPeriod(freq, firstyear, firstperiod), DoubleSequence.ofInternal(values)));
    }

    private static <T> void testBuilderAdd(CustomFactory<T> cf) {
        Object[][] example = {{cf.dateOf(2010, 1, 1), 10}, {cf.dateOf(2010, 2, 1), 20}};
        Function<Object[], T> dateFunc = o -> (T) o[0];
        Function<Object[], Number> valueFunc = o -> (Number) o[1];

        TsDataBuilder<T> b = cf.builderOf(MONTHLY, None, false);

        assertThat(b.clear().add(null, null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(null, 10).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add((T) example[0][0], null).build()).isEqualTo(data(MONTHLY, 2010, 0, NaN));
        assertThat(b.clear().add((T) example[0][0], 10).build()).isEqualTo(data(MONTHLY, 2010, 0, 10));

        assertThat(b.clear().add(example[0], o -> null, o -> null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(example[0], o -> null, valueFunc).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(example[0], dateFunc, o -> null).build()).isEqualTo(data(MONTHLY, 2010, 0, NaN));
        assertThat(b.clear().add(example[0], dateFunc, valueFunc).build()).isEqualTo(data(MONTHLY, 2010, 0, 10));
        assertThatThrownBy(() -> b.add(null, dateFunc, valueFunc)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.add(example[0], null, valueFunc)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.add(example[0], dateFunc, null)).isInstanceOf(NullPointerException.class);

        assertThat(b.clear().addAll(Stream.of(example), o -> null, o -> null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().addAll(Stream.of(example), o -> null, valueFunc).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().addAll(Stream.of(example), dateFunc, o -> null).build()).isEqualTo(data(MONTHLY, 2010, 0, NaN, NaN));
        assertThat(b.clear().addAll(Stream.of(example), dateFunc, valueFunc).build()).isEqualTo(data(MONTHLY, 2010, 0, 10, 20));
        assertThatThrownBy(() -> b.addAll(null, dateFunc, valueFunc)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.addAll(Stream.of(example), null, valueFunc)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.addAll(Stream.of(example), dateFunc, null)).isInstanceOf(NullPointerException.class);
    }

    private static <T> void testBuilderPresent(CustomFactory<T> cf) {
        TsDataBuilder<T> b;
        List<TsFrequency> defined = Fixme.complementOfUndefined();
        T jan2010 = cf.dateOf(2010, 1, 1);
        T feb2010 = cf.dateOf(2010, 2, 1);
        T apr2010 = cf.dateOf(2010, 4, 1);
        T may2010 = cf.dateOf(2010, 5, 1);

        // defined with single value
        defined.forEach(o -> {
            TsDataBuilder<T> b2 = cf.builderOf(o, None, false).add(jan2010, 10);
            assertThat(b2.build())
                    .isEqualTo(data(o, 2010, 0, 10))
                    .isEqualTo(b2.build());
        });

        // monthly with missing values
        b = cf.builderOf(MONTHLY, None, false).add(jan2010, 10).add(apr2010, 40);
        assertThat(b.build())
                .isEqualTo(data(MONTHLY, 2010, 0, 10, Double.NaN, Double.NaN, 40))
                .isEqualTo(b.build());

        // quarterly
        b = cf.builderOf(QUARTERLY, None, false).add(jan2010, 10).add(apr2010, 40);
        assertThat(b.build())
                .isEqualTo(data(QUARTERLY, 2010, 0, 10, 40))
                .isEqualTo(b.build());

        // undefined to monthly
        b = cf.builderOf(Undefined, None, false).add(jan2010, 10).add(feb2010, 20);
        assertThat(b.build())
                .isEqualTo(data(MONTHLY, 2010, 0, 10, 20))
                .isEqualTo(b.build());

        // undefined to monthly with missing values
        b = cf.builderOf(Undefined, None, false).add(jan2010, 10).add(feb2010, 20).add(may2010, 50);
        assertThat(b.build())
                .isEqualTo(data(MONTHLY, 2010, 0, 10, 20, Double.NaN, Double.NaN, 50))
                .isEqualTo(b.build());

        // undefined to quarterly
        b = cf.builderOf(Undefined, None, false).add(jan2010, 10).add(apr2010, 40);
        assertThat(b.build())
                .isEqualTo(data(QUARTERLY, 2010, 0, 10, 40))
                .isEqualTo(b.build());

        // defined with aggregation
        BiFunction<TsFrequency, AggregationType, OptionalTsData> b6 = (f, a) -> {
            return cf.builderOf(f, a, false)
                    .add(jan2010, 12)
                    .add(jan2010, 13)
                    .add(jan2010, 10)
                    .add(jan2010, 11)
                    .build();
        };
        defined.forEach(o -> {
            assertThat(b6.apply(o, First)).isEqualTo(data(o, 2010, 0, 12));
            assertThat(b6.apply(o, Last)).isEqualTo(data(o, 2010, 0, 11));
            assertThat(b6.apply(o, Min)).isEqualTo(data(o, 2010, 0, 10));
            assertThat(b6.apply(o, Max)).isEqualTo(data(o, 2010, 0, 13));
            assertThat(b6.apply(o, Average)).isEqualTo(data(o, 2010, 0, 46d / 4));
            assertThat(b6.apply(o, Sum)).isEqualTo(data(o, 2010, 0, 46));
        });

        // unordered daily to monthly
        Function<AggregationType, OptionalTsData> b7 = a -> {
            return cf.builderOf(MONTHLY, a, false)
                    .add(cf.dateOf(2010, 2, 1), 20)
                    .add(cf.dateOf(2010, 1, 3), 10)
                    .add(cf.dateOf(2010, 1, 4), 11)
                    .add(cf.dateOf(2010, 1, 1), 12)
                    .add(cf.dateOf(2010, 1, 2), 13)
                    .build();
        };
        assertThat(b7.apply(First)).isEqualTo(data(MONTHLY, 2010, 0, 12, 20));
        assertThat(b7.apply(Last)).isEqualTo(data(MONTHLY, 2010, 0, 11, 20));
        assertThat(b7.apply(Min)).isEqualTo(data(MONTHLY, 2010, 0, 10, 20));
        assertThat(b7.apply(Max)).isEqualTo(data(MONTHLY, 2010, 0, 13, 20));
        assertThat(b7.apply(Average)).isEqualTo(data(MONTHLY, 2010, 0, 46d / 4, 20));
        assertThat(b7.apply(Sum)).isEqualTo(data(MONTHLY, 2010, 0, 46, 20));
    }

    private static <T> void testBuilderAbsent(CustomFactory<T> cf) {
        TsDataBuilder<T> b;
        T jan2010 = cf.dateOf(2010, 1, 1);

        // no data
        Fixme.allOf().forEach(o -> {
            TsDataBuilder<T> b2 = cf.builderOf(o, None, false);
            assertThat(b2.build())
                    .isEqualTo(NO_DATA)
                    .isEqualTo(b2.build());
        });

        // invalid aggregation
        complementOf(of(None)).forEach(o -> {
            TsDataBuilder<T> b2 = cf.builderOf(Undefined, o, false).add(jan2010, 10);
            assertThat(b2.build())
                    .isEqualTo(INVALID_AGGREGATION)
                    .isEqualTo(b2.build());
        });

        // guess single
        b = cf.builderOf(Undefined, None, false).add(jan2010, 10);
        assertThat(b.build())
                .isEqualTo(GUESS_SINGLE)
                .isEqualTo(b.build());

        // guess duplication
        b = cf.builderOf(Undefined, None, false).add(jan2010, 10).add(jan2010, 20);
        assertThat(b.build())
                .isEqualTo(GUESS_DUPLICATION)
                .isEqualTo(b.build());

        // duplication without aggregation
        Fixme.complementOfUndefined().forEach(o -> {
            TsDataBuilder<T> b2 = cf.builderOf(o, None, false).add(jan2010, 10).add(jan2010, 20);
            assertThat(b2.build())
                    .isEqualTo(DUPLICATION_WITHOUT_AGGREGATION)
                    .isEqualTo(b2.build());
        });
    }

    private interface CustomFactory<T> {

        TsDataBuilder<T> builderOf(ObsGathering gathering);

        default TsDataBuilder<T> builderOf(TsFrequency freq, AggregationType aggregation, boolean skipMissingValues) {
            ObsGathering gathering = skipMissingValues
                    ? ObsGathering.excludingMissingValues(freq, aggregation)
                    : ObsGathering.includingMissingValues(freq, aggregation);
            return builderOf(gathering);
        }

        T dateOf(int year, int month, int dayOfMonth);
    }
}
