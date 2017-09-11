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
package demetra.tsprovider.util;

import demetra.data.AggregationType;
import static demetra.data.AggregationType.Average;
import static demetra.data.AggregationType.First;
import static demetra.data.AggregationType.Last;
import static demetra.data.AggregationType.Max;
import static demetra.data.AggregationType.Min;
import static demetra.data.AggregationType.None;
import static demetra.data.AggregationType.Sum;
import demetra.data.DoubleSequence;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import static demetra.timeseries.TsUnit.*;
import demetra.timeseries.simplets.TsData;
import demetra.tsprovider.OptionalTsData;
import static demetra.tsprovider.OptionalTsData.present;
import internal.tsprovider.util.TsDataBuilderUtil;
import static internal.tsprovider.util.TsDataBuilderUtil.DUPLICATION_WITHOUT_AGGREGATION;
import static internal.tsprovider.util.TsDataBuilderUtil.GUESS_DUPLICATION;
import static internal.tsprovider.util.TsDataBuilderUtil.GUESS_SINGLE;
import static internal.tsprovider.util.TsDataBuilderUtil.INVALID_AGGREGATION;
import static internal.tsprovider.util.TsDataBuilderUtil.NO_DATA;
import static java.lang.Double.NaN;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;
import java.util.GregorianCalendar;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsDataBuilderTest {

    @Test
    public void testBuilderByCalendar() {
        CustomFactory<Date> factory = new CustomFactory<Date>() {
            final GregorianCalendar cal = new GregorianCalendar();

            @Override
            public TsDataBuilder<Date> builder(ObsGathering gathering) {
                return TsDataBuilder.byCalendar(cal, gathering);
            }

            @Override
            public Date date(LocalDateTime o) {
                return Date.from(o.atZone(ZoneId.systemDefault()).toInstant());
            }

            @Override
            public boolean supports(TsUnit unit) {
                return true;
            }
        };
        assertCompliance(factory);
        testBuilderAdd(factory);
        testBuilderPresent(factory);
        testBuilderAbsent(factory);
    }

    @Test
    public void testBuilderByDate() {
        CustomFactory<LocalDate> factory = new CustomFactory<LocalDate>() {
            @Override
            public TsDataBuilder<LocalDate> builder(ObsGathering gathering) {
                return TsDataBuilder.byDate(gathering);
            }

            @Override
            public LocalDate date(LocalDateTime o) {
                return o.toLocalDate();
            }

            @Override
            public boolean supports(TsUnit unit) {
                return unit.getChronoUnit().isDateBased();
            }
        };
        assertCompliance(factory);
        testBuilderAdd(factory);
        testBuilderPresent(factory);
        testBuilderAbsent(factory);
    }

    @Test
    public void testBuilderByDateTime() {
        CustomFactory<LocalDateTime> factory = new CustomFactory<LocalDateTime>() {
            @Override
            public TsDataBuilder<LocalDateTime> builder(ObsGathering gathering) {
                return TsDataBuilder.byDateTime(gathering);
            }

            @Override
            public LocalDateTime date(LocalDateTime o) {
                return o;
            }

            @Override
            public boolean supports(TsUnit unit) {
                return true;
            }
        };
        assertCompliance(factory);
        testBuilderAdd(factory);
        testBuilderPresent(factory);
        testBuilderAbsent(factory);
    }

    @SuppressWarnings("null")
    private static <T> void testBuilderAdd(CustomFactory<T> x) {
        Object[][] example = {{x.date(START), 10}, {x.date(START.plusMonths(1)), 20}};
        Function<Object[], T> dateFunc = o -> (T) o[0];
        Function<Object[], Number> valueFunc = o -> (Number) o[1];

        TsDataBuilder<T> b = x.builder(ObsGathering.DEFAULT.withUnit(MONTHLY).withSkipMissingValues(false));

        assertThat(b.clear().add(null, null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(null, 10).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add((T) example[0][0], null).build()).isEqualTo(data(MONTHLY, 2010, NaN));
        assertThat(b.clear().add((T) example[0][0], 10).build()).isEqualTo(data(MONTHLY, 2010, 10));

        assertThat(b.clear().add(example[0], o -> null, o -> null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(example[0], o -> null, valueFunc).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(example[0], dateFunc, o -> null).build()).isEqualTo(data(MONTHLY, 2010, NaN));
        assertThat(b.clear().add(example[0], dateFunc, valueFunc).build()).isEqualTo(data(MONTHLY, 2010, 10));

        assertThat(b.clear().addAll(Stream.of(example), o -> null, o -> null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().addAll(Stream.of(example), o -> null, valueFunc).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().addAll(Stream.of(example), dateFunc, o -> null).build()).isEqualTo(data(MONTHLY, 2010, NaN, NaN));
        assertThat(b.clear().addAll(Stream.of(example), dateFunc, valueFunc).build()).isEqualTo(data(MONTHLY, 2010, 10, 20));
    }

    @SuppressWarnings("null")
    private static <T> void assertCompliance(CustomFactory<T> x) {
        TsDataBuilder<T> b = x.builder(ObsGathering.DEFAULT);

        Function<String, T> dateFunc = o -> x.date(START);
        Function<String, Number> valueFunc = o -> .1;

        assertThat(b.build()).isEqualTo(b.build()).isNotNull();
        assertThat(b.add(x.date(START), .1)).isSameAs(b);
        assertThat(b.addAll(Stream.of(""), dateFunc, valueFunc)).isSameAs(b);
        assertThat(b.clear()).isSameAs(b);

        assertThatThrownBy(() -> b.add("", null, valueFunc)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.add("", dateFunc, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.addAll(null, dateFunc, valueFunc)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.addAll(Stream.of(""), null, valueFunc)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.addAll(Stream.of(""), dateFunc, null)).isInstanceOf(NullPointerException.class);
    }

    private static <T> void testBuilderPresent(CustomFactory<T> x) {
        testDefinedUnits(x);
        testUndefinedUnit(x);
        testUnorderedDailyToMonthly(x);
    }

    private static <T> void testDefinedUnits(CustomFactory<T> x) {
        TsDataBuilderUtil.DEFINED_UNITS.forEach(unit -> {
            testDefinedWithSingleValue(x, unit);
            testDefinedWithMissingValues(x, unit);
            testDefinedWithAggregation(x, unit);
        });
    }

    private static <T> void testUndefinedUnit(CustomFactory<T> x) {
        TsDataBuilderUtil.GUESSING_UNITS.forEach(unit -> {
            testUndefinedToDefined(x, unit);
            testUndefinedToDefinedWithMissingValues(x, unit);
        });
    }

    private static <T> void testBuilderAbsent(CustomFactory<T> x) {
        testNoData(x);
        testInvalidAggregation(x);
        testGuessSingle(x);
        testGuessDuplication(x);
        testDuplicationWithoutAggregation(x);
    }

    private static <T> void testDefinedWithSingleValue(CustomFactory<T> x, TsUnit unit) {
        double single = .1;

        ObsGathering g = ObsGathering.DEFAULT.withUnit(unit);

        TsDataBuilder<T> b = x.builder(g)
                .add(x.date(START), single);

        assertBuild(b, data(unit, START, single));
    }

    private static <T> void testDefinedWithMissingValues(CustomFactory<T> x, TsUnit unit) {
        double first = .1, second = .2;

        ObsGathering g = ObsGathering.DEFAULT.withUnit(unit);

        TsDataBuilder<T> b = x.builder(g)
                .add(x.date(START), first)
                .add(x.date(START.plus(unit).plus(unit)), second);

        if (x.supports(unit)) {
            assertBuild(b, data(unit, START, first, Double.NaN, second));
        } else {
            assertBuild(b, DUPLICATION_WITHOUT_AGGREGATION);
        }
    }

    private static <T> void testDefinedWithAggregation(CustomFactory<T> x, TsUnit unit) {
        double v1 = .12, v2 = .13, v3 = .10, v4 = .11;

        BiFunction<TsUnit, AggregationType, TsDataBuilder<T>> b6 = (f, a) -> {
            return x.builder(ObsGathering.builder().unit(f).aggregationType(a).build())
                    .add(x.date(START), v1)
                    .add(x.date(START), v2)
                    .add(x.date(START), v3)
                    .add(x.date(START), v4);
        };

        assertBuild(b6.apply(unit, First), data(unit, START, v1));
        assertBuild(b6.apply(unit, Last), data(unit, START, v4));
        assertBuild(b6.apply(unit, Min), data(unit, START, v3));
        assertBuild(b6.apply(unit, Max), data(unit, START, v2));
        assertBuild(b6.apply(unit, Average), data(unit, START, (v1 + v2 + v3 + v4) / 4));
        assertBuild(b6.apply(unit, Sum), data(unit, START, (v1 + v2 + v3 + v4)));
    }

    private static <T> void testUndefinedToDefined(CustomFactory<T> x, TsUnit unit) {
        double first = .1, second = .2;

        ObsGathering g = ObsGathering.DEFAULT;

        TsDataBuilder<T> b = x.builder(g)
                .add(x.date(START), first)
                .add(x.date(START.plus(unit)), second);

        if (x.supports(unit)) {
            assertBuild(b, data(unit, START, first, second));
        } else {
            assertBuild(b, GUESS_DUPLICATION);
        }
    }

    private static <T> void testUndefinedToDefinedWithMissingValues(CustomFactory<T> x, TsUnit unit) {
        double v1 = .1, v3 = .3;

        ObsGathering g = ObsGathering.DEFAULT;

        TsDataBuilder<T> b = x.builder(g)
                .add(x.date(START), v1)
                .add(x.date(START.plus(unit).plus(unit)), v3);

        if (x.supports(unit)) {
            assertBuild(b, data(unit, START, v1, Double.NaN, v3));
        } else {
            assertBuild(b, GUESS_DUPLICATION);
        }
    }

    private static <T> void testUnorderedDailyToMonthly(CustomFactory<T> x) {
        Function<AggregationType, OptionalTsData> b7 = a -> {
            return x.builder(ObsGathering.builder().unit(MONTHLY).aggregationType(a).build())
                    .add(x.date(LocalDate.of(2010, 2, 1).atStartOfDay()), 20)
                    .add(x.date(LocalDate.of(2010, 1, 3).atStartOfDay()), 10)
                    .add(x.date(LocalDate.of(2010, 1, 4).atStartOfDay()), 11)
                    .add(x.date(LocalDate.of(2010, 1, 1).atStartOfDay()), 12)
                    .add(x.date(LocalDate.of(2010, 1, 2).atStartOfDay()), 13)
                    .build();
        };
        assertThat(b7.apply(First)).isEqualTo(data(MONTHLY, 2010, 12, 20));
        assertThat(b7.apply(Last)).isEqualTo(data(MONTHLY, 2010, 11, 20));
        assertThat(b7.apply(Min)).isEqualTo(data(MONTHLY, 2010, 10, 20));
        assertThat(b7.apply(Max)).isEqualTo(data(MONTHLY, 2010, 13, 20));
        assertThat(b7.apply(Average)).isEqualTo(data(MONTHLY, 2010, 46d / 4, 20));
        assertThat(b7.apply(Sum)).isEqualTo(data(MONTHLY, 2010, 46, 20));
    }

    private static <T> void testNoData(CustomFactory<T> o) {
        TsDataBuilderUtil.ALL_UNITS.forEach(unit -> {

            ObsGathering g = ObsGathering.DEFAULT.withUnit(unit);

            TsDataBuilder<T> b = o.builder(g);

            assertBuild(b, NO_DATA);
        });
    }

    private static <T> void testInvalidAggregation(CustomFactory<T> o) {
        complementOf(of(None)).forEach(aggregationType -> {

            ObsGathering g = ObsGathering.DEFAULT.withAggregationType(aggregationType);

            TsDataBuilder<T> b = o.builder(g)
                    .add(o.date(START), 10);

            assertBuild(b, INVALID_AGGREGATION);
        });
    }

    private static <T> void testGuessSingle(CustomFactory<T> o) {
        ObsGathering g = ObsGathering.DEFAULT;

        TsDataBuilder<T> b = o.builder(g)
                .add(o.date(START), 10);

        assertBuild(b, GUESS_SINGLE);
    }

    private static <T> void testGuessDuplication(CustomFactory<T> o) {
        ObsGathering g = ObsGathering.DEFAULT;

        TsDataBuilder<T> b = o.builder(g)
                .add(o.date(START), 10)
                .add(o.date(START), 20);

        assertBuild(b, GUESS_DUPLICATION);
    }

    private static <T> void testDuplicationWithoutAggregation(CustomFactory<T> o) {
        TsDataBuilderUtil.DEFINED_UNITS.forEach(unit -> {

            ObsGathering g = ObsGathering.DEFAULT.withUnit(unit);

            TsDataBuilder<T> b = o.builder(g)
                    .add(o.date(START), 10)
                    .add(o.date(START), 20);

            assertBuild(b, DUPLICATION_WITHOUT_AGGREGATION);
        });
    }

    private static void assertBuild(TsDataBuilder<?> builder, OptionalTsData data) {
        assertThat(builder.build())
                .isEqualTo(data)
                .isEqualTo(builder.build());
    }

    private interface CustomFactory<T> {

        TsDataBuilder<T> builder(ObsGathering gathering);

        T date(LocalDateTime o);

        boolean supports(TsUnit unit);
    }

    private static OptionalTsData data(TsUnit unit, LocalDateTime date, double... values) {
        return present(TsData.of(TsPeriod.of(unit, date), DoubleSequence.ofInternal(values)));
    }

    private static OptionalTsData data(TsUnit unit, int year, double... values) {
        return data(unit, LocalDate.of(year, 1, 1).atStartOfDay(), values);
    }

    private static final LocalDateTime START = LocalDateTime.of(2010, 1, 1, 0, 0);
}
