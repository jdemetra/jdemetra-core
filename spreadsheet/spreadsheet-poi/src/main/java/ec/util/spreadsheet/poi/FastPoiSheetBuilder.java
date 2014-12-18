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
package ec.util.spreadsheet.poi;

import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import ec.util.spreadsheet.poi.FastPoiBook.SheetContext;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.poi.ss.usermodel.DateUtil;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philippe Charles
 */
abstract class FastPoiSheetBuilder {

    @Nonnull
    abstract public FastPoiSheetBuilder put(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable String rawStyleIndex);

    @Nonnull
    abstract public Sheet build();

    private static final int CORES = Runtime.getRuntime().availableProcessors();

    @Nonnull
    public static FastPoiSheetBuilder create(@Nonnull String name, @Nonnull FastPoiBook.SheetContext sheetContext, @Nullable String sheetBounds) {
        return CORES > 1 ? new MultiThreadedBuilder(name, sheetContext, sheetBounds) : new Builder(name, sheetContext, sheetBounds);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static class Builder extends FastPoiSheetBuilder {

        private final ArraySheet.Builder valuesBuilder;
        private final CellRefHelper refHelper;
        private final CellValueFactory valueFactory;

        public Builder(@Nonnull String name, @Nonnull FastPoiBook.SheetContext sheetContext, @Nullable String sheetBounds) {
            this.valuesBuilder = ArraySheet.builder(sheetBounds).name(name);
            this.refHelper = new CellRefHelper();
            this.valueFactory = new CellValueFactory(sheetContext);
        }

        @Override
        public Builder put(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable String rawStyleIndex) {
            process(ref, rawValue, rawDataType, rawStyleIndex);
            return this;
        }

        protected void process(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable String rawStyleIndex) {
            Object cellValue = valueFactory.getValue(rawValue.toString(), rawDataType, rawStyleIndex);
            if (cellValue == null || !refHelper.parse(ref)) {
                return;
            }
            valuesBuilder.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), cellValue);
        }

        @Override
        public Sheet build() {
            return valuesBuilder.build();
        }
    }

    private static final class MultiThreadedBuilder extends Builder {

        private final ExecutorService executor;
        private final Queue<QueueItem> queue;
        private final Consumer singleConsumer;
//        int outOfCapacity = 0;

        public MultiThreadedBuilder(@Nonnull String name, @Nonnull FastPoiBook.SheetContext sheetContext, @Nullable String sheetBounds) {
            super(name, sheetContext, sheetBounds);
            this.executor = Executors.newSingleThreadExecutor();
            this.queue = new P1C1QueueOriginal3<>(1000);
            this.singleConsumer = new Consumer<QueueItem>(queue) {
                @Override
                protected void consume(QueueItem item) {
                    process(item.ref, item.rawValue, item.rawDataType, item.rawStyleIndex);
                }
            };
            executor.execute(singleConsumer);
        }

        @Override
        public Builder put(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable String rawStyleIndex) {
            QueueItem item = new QueueItem(ref, rawValue, rawDataType, rawStyleIndex);
            if (!queue.offer(item)) {
//                outOfCapacity++;
                do {
                    threadYield();
                } while (!queue.offer(item));
            }
            return this;
        }

        @Override
        public Sheet build() {
//            System.out.println("outOfCapacity=" + outOfCapacity);
            while (!queue.isEmpty()) {
                threadYield();
            }

            singleConsumer.interrupt();
            executor.shutdown();
            try {
                executor.awaitTermination(100, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(MultiThreadedBuilder.class.getName()).log(Level.SEVERE, "", ex);
            }

            return super.build();
        }

        private static final class QueueItem {

            @Nullable
            public final String ref;
            @Nonnull
            public final CharSequence rawValue;
            @Nullable
            public final String rawDataType;
            @Nullable
            public final String rawStyleIndex;

            public QueueItem(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable String rawStyleIndex) {
                this.ref = ref;
                this.rawValue = rawValue;
                this.rawDataType = rawDataType;
                this.rawStyleIndex = rawStyleIndex;
            }
        }

        private static abstract class Consumer<T> implements Runnable {

            private final Queue<T> queue;
            private final AtomicBoolean endOfData;

            public Consumer(Queue<T> queue) {
                this.queue = queue;
                this.endOfData = new AtomicBoolean(false);
            }

            @Override
            public void run() {
                T result;
                while (true) {
                    while (null == (result = queue.poll())) {
                        if (endOfData.get()) {
                            return;
                        }
                        threadYield();
                    }
                    consume(result);
                }
            }

            abstract protected void consume(T item);

            public void interrupt() {
                endOfData.set(true);
            }
        }

        @SuppressWarnings("CallToThreadYield")
        private static void threadYield() {
            Thread.yield();
        }
    }

    private static final class CellValueFactory {

        // http://openxmldeveloper.org/blog/b/openxmldeveloper/archive/2012/03/08/dates-in-strict-spreadsheetml-files.aspx
        private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
        // http://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.cellvalues.aspx
        private static final String BOOLEAN = "b";
        private static final String NUMBER = "n";
        private static final String ERROR = "e";
        private static final String SHARED_STRING = "s";
        private static final String STRING = "str";
        private static final String INLINE_STRING = "inlineStr";
        private static final String DATE = "d";
        //
        private final SheetContext context;
        //
        private final Calendar calendar;
        private final DateFormat isoDateFormat;

        public CellValueFactory(SheetContext context) {
            this.context = context;
            // using default time-zone
            this.calendar = new GregorianCalendar();
            this.isoDateFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
        }

        private boolean isDateStyle(@Nullable String rawStyleIndex) {
            if (rawStyleIndex == null) {
                return false;
            }
            int styleIndex = Integer.parseInt(rawStyleIndex);
            FastPoiBook.Style style = context.styles.get(styleIndex);
            return DateUtil.isADateFormat(style.formatId, style.formatString);
        }

        @Nullable
        private Number parseNumber(@Nonnull String rawValue) {
            try {
                return Double.valueOf(rawValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        @Nullable
        private Object getNumberOrDate(@Nonnull String rawValue, @Nullable String rawStyleIndex) {
            Number number = parseNumber(rawValue);
            if (number != null && isDateStyle(rawStyleIndex)) {
                double tmp = number.doubleValue();
                if (DateUtil.isValidExcelDate(tmp)) {
                    return DateUtil2.getJavaDate(calendar, tmp, context.date1904);
                }
            }
            return number;
        }

        @Nullable
        public Object getValue(@Nonnull String rawValue, @Nullable String rawDataType, @Nullable String rawStyleIndex) {
            if (rawDataType == null) {
                return getNumberOrDate(rawValue, rawStyleIndex);
            }
            switch (rawDataType) {
                case NUMBER:
                    return getNumberOrDate(rawValue, rawStyleIndex);
                case SHARED_STRING:
                    return context.sharedStrings.get(Integer.parseInt(rawValue));
                case STRING:
                    return rawValue;
                case INLINE_STRING:
                    // TODO: rawValue might contain rich text
                    return rawValue;
                case DATE:
                    try {
                        return isoDateFormat.parse(rawValue);
                    } catch (ParseException ex) {
                        return null;
                    }
                default:
                    // BOOLEAN or ERROR or default
                    return null;
            }
        }
    }
    //</editor-fold>
}
