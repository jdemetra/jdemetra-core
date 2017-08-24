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

import demetra.design.VisibleForTesting;
import demetra.data.AggregationType;
import demetra.timeseries.Fixme;
import demetra.timeseries.TsFrequency;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataConverter;
import demetra.tsprovider.util.ObsCharacteristics;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.OptionalTsData;
import static demetra.tsprovider.OptionalTsData.present;
import demetra.tsprovider.util.TsDataBuilder;
import demetra.utilities.functions.ObjLongToIntFunction;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalTsDataBuilder {

    @VisibleForTesting
    public static final OptionalTsData NO_DATA = OptionalTsData.absent("No data available");
    @VisibleForTesting
    public static final OptionalTsData INVALID_AGGREGATION = OptionalTsData.absent("Invalid aggregation mode");
    @VisibleForTesting
    public static final OptionalTsData GUESS_SINGLE = OptionalTsData.absent("Cannot guess frequency with a single observation");
    @VisibleForTesting
    public static final OptionalTsData GUESS_DUPLICATION = OptionalTsData.absent("Cannot guess frequency with duplicated periods");
    @VisibleForTesting
    public static final OptionalTsData DUPLICATION_WITHOUT_AGGREGATION = OptionalTsData.absent("Duplicated observations without aggregation");
    @VisibleForTesting
    public static final OptionalTsData UNKNOWN = OptionalTsData.absent("Unexpected error");

    private static final class UndefinedWithAggregation<T> implements TsDataBuilder<T> {

        @Override
        public TsDataBuilder<T> clear() {
            return this;
        }

        @Override
        public TsDataBuilder<T> add(T date, Number value) {
            return this;
        }

        @Override
        public OptionalTsData build() {
            return INVALID_AGGREGATION;
        }
    }

    private static final class BuilderSupport<T> implements TsDataBuilder<T> {

        private final ObsList.LongObsList obs;
        private final ToLongFunction<T> periodFunc;
        private final boolean skipMissingValues;
        private final Function<ObsList, OptionalTsData> maker;

        BuilderSupport(
                ObsList.LongObsList obs,
                ToLongFunction<T> periodFunc,
                boolean skipMissingValues,
                Function<ObsList, OptionalTsData> maker) {
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
    }

    public static <T> TsDataBuilder<T> builder(
            ObsGathering gathering, @Nonnull Set<ObsCharacteristics> characteristics,
            ToLongFunction<T> periodFunc, ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc) {
        boolean ordered = characteristics.contains(ObsCharacteristics.ORDERED);
        if (gathering.getFrequency().equals(Fixme.Undefined)) {
            if (gathering.getAggregationType() != AggregationType.None) {
                return new UndefinedWithAggregation<>();
            }
            return new BuilderSupport<>(
                    ObsList.newLongObsList(ordered, tsPeriodIdFunc),
                    periodFunc,
                    gathering.isSkipMissingValues(),
                    o -> makeFromUnknownFrequency(o));
        }
        if (gathering.getAggregationType() != AggregationType.None) {
            return new BuilderSupport<>(
                    ObsList.newLongObsList(ordered, tsPeriodIdFunc),
                    periodFunc,
                    gathering.isSkipMissingValues(),
                    o -> makeWithAggregation(o, gathering.getFrequency(), gathering.getAggregationType()));
        }
        return new BuilderSupport<>(
                ObsList.newLongObsList(ordered, tsPeriodIdFunc),
                periodFunc,
                gathering.isSkipMissingValues(),
                o -> makeWithoutAggregation(o, gathering.getFrequency()));
    }

    private static OptionalTsData makeFromUnknownFrequency(ObsList obs) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            case 1:
                return GUESS_SINGLE;
            default:
                TsData result = TsDataCollector.makeFromUnknownFrequency(obs);
                return result != null ? present(result) : GUESS_DUPLICATION;
        }
    }

    private static OptionalTsData makeWithoutAggregation(ObsList obs, TsFrequency freq) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeWithoutAggregation(obs, freq);
                return result != null ? present(result) : DUPLICATION_WITHOUT_AGGREGATION;
        }
    }

    private static OptionalTsData makeWithAggregation(ObsList obs, TsFrequency freq, AggregationType convMode) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeFromUnknownFrequency(obs);
                if (result != null && (Fixme.getAsInt(result.getFrequency()) % Fixme.getAsInt(freq) == 0)) {
                    // should succeed
                    result = TsDataConverter.changeFrequency(result, freq, convMode, true);
                } else {
                    result = TsDataCollector.makeWithAggregation(obs, freq, convMode);
                }
                return result != null ? present(result) : UNKNOWN;
        }
    }

    public static int getIdFromTimeInMillis(Calendar cal, TsFrequency freq, long period) {
        cal.setTimeInMillis(period);
        return calcTsPeriodId(Fixme.getAsInt(freq), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    public static long getYearMonthDay(LocalDate date) {
        return (long) (date.getYear() * 100 + date.getMonthValue()) * 100 + date.getDayOfMonth();
    }

    public static int getIdFromYearMonthDay(TsFrequency freq, long period) {
        period = period / 100;
        return calcTsPeriodId(Fixme.getAsInt(freq), (int) (period / 100), (int) (period % 100 - 1));
    }

    private static int calcTsPeriodId(int freq, int year, int month) {
        return (year - 1970) * freq + month / (12 / freq);
    }

    public static EnumSet<ObsCharacteristics> toEnumSet(ObsCharacteristics[] items) {
        switch (items.length) {
            case 0:
                return EnumSet.noneOf(ObsCharacteristics.class);
            case 1:
                return EnumSet.of(items[0]);
            default:
                return EnumSet.copyOf(Arrays.asList(items));
        }
    }
}
