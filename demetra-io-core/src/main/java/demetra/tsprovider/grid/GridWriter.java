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
package demetra.tsprovider.grid;

import demetra.design.LombokWorkaround;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsDomain;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.util.ObsFormat;
import internal.tsprovider.grid.InternalValueWriter;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class GridWriter {

    public static final GridWriter DEFAULT = builder().build();

    @lombok.NonNull
    private ObsFormat format;

    @lombok.NonNull
    private GridLayout layout;

    private boolean includeNames;

    private boolean includeDates;

    @LombokWorkaround
    public static Builder builder() {
        Builder result = new Builder();
        result.format = ObsFormat.DEFAULT;
        result.layout = GridLayout.VERTICAL;
        result.includeNames = true;
        result.includeDates = true;
        return result;
    }

    public void write(@Nonnull TsCollection col, @Nonnull GridOutput output) {
        ValueWriters writers = ValueWriters.of(
                output::isSupportedDataType,
                () -> format.dateTimeFormatter()::formatAsString,
                () -> format.numberFormatter()::formatAsString
        );

        output.setName(col.getName());

        TsDataTable table = TsDataTable.of(col.getData(), Ts::getData);

        if (table.getDomain().isEmpty()) {
            return;
        }

        IntFunction<String> names = getNames(col);
        IntFunction<LocalDateTime> dates = getDates(table.getDomain());
        TsDataTable.Cursor cursor = table.cursor(getDistribution(col));

        switch (layout) {
            case VERTICAL:
            case UNKNOWN:
                writePeriodByRow(cursor, names, dates, output, writers);
                break;
            case HORIZONTAL:
                writeSeriesByRow(cursor, names, dates, output, writers);
                break;
        }
    }

    private void writePeriodByRow(TsDataTable.Cursor c, IntFunction<String> names, IntFunction<LocalDateTime> dates, GridOutput output, ValueWriters writers) {
        int row = 0;

        if (includeNames) {
            int column = includeDates ? 1 : 0;
            for (int series = 0; series < c.getSeriesCount(); series++) {
                writers.writeString(output, row, column, names.apply(series));
                column++;
            }
            row++;
        }

        for (int period = 0; period < c.getPeriodCount(); period++) {
            int column = 0;
            if (includeDates) {
                writers.writeDateTime(output, row, column, dates.apply(period));
                column++;
            }
            for (int series = 0; series < c.getSeriesCount(); series++) {
                c.moveTo(period, series);
                if (c.getStatus() == TsDataTable.ValueStatus.PRESENT) {
                    writers.writeNumber(output, row, column, nanToNull(c.getValue()));
                }
                column++;
            }
            row++;
        }
    }

    private void writeSeriesByRow(TsDataTable.Cursor c, IntFunction<String> names, IntFunction<LocalDateTime> dates, GridOutput output, ValueWriters writers) {
        int row = 0;

        if (includeDates) {
            int column = includeNames ? 1 : 0;
            for (int period = 0; period < c.getPeriodCount(); period++) {
                writers.writeDateTime(output, row, column, dates.apply(period));
                column++;
            }
            row++;
        }

        for (int series = 0; series < c.getSeriesCount(); series++) {
            int column = 0;
            if (includeNames) {
                writers.writeString(output, row, column, names.apply(series));
                column++;
            }
            for (int period = 0; period < c.getPeriodCount(); period++) {
                c.moveTo(period, series);
                if (c.getStatus() == TsDataTable.ValueStatus.PRESENT) {
                    writers.writeNumber(output, row, column, nanToNull(c.getValue()));
                }
                column++;
            }
            row++;
        }
    }

    private static Number nanToNull(double value) {
        return Double.isNaN(value) ? null : value;
    }

    private static IntFunction<String> getNames(TsCollection col) {
        return series -> col.getData().get(series).getName();
    }

    private static IntFunction<LocalDateTime> getDates(TsDomain domain) {
        return series -> domain.get(series).start();
    }

    private static IntFunction<TsDataTable.DistributionType> getDistribution(TsCollection col) {
        return series -> TsDataTable.DistributionType.FIRST;
    }

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ValueWriters {

        static ValueWriters of(
                Predicate<Class<?>> isSupportedDataType,
                Supplier<Function<LocalDateTime, String>> dateTimeFormatter,
                Supplier<Function<Number, String>> numberFormatter) {

            boolean stringSupported = isSupportedDataType.test(String.class);

            InternalValueWriter<String> string = stringSupported ? InternalValueWriter.onString() : InternalValueWriter.onNull();

            InternalValueWriter<LocalDateTime> dateTime
                    = isSupportedDataType.test(LocalDateTime.class)
                    ? InternalValueWriter.onDateTime()
                    : (stringSupported ? InternalValueWriter.onStringFormatter(dateTimeFormatter.get()) : InternalValueWriter.onNull());

            InternalValueWriter<Number> number
                    = isSupportedDataType.test(Number.class)
                    ? InternalValueWriter.onNumber()
                    : (stringSupported ? InternalValueWriter.onStringFormatter(numberFormatter.get()) : InternalValueWriter.onNull());

            return new ValueWriters(string, dateTime, number);
        }

        private final InternalValueWriter<String> string;
        private final InternalValueWriter<LocalDateTime> dateTime;
        private final InternalValueWriter<Number> number;

        public void writeString(GridOutput grid, int row, int column, String value) {
            string.write(grid, row, column, value);
        }

        public void writeDateTime(GridOutput grid, int row, int column, LocalDateTime value) {
            dateTime.write(grid, row, column, value);
        }

        public void writeNumber(GridOutput grid, int row, int column, Number value) {
            number.write(grid, row, column, value);
        }
    }
}
