/*
 * Copyright 2015 National Bank of Belgium
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

import demetra.timeseries.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataDisplayName;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.stream.DataSetTs;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.ResourcePool;
import internal.util.Strings;
import lombok.AllArgsConstructor;
import nbbrd.design.MightBePromoted;
import nbbrd.design.ThreadSafe;
import nbbrd.io.function.IOFunction;
import nbbrd.io.function.IORunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class CubeSupport implements HasDataHierarchy, HasTsStream, HasDataDisplayName {

    @FunctionalInterface
    public interface CubeConnectionSupplier {

        @NonNull CubeConnection openConnection(@NonNull DataSource dataSource) throws IOException;
    }

    @FunctionalInterface
    public interface CubeConverterSupplier {

        DataSet.@NonNull Converter<CubeId> getConverter(@NonNull CubeId root) throws IOException;
    }

    @lombok.NonNull
    private final String providerName;

    @lombok.NonNull
    private final CubeSupport.CubeConnectionSupplier connectionSupplier;

    @lombok.NonNull
    private final CubeConverterSupplier converterSupplier;

    //<editor-fold defaultstate="collapsed" desc="HasDataHierarchy">
    @Override
    public List<DataSet> children(DataSource dataSource) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        try (CubeConnection conn = connectionSupplier.openConnection(dataSource)) {
            CubeId parentId = conn.getRoot();

            // special case: we return a fake dataset if no dimColumns
            if (parentId.isVoid()) {
                Optional<IOException> ex = conn.testConnection();
                if (ex.isPresent()) {
                    throw ex.get();
                }
                DataSet fake = DataSet.of(dataSource, DataSet.Kind.SERIES);
                return Collections.singletonList(fake);
            }

            try (Stream<CubeId> children = conn.getChildren(parentId)) {
                DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
                return children
                        .map(toDataSetFunc(builder, converterSupplier.getConverter(parentId)))
                        .collect(Collectors.toList());
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
        }
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, parent);

        if (!DataSet.Kind.COLLECTION.equals(parent.getKind())) {
            throw new IllegalArgumentException("Not a collection");
        }

        try (CubeConnection conn = connectionSupplier.openConnection(parent.getDataSource())) {
            DataSet.Converter<CubeId> converter = converterSupplier.getConverter(conn.getRoot());

            CubeId parentId = converter.get(parent);

            try (Stream<CubeId> children = conn.getChildren(parentId)) {
                DataSet.Builder builder = parent.toBuilder(parentId.getDepth() > 1 ? DataSet.Kind.COLLECTION : DataSet.Kind.SERIES);
                return children
                        .map(toDataSetFunc(builder, converter))
                        .collect(Collectors.toList());
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasTsStream">
    @Override
    public Stream<DataSetTs> getData(DataSource dataSource, TsInformationType type) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        CubeConnection conn = connectionSupplier.openConnection(dataSource);
        try {
            CubeId parentId = conn.getRoot();

            Function<CubeId, DataSet> toDataSet = toDataSetFunc(DataSet.builder(dataSource, DataSet.Kind.SERIES), converterSupplier.getConverter(parentId));

            return (
                    type.encompass(TsInformationType.Data)
                            ? conn.getAllSeriesWithData(parentId).map(getSeriesWithDataFunc(toDataSet, conn::getDisplayName))
                            : conn.getAllSeries(parentId).map(getSeriesFunc(toDataSet, conn::getDisplayName))
            ).onClose(IORunnable.unchecked(conn::close));
        } catch (IOException ex) {
            throw close(conn, ex);
        }
    }

    @Override
    public Stream<DataSetTs> getData(DataSet dataSet, TsInformationType type) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);

        CubeConnection conn = connectionSupplier.openConnection(dataSet.getDataSource());
        try {
            DataSet.Converter<CubeId> converter = converterSupplier.getConverter(conn.getRoot());

            CubeId id = converter.get(dataSet);

            Function<CubeId, DataSet> toDataSet = toDataSetFunc(dataSet.toBuilder(DataSet.Kind.SERIES), converter);

            boolean collection = DataSet.Kind.COLLECTION.equals(dataSet.getKind());
            return (
                    type.encompass(TsInformationType.Data)
                            ? (collection ? conn.getAllSeriesWithData(id) : stream(conn.getSeriesWithData(id))).map(getSeriesWithDataFunc(toDataSet, conn::getDisplayName))
                            : (collection ? conn.getAllSeries(id) : stream(conn.getSeries(id))).map(getSeriesFunc(toDataSet, conn::getDisplayName))
            ).onClose(IORunnable.unchecked(conn::close));
        } catch (IOException ex) {
            throw close(conn, ex);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasDataDisplayName">
    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        try (CubeConnection conn = connectionSupplier.openConnection(dataSource)) {
            return conn.getDisplayName();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);

        try (CubeConnection conn = connectionSupplier.openConnection(dataSet.getDataSource())) {
            CubeId id = converterSupplier.getConverter(conn.getRoot()).get(dataSet);
            return conn.getDisplayName(id);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);

        try (CubeConnection conn = connectionSupplier.openConnection(dataSet.getDataSource())) {
            CubeId id = converterSupplier.getConverter(conn.getRoot()).get(dataSet);
            return conn.getDisplayNodeName(id);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
    //</editor-fold>

    public static DataSet.@NonNull Converter<CubeId> idByName(@NonNull CubeId root) {
        return new ByNameParam(Objects.requireNonNull(root));
    }

    public static DataSet.@NonNull Converter<CubeId> idBySeparator(@NonNull CubeId root, @NonNull String separator, @NonNull String name) {
        return new BySeparatorParam(Objects.requireNonNull(separator), Objects.requireNonNull(root), Objects.requireNonNull(name));
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static Function<CubeId, DataSet> toDataSetFunc(DataSet.Builder builder, DataSet.Converter<CubeId> converter) {
        return o -> {
            converter.set(builder, o);
            return builder.build();
        };
    }

    private static String getNonNullLabel(String label, CubeId id, IOFunction<CubeId, String> toLabel) {
        if (label != null) {
            return label;
        }
        try {
            return toLabel.applyWithIO(id);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Function<CubeSeriesWithData, DataSetTs> getSeriesWithDataFunc(Function<CubeId, DataSet> toDataSet, IOFunction<CubeId, String> toLabel) {
        return ts -> new DataSetTs(toDataSet.apply(ts.getId()), getNonNullLabel(ts.getLabel(), ts.getId(), toLabel), ts.getMeta(), ts.getData());
    }

    private static Function<CubeSeries, DataSetTs> getSeriesFunc(Function<CubeId, DataSet> toDataSet, IOFunction<CubeId, String> toLabel) {
        return ts -> new DataSetTs(toDataSet.apply(ts.getId()), getNonNullLabel(ts.getLabel(), ts.getId(), toLabel), ts.getMeta(), DataSetTs.DATA_NOT_REQUESTED);
    }

    @AllArgsConstructor
    private static final class ByNameParam implements DataSet.Converter<CubeId> {

        private final CubeId root;

        @Override
        public CubeId getDefaultValue() {
            return root;
        }

        @Override
        public CubeId get(DataSet config) {
            String[] dimValues = new String[root.getMaxLevel()];
            int i = 0;
            while (i < dimValues.length) {
                String id = root.getDimensionId(i);
                if ((dimValues[i] = config.getParameter(id)) == null) {
                    break;
                }
                i++;
            }
            return root.child(dimValues, i);
        }

        @Override
        public void set(DataSet.Builder builder, CubeId value) {
            for (int i = 0; i < value.getLevel(); i++) {
                builder.parameter(value.getDimensionId(i), value.getDimensionValue(i));
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class BySeparatorParam implements DataSet.Converter<CubeId> {

        private final String separator;
        private final CubeId root;
        private final String name;

        @Override
        public CubeId getDefaultValue() {
            return root;
        }

        @Override
        public CubeId get(DataSet config) {
            String value = config.getParameter(name);
            if (value == null || value.isEmpty()) {
                return getDefaultValue();
            }

            String[] dimValues = new String[root.getMaxLevel()];
            int i = 0;
            Iterator<String> iterator = Strings.splitToIterator(separator, value);
            while (i < dimValues.length && iterator.hasNext()) {
                dimValues[i] = iterator.next();
                i++;
            }
            return root.child(dimValues, i);
        }

        @Override
        public void set(DataSet.Builder builder, CubeId value) {
            if (value.getLevel() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(value.getDimensionValue(0));
                for (int i = 1; i < value.getLevel(); i++) {
                    sb.append(separator).append(value.getDimensionValue(i));
                }
                builder.parameter(name, sb.toString());
            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @MightBePromoted
    private static <T> Stream<T> stream(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    @MightBePromoted
    private static <EX extends Throwable> EX close(Closeable conn, EX ex) {
        try {
            conn.close();
        } catch (IOException other) {
            ex.addSuppressed(other);
        }
        return ex;
    }
    //</editor-fold>

    @NonNull
    public static ResourcePool<CubeConnection> newCubeConnectionPool() {
        return ResourcePool.of(PooledCubeConnection::new);
    }

    @NonNull
    public static CubeConnectionSupplier asCubeConnectionSupplier(@NonNull ResourcePool<CubeConnection> pool, ResourcePool.@NonNull Factory<CubeConnection> delegate) {
        return dataSource -> pool.get(dataSource, delegate);
    }

    @lombok.RequiredArgsConstructor
    private static final class PooledCubeConnection implements CubeConnection {

        @lombok.NonNull
        @lombok.experimental.Delegate(excludes = Closeable.class)
        private final CubeConnection delegate;

        @lombok.NonNull
        @lombok.experimental.Delegate
        private final Closeable onClose;
    }
}
