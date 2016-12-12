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

import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
        Builder result = new Builder(new XlsxValueFactory(dateSystem, sharedStrings, dateFormats));
        return CORES > 1 ? new MultiSheetBuilder(result) : result;
    }

    private static final int CORES = Runtime.getRuntime().availableProcessors();

    static final class Builder implements XlsxSheetBuilder {

        private final XlsxValueFactory valueFactory;
        private final CellRefHelper refHelper;
        private ArraySheet.Builder arraySheetBuilder;

        Builder(XlsxValueFactory valueFactory) {
            this.valueFactory = valueFactory;
            this.refHelper = new CellRefHelper();
            this.arraySheetBuilder = ArraySheet.builder();
        }

        @Override
        public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
            arraySheetBuilder = ArraySheet.builder(sheetBounds).name(sheetName);
            return this;
        }

        @Override
        public XlsxSheetBuilder put(String ref, CharSequence value, String dataType, Integer styleIndex) {
            Object cellValue = valueFactory.getValue(value.toString(), dataType, styleIndex);
            if (cellValue != null && refHelper.parse(ref)) {
                arraySheetBuilder.value(refHelper.getRowIndex(), refHelper.getColumnIndex(), cellValue);
            }
            return this;
        }

        @Override
        public Sheet build() {
            return arraySheetBuilder.build();
        }

        @Override
        public void close() {
            arraySheetBuilder.clear();
        }
    }

    static final class MultiSheetBuilder implements XlsxSheetBuilder {

        private static final int FIRST_BATCH_SIZE = 10;
        private static final int NEXT_BATCH_SIZE = 1000;
        private static final int QUEUE_MAX_SIZE = 10;

        private final Builder delegate;
        private final ExecutorService executor;
        private final CustomQueue queue;
        private Batch nextBatch;

        MultiSheetBuilder(Builder delegate) {
            this.delegate = delegate;
            this.executor = Executors.newSingleThreadExecutor();
            this.queue = new CustomQueue(QUEUE_MAX_SIZE);
            this.nextBatch = new Batch(FIRST_BATCH_SIZE);
        }

        @Override
        public XlsxSheetBuilder reset(String sheetName, String sheetBounds) {
            queue.waitForCompletion();
            delegate.reset(sheetName, sheetBounds);
            return this;
        }

        @Override
        public XlsxSheetBuilder put(String ref, CharSequence value, String dataType, Integer styleIndex) {
            if (nextBatch.isFull()) {
                if (queue.isFull()) {
                    queue.waitForCompletion();
                }
                queue.add(executor.submit(nextBatch.asTask(delegate)));
                nextBatch = new Batch(NEXT_BATCH_SIZE);
            }
            nextBatch.put(ref, value, dataType, styleIndex);
            return this;
        }

        @Override
        public Sheet build() {
            queue.waitForCompletion();
            if (nextBatch.getSize() > 0) {
                nextBatch.process(delegate);
                nextBatch = new Batch(FIRST_BATCH_SIZE);
            }
            return delegate.build();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
            executor.shutdown();
            try {
                executor.awaitTermination(100, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw new IOException("While closing executor", ex);
            }
        }

        static final class CustomQueue {

            private final int maxQueueSize;
            private final List<Future<?>> queue;

            CustomQueue(int maxQueueSize) {
                this.maxQueueSize = maxQueueSize;
                this.queue = new ArrayList<>(maxQueueSize);
            }

            boolean isFull() {
                return queue.size() >= maxQueueSize;
            }

            void waitForCompletion() {
                for (Future<?> o : queue) {
                    try {
                        o.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                queue.clear();
            }

            private void add(Future<?> submit) {
                queue.add(submit);
            }
        }

        static final class Batch {

            private final Object[][] values;
            private int size;

            Batch(int maxSize) {
                this.values = new Object[maxSize][4];
                this.size = 0;
            }

            void put(@Nullable String ref, @Nonnull CharSequence value, @Nullable String dataType, @Nullable Integer styleIndex) {
                Object[] row = values[size++];
                row[0] = ref;
                row[1] = value;
                row[2] = dataType;
                row[3] = styleIndex;
            }

            int getSize() {
                return size;
            }

            boolean isFull() {
                return values.length == size;
            }

            void process(Builder delegate) {
                for (int i = 0; i < size; i++) {
                    delegate.put((String) values[i][0], (CharSequence) values[i][1], (String) values[i][2], (Integer) values[i][3]);
                }
            }

            Runnable asTask(Builder delegate) {
                return () -> process(delegate);
            }
        }
    }
}
