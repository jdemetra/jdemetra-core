/*
 * Copyright 2016 National Bank of Belgium
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
package spreadsheet.xlsx.internal;

import ec.util.spreadsheet.helpers.ArraySheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxSheetBuilder;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class XlsxSheetBuilders {

    private XlsxSheetBuilders() {
        // static class
    }

    @Nonnull
    public static XlsxSheetBuilder create(
            @Nonnull XlsxDateSystem dateSystem,
            @Nonnull IntFunction<String> sharedStrings,
            @Nonnull IntPredicate dateFormats) {
        XlsxValueFactory valueFactory = new XlsxValueFactory(dateSystem, sharedStrings, dateFormats);
        return CORES > 1
                ? new MultiThreadedBuilder(valueFactory)
                : new Builder(valueFactory);
    }

    private static final int CORES = Runtime.getRuntime().availableProcessors();

    private static class Builder implements XlsxSheetBuilder {

        private final XlsxValueFactory valueFactory;
        private final CellRefHelper refHelper;
        private ArraySheet.Builder arraySheetBuilder;

        public Builder(XlsxValueFactory valueFactory) {
            this.valueFactory = valueFactory;
            this.refHelper = new CellRefHelper();
            this.arraySheetBuilder = null;
        }

        @Override
        public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
            arraySheetBuilder = ArraySheet.builder(sheetBounds).name(sheetName);
            return this;
        }

        @Override
        public Builder put(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable Integer rawStyleIndex) {
            process(ref, rawValue, rawDataType, rawStyleIndex);
            return this;
        }

        protected void process(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable Integer rawStyleIndex) {
            Object cellValue = valueFactory.getValue(rawValue.toString(), rawDataType, rawStyleIndex);
            if (cellValue == null || !refHelper.parse(ref)) {
                return;
            }
            arraySheetBuilder.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), cellValue);
        }

        @Override
        public ArraySheet build() {
            return arraySheetBuilder != null ? arraySheetBuilder.build() : ArraySheet.copyOf("", new Object[][]{});
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static final class MultiThreadedBuilder extends Builder {

        private final ExecutorService executor;
        private final Queue<QueueItem> queue;
        private final Consumer singleConsumer;
//        int outOfCapacity = 0;

        public MultiThreadedBuilder(XlsxValueFactory valueFactory) {
            super(valueFactory);
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
        public Builder put(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable Integer rawStyleIndex) {
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
        public ArraySheet build() {
//            System.out.println("outOfCapacity=" + outOfCapacity);
            while (!queue.isEmpty()) {
                threadYield();
            }
            return super.build();
        }

        @Override
        public void close() throws IOException {
            singleConsumer.interrupt();
            executor.shutdown();
            try {
                executor.awaitTermination(100, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw new IOException("While closing executor", ex);
            }
        }

        private static final class QueueItem {

            @Nullable
            public final String ref;
            @Nonnull
            public final CharSequence rawValue;
            @Nullable
            public final String rawDataType;
            @Nullable
            public final Integer rawStyleIndex;

            public QueueItem(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable Integer rawStyleIndex) {
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
}
