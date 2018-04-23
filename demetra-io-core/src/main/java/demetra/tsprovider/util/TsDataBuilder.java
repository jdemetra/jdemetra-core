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

import demetra.design.IBuilder;
import demetra.timeseries.TsData;
import internal.tsprovider.util.ByLongDataBuilder;
import internal.tsprovider.util.ByObjDataBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Builder that collects observations in order to create an OptionalTsData.
 *
 * @param <DATE>
 * @since 2.2.0
 */
@NotThreadSafe
public interface TsDataBuilder<DATE> extends IBuilder<TsData> {

    /**
     * Removes all observations.
     *
     * @return this builder
     */
    @Nonnull
    TsDataBuilder<DATE> clear();

    /**
     * Adds an observation by using a date. This date belongs to the considered
     * period (it is not retained in the final time series).
     * <p>
     * An observation with <code>null</code> date is ignored and
     * <code>null</code> value is considered as missing.
     *
     * @param date an optional date
     * @param value an optional value
     * @return this builder
     */
    @Nonnull
    TsDataBuilder<DATE> add(@Nullable DATE date, @Nullable Number value);

    /**
     * Adds an observation by using a date.
     *
     * @param <X> the observation type
     * @param obs the non-null observation to add
     * @param dateFunc a non-null function that retrieves a date from an
     * observation
     * @param valueFunc a non-null function that retrieves a value from an
     * observation
     * @return this builder
     */
    @Nonnull
    default <X> TsDataBuilder<DATE> add(
            @Nullable X obs,
            @Nonnull Function<? super X, ? extends DATE> dateFunc,
            @Nonnull Function<? super X, ? extends Number> valueFunc) {
        DATE date = dateFunc.apply(obs);
        return TsDataBuilder.this.add(date, date != null ? valueFunc.apply(obs) : null);
    }

    /**
     * Adds a stream of observations by using a date.
     *
     * @param <X> the observation type
     * @param stream the non-null stream to add
     * @param dateFunc a non-null function that retrieves a date from an
     * observation
     * @param valueFunc a non-null function that retrieves a value from an
     * observation
     * @return this builder
     */
    @Nonnull
    default <X> TsDataBuilder<DATE> addAll(
            @Nonnull Stream<X> stream,
            @Nonnull Function<? super X, ? extends DATE> dateFunc,
            @Nonnull Function<? super X, ? extends Number> valueFunc) {
        stream.forEach(o -> add(o, dateFunc, valueFunc));
        return this;
    }

    /**
     * Creates an TsData from the collected observations.
     *
     * @return a non-null OptionalTsData
     */
    @Nonnull
    @Override
    TsData build();

    /**
     * Creates an TsData builder that collects {@link Date} values.
     *
     * @param resource non-null resource used to handle dates quirks such as
     * time zones
     * @param gathering non-null observation collection parameters
     * @param characteristics non-null observations characteristics
     * @return non-null builder
     */
    @Nonnull
    static TsDataBuilder<Date> byCalendar(@Nonnull Calendar resource, @Nonnull ObsGathering gathering, @Nonnull ObsCharacteristics... characteristics) {
        return ByLongDataBuilder.fromCalendar(resource, gathering, characteristics);
    }

    /**
     * Creates an TsData builder that collects {@link LocalDate} values.
     *
     * @param gathering non-null observation collection parameters
     * @param characteristics non-null observations characteristics
     * @return non-null builder
     */
    @Nonnull
    static TsDataBuilder<LocalDate> byDate(@Nonnull ObsGathering gathering, @Nonnull ObsCharacteristics... characteristics) {
        return ByLongDataBuilder.fromDate(gathering, characteristics);
    }

    /**
     * Creates an TsData builder that collects {@link LocalDateTime}
     * values.
     *
     * @param gathering non-null observation collection parameters
     * @param characteristics non-null observations characteristics
     * @return non-null builder
     */
    @Nonnull
    static TsDataBuilder<LocalDateTime> byDateTime(@Nonnull ObsGathering gathering, @Nonnull ObsCharacteristics... characteristics) {
        return ByObjDataBuilder.fromDateTime(gathering, characteristics);
    }
}
