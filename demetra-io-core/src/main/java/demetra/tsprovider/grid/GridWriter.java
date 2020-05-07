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
import demetra.design.MightBePromoted;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsDomain;
import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.tsprovider.util.ObsFormat;
import internal.tsprovider.grid.InternalValueWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import org.checkerframework.checker.nullness.qual.NonNull;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    private boolean ignoreNames;

    private boolean ignoreDates;

    private String cornerLabel;

    private boolean reverseChronology;

    // TODO
    private Function<TsCollection, TsDataTable.DistributionType> colX = col -> TsDataTable.DistributionType.FIRST;

    // TODO
    private Function<Ts, TsDataTable.DistributionType> seriesX = series -> TsDataTable.DistributionType.FIRST;

    @LombokWorkaround
    public static Builder builder() {
        Builder result = new Builder();
        result.format = ObsFormat.DEFAULT;
        result.layout = GridLayout.UNDEFINED;
        result.ignoreNames = false;
        result.ignoreDates = false;
        result.cornerLabel = null;
        result.reverseChronology = false;
        return result;
    }

    public void write(@NonNull TsCollection input, @NonNull GridOutput output) throws IOException {
        TsDataTable table = TsDataTable.of(input.getData(), Ts::getData);

        if (table.getDomain().isEmpty()) {
            try (GridOutput.Stream stream = output.open(input.getName(), 0, 0)) {
            }
            return;
        }

        boolean seriesByRow = isSeriesByRow(input);

        int rows = getLength(table, seriesByRow);
        int columns = getLength(table, !seriesByRow);

        IntFunction<String> names = getNames(input.getData());
        IntFunction<LocalDateTime> dates = getDates(table.getDomain());

        TsDataTable.Cursor cursor = table.cursor(getDistribution(input.getData()));

        try (TypedOutputStream stream = TypedOutputStream.of(output.getDataTypes(), format, output.open(input.getName(), rows, columns))) {
            if (seriesByRow) {
                writeSeriesByRow(cursor, stream, names, dates);
            } else {
                writePeriodByRow(cursor, stream, names, dates);
            }
        }
    }

    private boolean isSeriesByRow(TsCollection col) {
        return layout.equals(GridLayout.HORIZONTAL) || peekSeriesByRow(col);
    }

    private int getLength(TsDataTable table, boolean data) {
        return data
                ? (table.getData().size() + (!ignoreDates ? 1 : 0))
                : (table.getDomain().length() + (!ignoreNames ? 1 : 0));
    }

    private PrimitiveIterator.OfInt getPeriodIterator(TsDataTable.Cursor input) {
        return reverseChronology
                ? reverseRange(0, input.getPeriodCount()).iterator()
                : IntStream.range(0, input.getPeriodCount()).iterator();
    }

    private void writePeriodByRow(TsDataTable.Cursor input, TypedOutputStream output, IntFunction<String> names, IntFunction<LocalDateTime> dates) throws IOException {
        writePeriodByRowHead(input, output, names);
        writePeriodByRowBody(input, output, dates);
    }

    private void writePeriodByRowHead(TsDataTable.Cursor input, TypedOutputStream output, IntFunction<String> names) throws IOException {
        if (!ignoreNames) {
            if (!ignoreDates) {
                output.writeString(cornerLabel);
            }
            for (int series = 0; series < input.getSeriesCount(); series++) {
                output.writeString(names.apply(series));
            }
            output.writeEndOfRow();
        }
    }

    private void writePeriodByRowBody(TsDataTable.Cursor input, TypedOutputStream output, IntFunction<LocalDateTime> dates) throws IOException {
        for (PrimitiveIterator.OfInt periods = getPeriodIterator(input); periods.hasNext();) {
            int period = periods.nextInt();
            if (!ignoreDates) {
                output.writeDateTime(dates.apply(period));
            }
            for (int series = 0; series < input.getSeriesCount(); series++) {
                writeValue(input, output, period, series);
            }
            output.writeEndOfRow();
        }
    }

    private void writeSeriesByRow(TsDataTable.Cursor input, TypedOutputStream output, IntFunction<String> names, IntFunction<LocalDateTime> dates) throws IOException {
        writeSeriesByRowHead(input, output, dates);
        writeSeriesByRowBody(input, output, names);
    }

    private void writeSeriesByRowHead(TsDataTable.Cursor input, TypedOutputStream output, IntFunction<LocalDateTime> dates) throws IOException {
        if (!ignoreDates) {
            if (!ignoreNames) {
                output.writeString(cornerLabel);
            }
            for (PrimitiveIterator.OfInt periods = getPeriodIterator(input); periods.hasNext();) {
                int period = periods.nextInt();
                output.writeDateTime(dates.apply(period));
            }
            output.writeEndOfRow();
        }
    }

    private void writeSeriesByRowBody(TsDataTable.Cursor input, TypedOutputStream output, IntFunction<String> names) throws IOException {
        for (int series = 0; series < input.getSeriesCount(); series++) {
            if (!ignoreNames) {
                output.writeString(names.apply(series));
            }
            for (PrimitiveIterator.OfInt periods = getPeriodIterator(input); periods.hasNext();) {
                writeValue(input, output, periods.nextInt(), series);
            }
            output.writeEndOfRow();
        }
    }

    private void writeValue(TsDataTable.Cursor input, TypedOutputStream output, int period, int series) throws IOException {
        input.moveTo(period, series);
        if (input.getStatus() == TsDataTable.ValueStatus.PRESENT) {
            output.writeDouble(nanToNull(input.getValue()));
        } else {
            output.writeDouble(null);
        }
    }

    private Double nanToNull(double value) {
        return Double.isNaN(value) ? null : value;
    }

    private IntFunction<String> getNames(List<Ts> col) {
        return seriesIndex -> col.get(seriesIndex).getName();
    }

    private IntFunction<LocalDateTime> getDates(TsDomain domain) {
        return seriesIndex -> domain.get(seriesIndex).start();
    }

    private IntFunction<TsDataTable.DistributionType> getDistribution(List<Ts> col) {
        return seriesIndex -> seriesX.apply(col.get(seriesIndex));
    }

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TypedOutputStream implements GridOutput.Stream {

        static TypedOutputStream of(Set<GridDataType> dataTypes, ObsFormat format, GridOutput.Stream stream) {
            boolean stringSupported = dataTypes.contains(GridDataType.STRING);

            InternalValueWriter<String> string = stringSupported ? InternalValueWriter.onString() : InternalValueWriter.onNull();

            InternalValueWriter<LocalDateTime> dateTime
                    = dataTypes.contains(GridDataType.LOCAL_DATE_TIME)
                    ? InternalValueWriter.onDateTime()
                    : (stringSupported ? InternalValueWriter.onStringFormatter(format.dateTimeFormatter()::formatAsString) : InternalValueWriter.onNull());

            InternalValueWriter<Double> number
                    = dataTypes.contains(GridDataType.DOUBLE)
                    ? InternalValueWriter.onDouble()
                    : (stringSupported ? InternalValueWriter.onStringFormatter(format.numberFormatter()::formatAsString) : InternalValueWriter.onNull());

            return new TypedOutputStream(string, dateTime, number, stream);
        }

        @lombok.NonNull
        private final InternalValueWriter<String> string;

        @lombok.NonNull
        private final InternalValueWriter<LocalDateTime> dateTime;

        @lombok.NonNull
        private final InternalValueWriter<Double> number;

        @lombok.NonNull
        @lombok.experimental.Delegate
        private final GridOutput.Stream delegate;

        public void writeString(@Nullable String value) throws IOException {
            string.write(delegate, value);
        }

        public void writeDateTime(@Nullable LocalDateTime value) throws IOException {
            dateTime.write(delegate, value);
        }

        public void writeDouble(@Nullable Double value) throws IOException {
            number.write(delegate, value);
        }
    }

    private static boolean peekSeriesByRow(TsCollection col) {
        return GridLayout.HORIZONTAL.name().equals(col.getMeta().get(GridLayout.PROPERTY));
    }

    // https://stackoverflow.com/questions/24010109/java-8-stream-reverse-order/24011264#24011264
    @MightBePromoted
    static IntStream reverseRange(int from, int to) {
        return IntStream.range(from, to).map(i -> to - i + from - 1);
    }
}
