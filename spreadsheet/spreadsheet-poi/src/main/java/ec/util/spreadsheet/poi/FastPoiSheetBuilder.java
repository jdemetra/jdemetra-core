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
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nonnull
    public static FastPoiSheetBuilder create(@Nonnull String name, @Nonnull FastPoiContext sheetContext, @Nullable String sheetBounds) {
        ArraySheet.Builder arraySheetBuilder = ArraySheet.builder(sheetBounds).name(name);
        FastPoiValueFactory valueFactory = new FastPoiValueFactory(sheetContext);
        return CORES > 1 ? new MultiThreadedBuilder(arraySheetBuilder, valueFactory) : new Builder(arraySheetBuilder, valueFactory);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final int CORES = Runtime.getRuntime().availableProcessors();

    private static class Builder extends FastPoiSheetBuilder {

        private final ArraySheet.Builder arraySheetBuilder;
        private final FastPoiValueFactory valueFactory;
        private final CellRefHelper refHelper;

        public Builder(ArraySheet.Builder arraySheetBuilder, FastPoiValueFactory valueFactory) {
            this.arraySheetBuilder = arraySheetBuilder;
            this.valueFactory = valueFactory;
            this.refHelper = new CellRefHelper();
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
            arraySheetBuilder.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), cellValue);
        }

        @Override
        public Sheet build() {
            return arraySheetBuilder.build();
        }
    }

    private static final class MultiThreadedBuilder extends Builder {

        private final ExecutorService executor;
        private final Queue<QueueItem> queue;
        private final Consumer singleConsumer;
//        int outOfCapacity = 0;

        public MultiThreadedBuilder(ArraySheet.Builder arraySheetBuilder, FastPoiValueFactory valueFactory) {
            super(arraySheetBuilder, valueFactory);
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
    //</editor-fold>
}
