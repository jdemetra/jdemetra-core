/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders.db;

import com.google.common.collect.ImmutableList;
import ec.tss.tsproviders.utils.ObsCharacteristics;
import ec.tss.tsproviders.utils.ObsGathering;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
public final class DbUtil {

    private DbUtil() {
        // static class
    }

    public interface Func<X, Y, EX extends Exception> {

        @Nullable
        Y apply(@NonNull X input) throws EX;
    }

    public interface Cursor<EX extends Exception> {

        boolean next() throws EX;
    }

    public static abstract class AllSeriesCursor<T extends Exception> implements Cursor<T> {

        public String[] dimValues;
    }

    @NonNull
    public static <T extends Exception> List<DbSetId> getAllSeries(@NonNull AllSeriesCursor<T> cursor, @NonNull DbSetId ref) throws T {
        ImmutableList.Builder<DbSetId> result = ImmutableList.builder();
        while (cursor.next()) {
            result.add(ref.child(cursor.dimValues));
        }
        return result.build();
    }

    public static abstract class AllSeriesWithDataCursor<T extends Exception> implements Cursor<T> {

        public String[] dimValues;
        public java.util.Date period;
        public Number value;
    }

    @NonNull
    public static <T extends Exception> List<DbSeries> getAllSeriesWithData(@NonNull AllSeriesWithDataCursor<T> cursor, @NonNull DbSetId ref, @NonNull TsFrequency frequency, @NonNull TsAggregationType aggregationType) throws T {
        ImmutableList.Builder<DbSeries> result = ImmutableList.builder();
        ObsGathering gathering = ObsGathering.includingMissingValues(frequency, aggregationType);
        OptionalTsData.Builder2<Date> data = OptionalTsData.builderByDate(new GregorianCalendar(), gathering, ObsCharacteristics.ORDERED);
        boolean t0 = cursor.next();
        while (t0) {
            String[] dimValues = cursor.dimValues;
            boolean t1 = true;
            while (t1) {
                Date period = cursor.period;
                Number value = null;
                boolean t2 = true;
                while (t2) {
                    value = cursor.value;
                    t0 = cursor.next();
                    t1 = t0 && Arrays.equals(dimValues, cursor.dimValues);
                    t2 = t1 && Objects.equals(period, cursor.period);
                }
                data.add(period, value);
            }
            result.add(new DbSeries(ref.child(dimValues), data.build()));
            data.clear();
        }
        return result.build();
    }

    public static abstract class SeriesWithDataCursor<T extends Exception> implements Cursor<T> {

        public Date period;
        public Number value;
    }

    @NonNull
    public static <T extends Exception> DbSeries getSeriesWithData(@NonNull SeriesWithDataCursor<T> cursor, @NonNull DbSetId ref, @NonNull TsFrequency frequency, @NonNull TsAggregationType aggregationType) throws T {
        ObsGathering gathering = ObsGathering.includingMissingValues(frequency, aggregationType);
        OptionalTsData.Builder2<Date> data = OptionalTsData.builderByDate(new GregorianCalendar(), gathering, ObsCharacteristics.ORDERED);
        boolean t0 = cursor.next();
        if (t0) {
            Date latestPeriod = cursor.period;
            while (t0) {
                Date period = latestPeriod;
                Number value = null;
                boolean t1 = true;
                while (t1) {
                    value = cursor.value;
                    t0 = cursor.next();
                    t1 = t0 && Objects.equals(period, latestPeriod = cursor.period);
                }
                data.add(period, value);
            }
        }
        return new DbSeries(ref, data.build());
    }

    public static abstract class ChildrenCursor<T extends Exception> implements Cursor<T> {

        public String child;
    }

    @NonNull
    public static <T extends Exception> List<String> getChildren(@NonNull ChildrenCursor<T> cursor) throws T {
        ImmutableList.Builder<String> result = ImmutableList.builder();
        while (cursor.next()) {
            result.add(cursor.child);
        }
        return result.build();
    }
}
