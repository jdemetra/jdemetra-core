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
import demetra.timeseries.TsData;
import internal.tsprovider.util.GuessingUnit;
import static internal.tsprovider.util.TsDataBuilderUtil.DUPLICATION_WITHOUT_AGGREGATION;
import static internal.tsprovider.util.TsDataBuilderUtil.GUESS_DUPLICATION;
import static internal.tsprovider.util.TsDataBuilderUtil.GUESS_SINGLE;
import static internal.tsprovider.util.TsDataBuilderUtil.INVALID_AGGREGATION;
import static internal.tsprovider.util.TsDataBuilderUtil.NO_DATA;
import static java.lang.Double.NaN;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import static java.util.EnumSet.complementOf;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static demetra.timeseries.TsPeriod.DEFAULT_EPOCH;
import static java.util.EnumSet.of;

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
        testBuilder(factory);
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
        testBuilder(factory);
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
        testBuilder(factory);
    }

    @SuppressWarnings("null")
    private static <T> void testBuilderAdd(CustomFactory<T> x) {
        double v1 = .1, v2 = .2;
        Object[][] example = {{x.date(START), v1}, {x.date(START.plusMonths(1)), v2}};
        Function<Object[], T> dateFunc = o -> (T) o[0];
        Function<Object[], Number> valueFunc = o -> (Number) o[1];

        TsDataBuilder<T> b = x.builder(ObsGathering.DEFAULT.withUnit(MONTH).withSkipMissingValues(false));

        assertThat(b.clear().add(null, null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(null, v1).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add((T) example[0][0], null).build()).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, NaN));
        assertThat(b.clear().add((T) example[0][0], v1).build()).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, v1));

        assertThat(b.clear().add(example[0], o -> null, o -> null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(example[0], o -> null, valueFunc).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().add(example[0], dateFunc, o -> null).build()).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, NaN));
        assertThat(b.clear().add(example[0], dateFunc, valueFunc).build()).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, v1));

        assertThat(b.clear().addAll(Stream.of(example), o -> null, o -> null).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().addAll(Stream.of(example), o -> null, valueFunc).build()).isEqualTo(NO_DATA);
        assertThat(b.clear().addAll(Stream.of(example), dateFunc, o -> null).build()).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, NaN, NaN));
        assertThat(b.clear().addAll(Stream.of(example), dateFunc, valueFunc).build()).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, v1, v2));
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

    private static <T> void testBuilder(CustomFactory<T> x) {
        testDefinedUnits(x);
        testUndefinedUnit(x);
        testUnorderedDailyToMonthly(x);
        testNoData(x);
        testInvalidAggregation(x);
        testGuessSingle(x);
        testGuessDuplication(x);
        testDuplicationWithoutAggregation(x);
    }

    private static <T> void testDefinedUnits(CustomFactory<T> x) {
        DEFINED_UNITS.forEach(unit -> {
            testDefinedWithSingleValue(x, unit, DEFAULT_EPOCH);
            testDefinedWithMissingValues(x, unit, DEFAULT_EPOCH);
            testDefinedWithAggregation(x, unit, DEFAULT_EPOCH);
        });
    }

    private static <T> void testUndefinedUnit(CustomFactory<T> x) {
        Stream.of(GuessingUnit.values()).forEach(guess -> {
            testUndefinedToDefined(x, guess);
            testUndefinedToDefinedWithMissingValues(x, guess);
        });
    }

    private static <T> void testDefinedWithSingleValue(CustomFactory<T> x, TsUnit unit, LocalDateTime reference) {
        double single = .1;
        ObsGathering g = ObsGathering.DEFAULT.withUnit(unit);

        forEachDates(unit, reference, start -> {
            TsDataBuilder<T> b = x.builder(g)
                    .add(x.date(start), single);

            assertBuild(b, data(unit, reference, start, single));
        });
    }

    private static <T> void testDefinedWithMissingValues(CustomFactory<T> x, TsUnit unit, LocalDateTime reference) {
        double first = .1, second = .2;
        ObsGathering g = ObsGathering.DEFAULT.withUnit(unit);

        forEachDates(unit, reference, start -> {
            TsDataBuilder<T> b = x.builder(g)
                    .add(x.date(start), first)
                    .add(x.date(start.plus(unit).plus(unit)), second);

            if (x.supports(unit)) {
                assertBuild(b, data(unit, reference, start, first, Double.NaN, second));
            } else {
                assertBuild(b, DUPLICATION_WITHOUT_AGGREGATION);
            }
        });
    }

    private static <T> void testDefinedWithAggregation(CustomFactory<T> x, TsUnit unit, LocalDateTime reference) {
        double v1 = .12, v2 = .13, v3 = .10, v4 = .11;

        forEachDates(unit, reference, start -> {
            BiFunction<TsUnit, AggregationType, TsDataBuilder<T>> b = (f, a) -> {
                return x.builder(ObsGathering.builder().unit(f).aggregationType(a).build())
                        .add(x.date(start), v1)
                        .add(x.date(start), v2)
                        .add(x.date(start), v3)
                        .add(x.date(start), v4);
            };

            assertBuild(b.apply(unit, First), data(unit, reference, start, v1));
            assertBuild(b.apply(unit, Last), data(unit, reference, start, v4));
            assertBuild(b.apply(unit, Min), data(unit, reference, start, v3));
            assertBuild(b.apply(unit, Max), data(unit, reference, start, v2));
            assertBuild(b.apply(unit, Average), data(unit, reference, start, (v1 + v2 + v3 + v4) / 4));
            assertBuild(b.apply(unit, Sum), data(unit, reference, start, (v1 + v2 + v3 + v4)));
        });
    }

    private static <T> void testUndefined(CustomFactory<T> x, GuessingUnit guess, LocalDateTime start, double[] values) {
        ObsGathering g = ObsGathering.DEFAULT;

        TsDataBuilder<T> b = x.builder(g);
        LocalDateTime date = start;
        for (double o : values) {
            b.add(x.date(date), o);
            date = date.plus(guess.getTsUnit());
        }

        if (x.supports(guess.getTsUnit())) {
            assertBuild(b, data(guess.getTsUnit(), guess.getReference(), start, values));
        } else {
            assertBuild(b, GUESS_DUPLICATION);
        }
    }

    private static <T> void testUndefinedToDefined(CustomFactory<T> x, GuessingUnit guess) {
        double[] values = new double[guess.getMinimumObsCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = i / 10d;
        }
        forEachDates(guess.getTsUnit(), guess.getReference(), start -> testUndefined(x, guess, start, values));
    }

    private static <T> void testUndefinedToDefinedWithMissingValues(CustomFactory<T> x, GuessingUnit guess) {
        double[] values = new double[guess.getMinimumObsCount() + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = i == 1 ? Double.NaN : i / 10d;
        }
        forEachDates(guess.getTsUnit(), guess.getReference(), start -> testUndefined(x, guess, start, values));
    }

    private static <T> void testUnorderedDailyToMonthly(CustomFactory<T> x) {
        double v1 = .12, v2 = .13, v3 = .10, v4 = .11;
        double v5 = 20;

        Function<AggregationType, TsData> b7 = a -> {
            return x.builder(ObsGathering.builder().unit(MONTH).aggregationType(a).build())
                    .add(x.date(LocalDate.of(2010, 2, 1).atStartOfDay()), v5)
                    .add(x.date(LocalDate.of(2010, 1, 3).atStartOfDay()), v3)
                    .add(x.date(LocalDate.of(2010, 1, 4).atStartOfDay()), v4)
                    .add(x.date(LocalDate.of(2010, 1, 1).atStartOfDay()), v1)
                    .add(x.date(LocalDate.of(2010, 1, 2).atStartOfDay()), v2)
                    .build();
        };

        assertThat(b7.apply(First)).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, v1, v5));
        assertThat(b7.apply(Last)).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, v4, v5));
        assertThat(b7.apply(Min)).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, v3, v5));
        assertThat(b7.apply(Max)).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, v2, v5));
        assertThat(b7.apply(Average)).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, (v1 + v2 + v3 + v4) / 4, v5));
        assertThat(b7.apply(Sum)).isEqualTo(data(MONTH, DEFAULT_EPOCH, 2010, (v1 + v2 + v3 + v4), v5));
    }

    private static <T> void testNoData(CustomFactory<T> o) {
        ALL_UNITS.forEach(unit -> {

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
        DEFINED_UNITS.forEach(unit -> {

            ObsGathering g = ObsGathering.DEFAULT.withUnit(unit);

            TsDataBuilder<T> b = o.builder(g)
                    .add(o.date(START), 10)
                    .add(o.date(START), 20);

            assertBuild(b, DUPLICATION_WITHOUT_AGGREGATION);
        });
    }

    private static void assertBuild(TsDataBuilder<?> builder, TsData data) {
        assertThat(builder.build())
                .isEqualTo(data)
                .isEqualTo(builder.build());
    }

    private interface CustomFactory<T> {

        TsDataBuilder<T> builder(ObsGathering gathering);

        T date(LocalDateTime o);

        boolean supports(TsUnit unit);
    }

    private static TsData data(TsUnit unit, LocalDateTime reference, LocalDateTime date, double... values) {
        return TsData.of(TsPeriod.builder().unit(unit).epoch(reference).date(date).build(), DoubleSequence.ofInternal(values));
    }

    private static TsData data(TsUnit unit, LocalDateTime reference, int year, double... values) {
        return data(unit, reference, LocalDate.of(year, 1, 1).atStartOfDay(), values);
    }

    private static List<LocalDateTime> dates(TsUnit unit, LocalDateTime reference) {
        TsPeriod startPeriod = TsPeriod.builder().unit(unit).epoch(reference).date(START).build();
        LocalDateTime first = startPeriod.start();
        LocalDateTime middle = first.plus(Duration.between(first, startPeriod.next().start()).dividedBy(2));
        LocalDateTime last = startPeriod.next().start().minusNanos(1);
        return Arrays.asList(first, middle, last);
    }

    private static void forEachDates(TsUnit unit, LocalDateTime reference, Consumer<LocalDateTime> consumer) {
        dates(unit, reference).forEach(consumer);
    }

    private static final LocalDateTime START = LocalDateTime.of(2010, 1, 1, 0, 0);
    private static final List<TsUnit> DEFINED_UNITS = Arrays.asList(YEAR, HALF_YEAR, TsUnit.of(4, ChronoUnit.MONTHS), QUARTER, TsUnit.of(2, ChronoUnit.MONTHS), MONTH, WEEK, DAY, HOUR, MINUTE);
    private static final List<TsUnit> ALL_UNITS = Stream.concat(Stream.of(UNDEFINED), DEFINED_UNITS.stream()).collect(Collectors.toList());
}
