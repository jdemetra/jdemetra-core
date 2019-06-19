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
package ec.tss.tsproviders.cube;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataDisplayName;
import ec.tss.tsproviders.HasDataHierarchy;
import ec.tss.tsproviders.HasDataMoniker;
import ec.tss.tsproviders.cursor.HasTsCursor;
import ec.tss.tsproviders.utils.IConfig;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.cursor.TsCursor;
import ec.tss.tsproviders.cursor.TsCursorAsFiller;
import ec.tss.tsproviders.utils.IteratorWithIO;
import ec.tss.tsproviders.utils.TsFillerAsProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public final class CubeSupport implements HasDataHierarchy, HasTsCursor, HasDataDisplayName {

    @ThreadSafe
    public interface Resource {

        @NonNull
        CubeAccessor getAccessor(@NonNull DataSource dataSource) throws IOException, IllegalArgumentException;

        @NonNull
        IParam<DataSet, CubeId> getIdParam(@NonNull DataSource dataSource) throws IOException, IllegalArgumentException;
    }

    @NonNull
    public static CubeSupport of(@NonNull Resource resource) {
        return new CubeSupport(Objects.requireNonNull(resource));
    }

    private final Resource resource;

    private CubeSupport(Resource resource) {
        this.resource = resource;
    }

    //<editor-fold defaultstate="collapsed" desc="HasDataHierarchy">
    @Override
    public List<DataSet> children(DataSource dataSource) throws IOException {
        CubeAccessor acc = resource.getAccessor(dataSource);
        IParam<DataSet, CubeId> idParam = resource.getIdParam(dataSource);

        CubeId parentId = acc.getRoot();

        // special case: we return a fake dataset if no dimColumns
        if (parentId.isVoid()) {
            IOException ex = acc.testConnection();
            if (ex != null) {
                throw ex;
            }
            DataSet fake = DataSet.of(dataSource, DataSet.Kind.SERIES);
            return Collections.singletonList(fake);
        }

        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        return children(builder, acc.getChildren(parentId), idParam);
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IOException {
        if (!DataSet.Kind.COLLECTION.equals(parent.getKind())) {
            throw new IllegalArgumentException("Not a collection");
        }

        CubeAccessor acc = resource.getAccessor(parent.getDataSource());
        IParam<DataSet, CubeId> idParam = resource.getIdParam(parent.getDataSource());

        CubeId parentId = idParam.get(parent);

        DataSet.Builder builder = parent.toBuilder(parentId.getDepth() > 1 ? DataSet.Kind.COLLECTION : DataSet.Kind.SERIES);
        return children(builder, acc.getChildren(parentId), idParam);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasTsCursor">
    @Override
    public TsCursor<DataSet> getData(DataSource dataSource, TsInformationType type) throws IOException {
        CubeAccessor acc = resource.getAccessor(dataSource);
        IParam<DataSet, CubeId> idParam = resource.getIdParam(dataSource);

        CubeId parentId = acc.getRoot();

        TsCursor<CubeId> cursor = type.encompass(TsInformationType.Data) ? acc.getAllSeriesWithData(parentId) : acc.getAllSeries(parentId);
        return cursor.transform(toDataSetFunc(DataSet.builder(dataSource, DataSet.Kind.SERIES), idParam));
    }

    @Override
    public TsCursor<DataSet> getData(DataSet dataSet, TsInformationType type) throws IOException {
        CubeAccessor acc = resource.getAccessor(dataSet.getDataSource());
        IParam<DataSet, CubeId> idParam = resource.getIdParam(dataSet.getDataSource());

        CubeId id = idParam.get(dataSet);

        TsCollectionInformation result = new TsCollectionInformation();
        result.type = type;

        TsCursor<CubeId> cursor = DataSet.Kind.COLLECTION.equals(dataSet.getKind())
                ? type.encompass(TsInformationType.Data) ? acc.getAllSeriesWithData(id) : acc.getAllSeries(id)
                : type.encompass(TsInformationType.Data) ? acc.getSeriesWithData(id) : TsCursor.singleton(id);
        return cursor.transform(toDataSetFunc(dataSet.toBuilder(DataSet.Kind.SERIES), idParam));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasDataDisplayName">
    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        try {
            return resource.getAccessor(dataSource).getDisplayName();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        try {
            DataSource dataSource = dataSet.getDataSource();
            CubeId id = resource.getIdParam(dataSource).get(dataSet);
            return resource.getAccessor(dataSource).getDisplayName(id);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        try {
            DataSource dataSource = dataSet.getDataSource();
            CubeId id = resource.getIdParam(dataSource).get(dataSet);
            return resource.getAccessor(dataSource).getDisplayNodeName(id);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
    //</editor-fold>

    @NonNull
    public static IParam<DataSet, CubeId> idByName(@NonNull CubeId root) {
        return new ByNameParam(Objects.requireNonNull(root));
    }

    @NonNull
    public static IParam<DataSet, CubeId> idBySeparator(@NonNull CubeId root, @NonNull String separator, @NonNull String name) {
        return new BySeparatorParam(Objects.requireNonNull(separator), Objects.requireNonNull(root), Objects.requireNonNull(name));
    }

    @NonNull
    public static ITsProvider asTsProvider(
            @NonNull String providerName,
            @NonNull Logger logger,
            @NonNull CubeSupport cubeSupport,
            @NonNull HasDataMoniker monikerSupport,
            @NonNull Runnable cacheCleaner) {
        return TsFillerAsProvider.of(providerName, TsAsyncMode.Once, TsCursorAsFiller.of(logger, cubeSupport, monikerSupport, cubeSupport), cacheCleaner);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static List<DataSet> children(DataSet.Builder builder, IteratorWithIO<CubeId> values, IParam<DataSet, CubeId> idParam) throws IOException {
        try (IteratorWithIO<DataSet> cursor = values.transform(toDataSetFunc(builder, idParam))) {
            List<DataSet> result = new ArrayList<>();
            while (cursor.hasNext()) {
                result.add(cursor.next());
            }
            return result;
        }
    }

    private static Function<CubeId, DataSet> toDataSetFunc(DataSet.Builder builder, IParam<DataSet, CubeId> dimValuesParam) {
        return o -> builder.put(dimValuesParam, o).build();
    }

    private static final class ByNameParam implements IParam<DataSet, CubeId> {

        private final CubeId root;

        public ByNameParam(CubeId root) {
            this.root = root;
        }

        @Override
        public CubeId defaultValue() {
            return root;
        }

        @Override
        public CubeId get(DataSet config) {
            List<String> result = new ArrayList<>(root.getMaxLevel());
            for (int i = 0; i < root.getMaxLevel(); i++) {
                String tmp = config.get(root.getDimensionId(i));
                if (Strings.isNullOrEmpty(tmp)) {
                    break;
                }
                result.add(tmp);
            }
            return root.child(result.toArray(new String[result.size()]));
        }

        @Override
        public void set(IConfig.Builder<?, DataSet> builder, CubeId value) {
            for (int i = 0; i < value.getLevel(); i++) {
                builder.put(value.getDimensionId(i), value.getDimensionValue(i));
            }
        }
    }

    private static final class BySeparatorParam implements IParam<DataSet, CubeId> {

        private final Splitter splitter;
        private final String separator;
        private final CubeId root;
        private final String name;

        public BySeparatorParam(String separator, CubeId root, String name) {
            this.splitter = Splitter.on(separator).trimResults();
            this.separator = separator;
            this.root = root;
            this.name = name;
        }

        @Override
        public CubeId defaultValue() {
            return root;
        }

        @Override
        public CubeId get(DataSet config) {
            String tmp = config.get(name);
            if (Strings.isNullOrEmpty(tmp)) {
                return defaultValue();
            }
            List<String> items = splitter.splitToList(tmp);
            return root.child(items.toArray(new String[items.size()]));
        }

        @Override
        public void set(IConfig.Builder<?, DataSet> builder, CubeId value) {
            if (value.getLevel() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(value.getDimensionValue(0));
                for (int i = 1; i < value.getLevel(); i++) {
                    sb.append(separator).append(value.getDimensionValue(i));
                }
                builder.put(name, sb.toString());
            }
        }
    }
    //</editor-fold>
}
