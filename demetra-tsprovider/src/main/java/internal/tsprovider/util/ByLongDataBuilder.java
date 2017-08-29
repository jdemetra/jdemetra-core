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
package internal.tsprovider.util;

import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.tsprovider.OptionalTsData;
import demetra.tsprovider.util.ObsCharacteristics;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.util.TsDataBuilder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public final class ByLongDataBuilder<T> implements TsDataBuilder<T> {

    public static TsDataBuilder<Date> fromCalendar(Calendar resource, ObsGathering gathering, ObsCharacteristics[] characteristics) {
        return TsDataBuilderUtil.isValid(gathering)
                ? of(gathering, characteristics, new CalendarConverter(resource.getTimeZone().toZoneId()))
                : new NoOpDataBuilder<>(TsDataBuilderUtil.INVALID_AGGREGATION);
    }

    public static TsDataBuilder<LocalDate> fromDate(ObsGathering gathering, ObsCharacteristics[] characteristics) {
        return TsDataBuilderUtil.isValid(gathering)
                ? of(gathering, characteristics, DateConverter.INSTANCE)
                : new NoOpDataBuilder<>(TsDataBuilderUtil.INVALID_AGGREGATION);
    }

    private final ByLongObsList obs;
    private final ToLongFunction<T> toLong;
    private final boolean skipMissingValues;
    private final Function<ObsList, OptionalTsData> maker;

    private ByLongDataBuilder(ByLongObsList obs, ToLongFunction<T> toLong, boolean skipMissingValues, Function<ObsList, OptionalTsData> maker) {
        this.obs = obs;
        this.toLong = toLong;
        this.skipMissingValues = skipMissingValues;
        this.maker = maker;
    }

    @Override
    public TsDataBuilder<T> clear() {
        obs.clear();
        return this;
    }

    @Override
    public TsDataBuilder<T> add(T date, Number value) {
        if (date != null) {
            if (value != null) {
                obs.add(toLong.applyAsLong(date), value.doubleValue());
            } else if (!skipMissingValues) {
                obs.add(toLong.applyAsLong(date), Double.NaN);
            }
        }
        return this;
    }

    @Override
    public OptionalTsData build() {
        return maker.apply(obs);
    }

    private static <T> ByLongDataBuilder<T> of(
            ObsGathering gathering, ObsCharacteristics[] characteristics,
            Converter<T> converter) {

        return new ByLongDataBuilder<>(
                ByLongObsList.of(isOrdered(characteristics), converter::longToPeriodId),
                converter::valueToLong,
                gathering.isSkipMissingValues(),
                TsDataBuilderUtil.getMaker(gathering));
    }

    private static boolean isOrdered(ObsCharacteristics[] characteristics) {
        return Arrays.binarySearch(characteristics, ObsCharacteristics.ORDERED) != -1;
    }

    private interface Converter<T> {

        long valueToLong(T value);

        int longToPeriodId(TsUnit unit, long l);
    }

    @lombok.AllArgsConstructor
    private static final class CalendarConverter implements Converter<Date> {

        private final ZoneId zoneId;

        @Override
        public long valueToLong(Date value) {
            return value.getTime();
        }

        @Override
        public int longToPeriodId(TsUnit unit, long l) {
            return (int) TsPeriod.idAt(0, unit, toLocalDateTime(l));
        }

        private LocalDateTime toLocalDateTime(long l) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(l), zoneId);
        }
    }

    private enum DateConverter implements Converter<LocalDate> {
        INSTANCE;

        @Override
        public long valueToLong(LocalDate date) {
            return (long) (date.getYear() * 100 + date.getMonthValue()) * 100 + date.getDayOfMonth();
        }

        @Override
        public int longToPeriodId(TsUnit unit, long l) {
            return (int) TsPeriod.idAt(0, unit, toLocalDateTime(l));
        }

        private static LocalDateTime toLocalDateTime(long value) {
            int dayOfMonth = (int) value % 100;
            value /= 100;
            int month = (int) value % 100;
            value /= 100;
            return LocalDateTime.of((int) value, month, dayOfMonth, 0, 0);
        }
    }
}
