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
package demetra.tsprovider.cube;

import demetra.timeseries.util.TsDataBuilder;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import demetra.design.NotThreadSafe;
import demetra.design.ThreadSafe;
import java.util.Map;
import java.util.stream.Stream;
import nbbrd.io.AbstractIOIterator;
import nbbrd.io.WrappedIOException;
import nbbrd.io.function.IORunnable;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class TableAsCubeAccessor implements CubeAccessor {

    @ThreadSafe
    public interface Resource<DATE> {

        @Nullable
        Exception testConnection();

        @NonNull
        CubeId getRoot() throws Exception;

        @NonNull
        AllSeriesCursor getAllSeriesCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        AllSeriesWithDataCursor<DATE> getAllSeriesWithDataCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        SeriesCursor getSeriesCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        SeriesWithDataCursor<DATE> getSeriesWithDataCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        ChildrenCursor getChildrenCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        String getDisplayName() throws Exception;

        @NonNull
        String getDisplayName(@NonNull CubeId id) throws Exception;

        @NonNull
        String getDisplayNodeName(@NonNull CubeId id) throws Exception;

        @NonNull
        TsDataBuilder<DATE> newBuilder();
    }

    @NotThreadSafe
    public interface TableCursor extends AutoCloseable {

        boolean nextRow() throws Exception;
    }

    @NotThreadSafe
    public interface WithLabel {

        @Nullable
        String getLabelOrNull() throws Exception;
    }

    @NotThreadSafe
    public interface WithData<DATE> {

        @Nullable
        DATE getPeriodOrNull() throws Exception;

        @Nullable
        Number getValueOrNull() throws Exception;
    }

    @NotThreadSafe
    public interface SeriesCursor extends TableCursor, WithLabel {
    }

    @NotThreadSafe
    public interface SeriesWithDataCursor<DATE> extends SeriesCursor, WithData<DATE> {
    }

    @NotThreadSafe
    public interface AllSeriesCursor extends TableCursor, WithLabel {

        @NonNull
        String[] getDimValues() throws Exception;
    }

    @NotThreadSafe
    public interface AllSeriesWithDataCursor<DATE> extends AllSeriesCursor, WithData<DATE> {
    }

    @NotThreadSafe
    public interface ChildrenCursor extends TableCursor {

        @NonNull
        String getChild() throws Exception;
    }

    @lombok.NonNull
    private final Resource<?> resource;

    @Override
    public IOException testConnection() {
        Exception result = resource.testConnection();
        return result != null ? WrappedIOException.wrap(result) : null;
    }

    @Override
    public CubeId getRoot() throws IOException {
        try {
            return resource.getRoot();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public Stream<CubeSeries> getAllSeries(CubeId id) throws IOException {
        try {
            AllSeriesCursor cursor = resource.getAllSeriesCursor(id);
            return new AllSeriesIterator(id, cursor).asStream();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public Stream<CubeSeriesWithData> getAllSeriesWithData(CubeId id) throws IOException {
        try {
            AllSeriesWithDataCursor cursor = resource.getAllSeriesWithDataCursor(id);
            return new AllSeriesWithDataIterator(id, cursor, resource.newBuilder()).asStream();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public CubeSeries getSeries(CubeId id) throws IOException {
        try (SeriesCursor cursor = resource.getSeriesCursor(id)) {
            AbstractIOIterator<CubeSeries> result = new SeriesIterator(id, cursor);
            return result.hasNextWithIO() ? result.nextWithIO() : null;
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public CubeSeriesWithData getSeriesWithData(CubeId id) throws IOException {
        try (SeriesWithDataCursor cursor = resource.getSeriesWithDataCursor(id)) {
            AbstractIOIterator<CubeSeriesWithData> result = new SeriesWithDataIterator(id, cursor, resource.newBuilder());
            return result.hasNextWithIO() ? result.nextWithIO() : null;
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public Stream<CubeId> getChildren(CubeId id) throws IOException {
        try {
            ChildrenCursor cursor = resource.getChildrenCursor(id);
            return new ChildrenIterator(id, cursor).asStream();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public String getDisplayName() throws IOException {
        try {
            return resource.getDisplayName();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public String getDisplayName(CubeId id) throws IOException {
        try {
            return resource.getDisplayName(id);
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public String getDisplayNodeName(CubeId id) throws IOException {
        try {
            return resource.getDisplayNodeName(id);
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public void close() throws IOException {
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final Map<String, String> NO_META = Collections.emptyMap();

    private static abstract class AbstractTableIterator<T> extends AbstractIOIterator<T> {

        abstract protected TableCursor getTableCursor();

        @Override
        public Stream<T> asStream() {
            return super.asStream().onClose(IORunnable.unchecked(this::close));
        }

        private void close() throws IOException {
            try {
                getTableCursor().close();
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class AllSeriesIterator extends AbstractTableIterator<CubeSeries> {

        private final CubeId parentId;
        private final AllSeriesCursor cursor;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                return cursor.nextRow();
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeries get() throws IOException {
            try {
                return new CubeSeries(parentId.child(cursor.getDimValues()), cursor.getLabelOrNull(), NO_META);
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class AllSeriesWithDataIterator<DATE> extends AbstractTableIterator<CubeSeriesWithData> {

        private final CubeId parentId;
        private final AllSeriesWithDataCursor<DATE> cursor;
        private final TsDataBuilder<DATE> data;

        private boolean first = true;
        private boolean t0 = false;
        private String[] currentId = null;
        private String currentLabel = null;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                if (first) {
                    t0 = cursor.nextRow();
                    first = false;
                }
                while (t0) {
                    data.clear();
                    currentId = cursor.getDimValues();
                    currentLabel = cursor.getLabelOrNull();
                    boolean t1 = true;
                    while (t1) {
                        DATE period = cursor.getPeriodOrNull();
                        Number value = null;
                        boolean t2 = true;
                        while (t2) {
                            value = cursor.getValueOrNull();
                            t0 = cursor.nextRow();
                            t1 = t0 && Arrays.equals(currentId, cursor.getDimValues());
                            t2 = t1 && Objects.equals(period, cursor.getPeriodOrNull());
                        }
                        data.add(period, value);
                    }
                    return true;
                }
                currentId = null;
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeriesWithData get() throws IOException {
            return new CubeSeriesWithData(parentId.child(currentId), currentLabel, NO_META, data.build());
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class SeriesIterator<DATE> extends AbstractTableIterator<CubeSeries> {

        private final CubeId parentId;
        private final SeriesCursor cursor;

        private String currentLabel = null;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                boolean t0 = cursor.nextRow();
                if (t0) {
                    currentLabel = cursor.getLabelOrNull();
                    return true;
                }
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeries get() throws IOException {
            return new CubeSeries(parentId, currentLabel, NO_META);
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class SeriesWithDataIterator<DATE> extends AbstractTableIterator<CubeSeriesWithData> {

        private final CubeId parentId;
        private final SeriesWithDataCursor<DATE> cursor;
        private final TsDataBuilder<DATE> data;

        private String currentLabel = null;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                boolean t0 = cursor.nextRow();
                if (t0) {
                    currentLabel = cursor.getLabelOrNull();
                    DATE latestPeriod = cursor.getPeriodOrNull();
                    while (t0) {
                        DATE period = latestPeriod;
                        Number value = null;
                        boolean t1 = true;
                        while (t1) {
                            value = cursor.getValueOrNull();
                            t0 = cursor.nextRow();
                            t1 = t0 && Objects.equals(period, latestPeriod = cursor.getPeriodOrNull());
                        }
                        data.add(period, value);
                    }
                    return true;
                }
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeriesWithData get() throws IOException {
            return new CubeSeriesWithData(parentId, currentLabel, NO_META, data.build());
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ChildrenIterator extends AbstractTableIterator<CubeId> {

        private final CubeId parentId;
        private final ChildrenCursor cursor;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                return cursor.nextRow();
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeId get() throws IOException {
            try {
                return parentId.child(cursor.getChild());
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }
    //</editor-fold>
}
