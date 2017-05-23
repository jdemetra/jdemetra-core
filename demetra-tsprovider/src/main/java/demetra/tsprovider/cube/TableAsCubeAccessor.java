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

import demetra.tsprovider.OptionalTsData;
import demetra.tsprovider.cursor.TsCursor;
import demetra.tsprovider.util.TsDataBuilder;
import demetra.io.IteratorWithIO;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public final class TableAsCubeAccessor implements CubeAccessor {

    @ThreadSafe
    public interface Resource<T> {

        @Nullable
        Exception testConnection();

        @Nonnull
        CubeId getRoot();

        @Nonnull
        AllSeriesCursor getAllSeriesCursor(@Nonnull CubeId id) throws Exception;

        @Nonnull
        AllSeriesWithDataCursor<T> getAllSeriesWithDataCursor(@Nonnull CubeId id) throws Exception;

        @Nonnull
        SeriesWithDataCursor<T> getSeriesWithDataCursor(@Nonnull CubeId id) throws Exception;

        @Nonnull
        ChildrenCursor getChildrenCursor(@Nonnull CubeId id) throws Exception;

        @Nonnull
        String getDisplayName() throws Exception;

        @Nonnull
        String getDisplayName(@Nonnull CubeId id) throws Exception;

        @Nonnull
        String getDisplayNodeName(@Nonnull CubeId id) throws Exception;

        @Nonnull
        TsDataBuilder<T> newBuilder();
    }

    @NotThreadSafe
    public interface TableCursor extends AutoCloseable {

        boolean isClosed() throws Exception;

        boolean nextRow() throws Exception;
    }

    @NotThreadSafe
    public interface SeriesCursor extends TableCursor {

        @Nonnull
        Map<String, String> getMetaData() throws Exception;

        @Nonnull
        String getLabel() throws Exception;
    }

    @NotThreadSafe
    public interface AllSeriesCursor extends SeriesCursor {

        @Nonnull
        String[] getDimValues() throws Exception;
    }

    @NotThreadSafe
    public interface AllSeriesWithDataCursor<T> extends SeriesCursor {

        @Nonnull
        String[] getDimValues() throws Exception;

        @Nullable
        T getPeriod() throws Exception;

        @Nullable
        Number getValue() throws Exception;
    }

    @NotThreadSafe
    public interface SeriesWithDataCursor<T> extends SeriesCursor {

        @Nullable
        T getPeriod() throws Exception;

        @Nullable
        Number getValue() throws Exception;
    }

    @NotThreadSafe
    public interface ChildrenCursor extends TableCursor {

        @Nonnull
        String getChild() throws Exception;
    }

    @Nonnull
    public static TableAsCubeAccessor create(@Nonnull Resource<?> resource) {
        return new TableAsCubeAccessor(Objects.requireNonNull(resource));
    }

    private final Resource<?> resource;

    private TableAsCubeAccessor(Resource<?> resource) {
        this.resource = resource;
    }

    @Override
    public IOException testConnection() {
        Exception result = resource.testConnection();
        return result != null ? propagateIOException(result) : null;
    }

    @Override
    public CubeId getRoot() {
        return resource.getRoot();
    }

    @Override
    public TsCursor<CubeId> getAllSeries(CubeId id) throws IOException {
        try {
            AllSeriesCursor cursor = resource.getAllSeriesCursor(id);
            return new AllSeriesAdapter(id, cursor);
        } catch (Exception ex) {
            throw propagateIOException(ex);
        }
    }

    @Override
    public TsCursor<CubeId> getAllSeriesWithData(CubeId id) throws IOException {
        try {
            AllSeriesWithDataCursor cursor = resource.getAllSeriesWithDataCursor(id);
            return new AllSeriesWithDataAdapter(id, cursor, resource.newBuilder());
        } catch (Exception ex) {
            throw propagateIOException(ex);
        }
    }

    @Override
    public TsCursor<CubeId> getSeriesWithData(CubeId id) throws IOException {
        try {
            SeriesWithDataCursor cursor = resource.getSeriesWithDataCursor(id);
            return new SeriesWithDataAdapter(id, cursor, resource.newBuilder());
        } catch (Exception ex) {
            throw propagateIOException(ex);
        }
    }

    @Override
    public IteratorWithIO<CubeId> getChildren(CubeId id) throws IOException {
        try {
            ChildrenCursor cursor = resource.getChildrenCursor(id);
            return new ChildrenAdapter(id, cursor);
        } catch (Exception ex) {
            throw propagateIOException(ex);
        }
    }

    @Override
    public String getDisplayName() throws IOException {
        try {
            return resource.getDisplayName();
        } catch (Exception ex) {
            throw propagateIOException(ex);
        }
    }

    @Override
    public String getDisplayName(CubeId id) throws IOException {
        try {
            return resource.getDisplayName(id);
        } catch (Exception ex) {
            throw propagateIOException(ex);
        }
    }

    @Override
    public String getDisplayNodeName(CubeId id) throws IOException {
        try {
            return resource.getDisplayNodeName(id);
        } catch (Exception ex) {
            throw propagateIOException(ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static IOException propagateIOException(Exception ex) {
        return ex instanceof IOException ? (IOException) ex : new IOException(ex);
    }

    private abstract static class TableAsCubeAdapter<T extends SeriesCursor> implements TsCursor<CubeId> {

        protected final CubeId parentId;
        protected final T cursor;

        private TableAsCubeAdapter(CubeId parentId, T cursor) {
            this.parentId = parentId;
            this.cursor = cursor;
        }

        @Override
        public boolean isClosed() throws IOException {
            try {
                return cursor.isClosed();
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public Map<String, String> getMetaData() throws IOException {
            try {
                return cursor.getMetaData();
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public Map<String, String> getSeriesMetaData() throws IOException {
            return Collections.emptyMap();
        }

        @Override
        public void close() throws IOException {
            try {
                cursor.close();
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }
    }

    private static final class AllSeriesAdapter extends TableAsCubeAdapter<AllSeriesCursor> {

        private AllSeriesAdapter(CubeId parentId, AllSeriesCursor cursor) {
            super(parentId, cursor);
        }

        @Override
        public boolean nextSeries() throws IOException {
            try {
                return cursor.nextRow();
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public CubeId getSeriesId() throws IOException {
            try {
                return parentId.child(cursor.getDimValues());
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public String getSeriesLabel() throws IOException, IllegalStateException {
            try {
                return cursor.getLabel();
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public OptionalTsData getSeriesData() throws IOException {
            throw new IOException("Not requested");
        }
    }

    private static final class AllSeriesWithDataAdapter<T> extends TableAsCubeAdapter<AllSeriesWithDataCursor<T>> {

        private final TsDataBuilder<T> data;
        private boolean first;
        private boolean t0;
        private String[] currentId;
        private String currentLabel;

        private AllSeriesWithDataAdapter(CubeId parentId, AllSeriesWithDataCursor cursor, TsDataBuilder<T> data) {
            super(parentId, cursor);
            this.data = data;
            this.first = true;
            this.t0 = false;
            this.currentId = null;
            this.currentLabel = null;
        }

        @Override
        public boolean nextSeries() throws IOException {
            try {
                if (first) {
                    t0 = cursor.nextRow();
                    first = false;
                }
                while (t0) {
                    data.clear();
                    currentId = cursor.getDimValues();
                    currentLabel = cursor.getLabel();
                    boolean t1 = true;
                    while (t1) {
                        T period = cursor.getPeriod();
                        Number value = null;
                        boolean t2 = true;
                        while (t2) {
                            value = cursor.getValue();
                            t0 = cursor.nextRow();
                            t1 = t0 && Arrays.equals(currentId, cursor.getDimValues());
                            t2 = t1 && Objects.equals(period, cursor.getPeriod());
                        }
                        data.add(period, value);
                    }
                    return true;
                }
                currentId = null;
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public CubeId getSeriesId() throws IOException {
            return parentId.child(currentId);
        }

        @Override
        public String getSeriesLabel() throws IOException, IllegalStateException {
            return currentLabel;
        }

        @Override
        public OptionalTsData getSeriesData() throws IOException {
            return data.build();
        }
    }

    private static final class SeriesWithDataAdapter<T> extends TableAsCubeAdapter<SeriesWithDataCursor<T>> {

        private final TsDataBuilder<T> data;
        private String currentLabel;

        private SeriesWithDataAdapter(CubeId parentId, SeriesWithDataCursor cursor, TsDataBuilder<T> data) {
            super(parentId, cursor);
            this.data = data;
            this.currentLabel = null;
        }

        @Override
        public boolean nextSeries() throws IOException {
            try {
                boolean t0 = cursor.nextRow();
                if (t0) {
                    currentLabel = cursor.getLabel();
                    T latestPeriod = cursor.getPeriod();
                    while (t0) {
                        T period = latestPeriod;
                        Number value = null;
                        boolean t1 = true;
                        while (t1) {
                            value = cursor.getValue();
                            t0 = cursor.nextRow();
                            t1 = t0 && Objects.equals(period, latestPeriod = cursor.getPeriod());
                        }
                        data.add(period, value);
                    }
                    return true;
                }
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public CubeId getSeriesId() throws IOException {
            return parentId;
        }

        @Override
        public String getSeriesLabel() throws IOException, IllegalStateException {
            return currentLabel;
        }

        @Override
        public OptionalTsData getSeriesData() throws IOException {
            return data.build();
        }
    }

    private static final class ChildrenAdapter implements IteratorWithIO<CubeId> {

        private final CubeId parentId;
        private final ChildrenCursor cursor;
        private boolean hasNext;

        private ChildrenAdapter(CubeId parentId, ChildrenCursor cursor) throws Exception {
            this.parentId = parentId;
            this.cursor = cursor;
            this.hasNext = cursor.nextRow();
        }

        @Override
        public boolean hasNext() throws IOException {
            return hasNext;
        }

        @Override
        public CubeId next() throws IOException, NoSuchElementException {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            try {
                CubeId result = parentId.child(cursor.getChild());
                hasNext = cursor.nextRow();
                return result;
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                cursor.close();
            } catch (Exception ex) {
                throw propagateIOException(ex);
            }
        }
    }
    //</editor-fold>
}
