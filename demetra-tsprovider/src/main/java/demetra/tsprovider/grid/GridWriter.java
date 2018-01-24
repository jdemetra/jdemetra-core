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

import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsDomain;
import demetra.tsprovider.util.ObsFormat;
import internal.tsprovider.grid.InternalValueWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridWriter {

    @Nonnull
    public static GridWriter of(@Nonnull GridExport options, @Nonnull GridInfo info) {
        ValueWriters writers = ValueWriters.of(info, options.getFormat());
        return new GridWriter(writers, options.getLayout(), options.getHeader().hasName(), options.getHeader().hasDate());
    }

    private final ValueWriters writers;
    private final GridLayout layout;
    private final boolean hasName;
    private final boolean hasDate;

    public void write(@Nonnull TsCollectionGrid col, @Nonnull GridOutput output) throws IOException {
        TsDataTable table = TsDataTable.of(col.getItems().stream().map(TsGrid::getData).collect(Collectors.toList()));

        if (table.getDomain().isEmpty()) {
            return;
        }

        IntFunction<String> names = getNames(col);
        IntFunction<LocalDateTime> dates = getDates(table.getDomain());
        TsDataTable.Cursor cursor = table.cursor(getDistribution(col));

        switch (layout) {
            case VERTICAL:
            case UNKNOWN:
                writePeriodByRow(cursor, names, dates, output);
                break;
            case HORIZONTAL:
                writeSeriesByRow(cursor, names, dates, output);
                break;
        }
    }

    private void writePeriodByRow(TsDataTable.Cursor c, IntFunction<String> names, IntFunction<LocalDateTime> dates, GridOutput output) throws IOException {
        int row = 0;

        if (hasName) {
            int column = hasDate ? 1 : 0;
            for (int series = 0; series < c.getSeriesCount(); series++) {
                writers.writeString(output, row, column, names.apply(series));
                column++;
            }
            row++;
        }

        for (int period = 0; period < c.getPeriodCount(); period++) {
            int column = 0;
            if (hasDate) {
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

    private void writeSeriesByRow(TsDataTable.Cursor c, IntFunction<String> names, IntFunction<LocalDateTime> dates, GridOutput output) throws IOException {
        int row = 0;

        if (hasDate) {
            int column = hasName ? 1 : 0;
            for (int period = 0; period < c.getPeriodCount(); period++) {
                writers.writeDateTime(output, row, column, dates.apply(period));
                column++;
            }
            row++;
        }

        for (int series = 0; series < c.getSeriesCount(); series++) {
            int column = 0;
            if (hasName) {
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

    private static IntFunction<String> getNames(TsCollectionGrid col) {
        return series -> col.getItems().get(series).getName();
    }

    private static IntFunction<LocalDateTime> getDates(TsDomain domain) {
        return series -> domain.get(series).start();
    }

    private static IntFunction<TsDataTable.DistributionType> getDistribution(TsCollectionGrid col) {
        return series -> TsDataTable.DistributionType.FIRST;
    }

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ValueWriters {

        static ValueWriters of(GridInfo info, ObsFormat format) {
            boolean stringSupported = info.isSupportedDataType(String.class);

            InternalValueWriter<String> string = stringSupported ? InternalValueWriter.onString() : InternalValueWriter.onNull();

            InternalValueWriter<LocalDateTime> dateTime
                    = info.isSupportedDataType(LocalDateTime.class)
                    ? InternalValueWriter.onDateTime()
                    : (stringSupported ? InternalValueWriter.onStringFormatter(format.dateTimeFormatter()) : InternalValueWriter.onNull());

            InternalValueWriter<Number> number
                    = info.isSupportedDataType(Number.class)
                    ? InternalValueWriter.onNumber()
                    : (stringSupported ? InternalValueWriter.onStringFormatter(format.numberFormatter()) : InternalValueWriter.onNull());

            return new ValueWriters(string, dateTime, number);
        }

        private final InternalValueWriter<String> string;
        private final InternalValueWriter<LocalDateTime> dateTime;
        private final InternalValueWriter<Number> number;

        public void writeString(GridOutput grid, int row, int column, String value) throws IOException {
            string.write(grid, row, column, value);
        }

        public void writeDateTime(GridOutput grid, int row, int column, LocalDateTime value) throws IOException {
            dateTime.write(grid, row, column, value);
        }

        public void writeNumber(GridOutput grid, int row, int column, Number value) throws IOException {
            number.write(grid, row, column, value);
        }
    }
}
