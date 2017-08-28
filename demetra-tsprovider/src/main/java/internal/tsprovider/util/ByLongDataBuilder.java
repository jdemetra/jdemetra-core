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

import demetra.timeseries.Fixme;
import demetra.timeseries.TsFrequency;
import demetra.tsprovider.OptionalTsData;
import demetra.tsprovider.util.ObsCharacteristics;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.util.TsDataBuilder;
import demetra.utilities.functions.ObjLongToIntFunction;
import java.time.LocalDate;
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
                ? of(gathering, characteristics, Date::getTime, (f, p) -> getIdFromTimeInMillis(resource, f, p))
                : new NoOpDataBuilder<>(TsDataBuilderUtil.INVALID_AGGREGATION);
    }

    public static TsDataBuilder<LocalDate> fromDate(ObsGathering gathering, ObsCharacteristics[] characteristics) {
        return TsDataBuilderUtil.isValid(gathering)
                ? of(gathering, characteristics, ByLongDataBuilder::getYearMonthDay, ByLongDataBuilder::getIdFromYearMonthDay)
                : new NoOpDataBuilder<>(TsDataBuilderUtil.INVALID_AGGREGATION);
    }

    private final ByLongObsList obs;
    private final ToLongFunction<T> periodFunc;
    private final boolean skipMissingValues;
    private final Function<ObsList, OptionalTsData> maker;

    private ByLongDataBuilder(ByLongObsList obs, ToLongFunction<T> periodFunc, boolean skipMissingValues, Function<ObsList, OptionalTsData> maker) {
        this.obs = obs;
        this.periodFunc = periodFunc;
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
                obs.add(periodFunc.applyAsLong(date), value.doubleValue());
            } else if (!skipMissingValues) {
                obs.add(periodFunc.applyAsLong(date), Double.NaN);
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
            ToLongFunction<T> periodFunc, ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc) {

        return new ByLongDataBuilder<>(
                ByLongObsList.of(isOrdered(characteristics), tsPeriodIdFunc),
                periodFunc,
                gathering.isSkipMissingValues(),
                TsDataBuilderUtil.getMaker(gathering));
    }

    private static boolean isOrdered(ObsCharacteristics[] characteristics) {
        return Arrays.binarySearch(characteristics, ObsCharacteristics.ORDERED) != -1;
    }

    private static int getIdFromTimeInMillis(Calendar cal, TsFrequency freq, long period) {
        cal.setTimeInMillis(period);
        return calcTsPeriodId(Fixme.getAsInt(freq), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    private static long getYearMonthDay(LocalDate date) {
        return (long) (date.getYear() * 100 + date.getMonthValue()) * 100 + date.getDayOfMonth();
    }

    private static int getIdFromYearMonthDay(TsFrequency freq, long period) {
        period = period / 100;
        return calcTsPeriodId(Fixme.getAsInt(freq), (int) (period / 100), (int) (period % 100 - 1));
    }

    private static int calcTsPeriodId(int freq, int year, int month) {
        return (year - 1970) * freq + month / (12 / freq);
    }
}
