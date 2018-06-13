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
package demetra.demo;

import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceProvider;
import demetra.tsprovider.HasDataDisplayName;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.HasDataSourceList;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.TsProvider;
import demetra.tsprovider.cursor.HasTsCursor;
import demetra.tsprovider.cursor.TsCursor;
import demetra.tsprovider.cursor.TsCursorAsProvider;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.Params;
import ioutil.IO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public final class PocProvider implements DataSourceProvider {

    public static final String NAME = "poc";

    private static final IParam<DataSource, DataType> TYPE_PARAM = Params.onEnum(DataType.NORMAL, "t");
    private static final IParam<DataSet, Integer> INDEX_PARAM = Params.onInteger(-1, "i");

    @lombok.experimental.Delegate(types = HasDataHierarchy.class)
    private final PocDataSupport dataSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceList listSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName nameSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate(excludes = AutoCloseable.class)
    private final TsProvider tsSupport;

    private final Timer updater;

    public PocProvider() {
        this.dataSupport = new PocDataSupport();
        this.listSupport = HasDataSourceList.of(NAME, createDataSources());
        this.nameSupport = new PocDataDisplayName();
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.tsSupport = TsCursorAsProvider.of(NAME, dataSupport, monikerSupport, () -> {
        });

        this.updater = new Timer(true);
        updater.schedule(new TimerTask() {
            private final DataSource updatingSource = createDataSource(DataType.UPDATING);

            @Override
            public void run() {
                reload(updatingSource);
            }
        }, 1000, 1000);
    }

    @Override
    public String getDisplayName() {
        return "Proof-of-concept";
    }

    @Override
    public void close() {
        updater.cancel();
        tsSupport.close();
    }

    private static List<DataSource> createDataSources() {
        return Stream.of(DataType.values()).map(PocProvider::createDataSource).collect(Collectors.toList());
    }

    private static DataSource createDataSource(DataType o) {
        return DataSource.builder(NAME, "").put(TYPE_PARAM, o).build();
    }

    private static final class PocDataDisplayName implements HasDataDisplayName {

        @Override
        public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
            DataSourcePreconditions.checkProvider(NAME, dataSource);
            switch (TYPE_PARAM.get(dataSource)) {
                case NORMAL:
                    return "Normal async";
                case FAILING_META:
                    return "Failing on meta";
                case FAILING_DATA:
                    return "Failing on data";
                case FAILING_DEF:
                    return "Failing on definition";
                case UPDATING:
                    return "Auto updating";
                case SLOW:
                    return "Slow retrieval";
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
            DataSourcePreconditions.checkProvider(NAME, dataSet);
            return getDisplayName(dataSet.getDataSource()) + System.lineSeparator() + getDisplayNodeName(dataSet);
        }

        @Override
        public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
            DataSourcePreconditions.checkProvider(NAME, dataSet);
            TsDomain domain = TYPE_PARAM.get(dataSet.getDataSource()).getDomain(INDEX_PARAM.get(dataSet));
            return domain.getStartPeriod().getUnit() + "#" + domain.getLength();
        }
    }

    private static final class PocDataSupport implements HasTsCursor, HasDataHierarchy {

        private final Function<Integer, TsData> normalData;
        private final Function<Integer, TsData> updatingData;
        private final Function<Integer, TsData> slowData;

        public PocDataSupport() {
            normalData = createData(DataType.NORMAL)::get;
            updatingData = shiftingValues(createData(DataType.UPDATING));
            slowData = createData(DataType.SLOW)::get;
        }

        @Override
        public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
            DataSourcePreconditions.checkProvider(NAME, dataSource);
            List<DataSet> result = new ArrayList<>();
            try (TsCursor<DataSet> cursor = cursorOf(TYPE_PARAM.get(dataSource), TsInformationType.Definition).map(dataSetFunc(dataSource))) {
                while (cursor.nextSeries()) {
                    result.add(cursor.getSeriesId());
                }
            }
            return result;
        }

        @Override
        public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
            DataSourcePreconditions.checkProvider(NAME, parent);
            throw new IllegalArgumentException("Invalid hierarchy");
        }

        @Override
        public TsCursor<DataSet> getData(DataSource dataSource, TsInformationType type) throws IllegalArgumentException, IOException {
            DataSourcePreconditions.checkProvider(NAME, dataSource);
            return cursorOf(TYPE_PARAM.get(dataSource), type).map(dataSetFunc(dataSource));
        }

        @Override
        public TsCursor<DataSet> getData(DataSet dataSet, TsInformationType type) throws IllegalArgumentException, IOException {
            DataSourcePreconditions.checkProvider(NAME, dataSet);
            if (!dataSet.getKind().equals(DataSet.Kind.SERIES)) {
                throw new IllegalArgumentException("Invalid hierarchy");
            }
            int seriesIndex = INDEX_PARAM.get(dataSet);
            return cursorOf(TYPE_PARAM.get(dataSet.getDataSource()), type).filter(o -> o == seriesIndex).map(o -> dataSet);
        }

        private static IO.Function<Integer, DataSet> dataSetFunc(DataSource dataSource) {
            DataSet.Builder b = DataSet.builder(dataSource, DataSet.Kind.SERIES);
            return o -> b.put(INDEX_PARAM, o).build();
        }

        private TsCursor<Integer> cursorOf(DataType dt, TsInformationType type) throws IOException {
            Iterator<Integer> iter = seriesIndexIterator(dt);
            switch (dt) {
                case NORMAL:
                    sleep(dt, type);
                    return TsCursor.from(iter, dataFunc(normalData, type), metaFunc(dt, type), labelFunc(dt));
                case FAILING_META:
                    sleep(dt, type);
                    if (type.encompass(TsInformationType.MetaData)) {
                        throw new IOException("Cannot load meta");
                    }
                    return TsCursor.from(iter);
                case FAILING_DATA:
                    sleep(dt, type);
                    if (type.encompass(TsInformationType.Data)) {
                        throw new IOException("Cannot load data");
                    }
                    return TsCursor.from(iter);
                case FAILING_DEF:
                    sleep(dt, type);
                    if (type.encompass(TsInformationType.Definition)) {
                        throw new IOException("Cannot load definition");
                    }
                    return TsCursor.from(iter);
                case UPDATING:
                    return TsCursor.from(iter, dataFunc(updatingData, type), metaFunc(dt, type), labelFunc(dt));
                case SLOW:
                    log.log(Level.INFO, "Getting data %s - %s", new Object[]{dt, type});
                    sleep(dt, type);
                    return TsCursor.from(iter, dataFunc(slowData, type), metaFunc(dt, type), labelFunc(dt));
                default:
                    throw new IllegalArgumentException("Invalid data type");
            }
        }

        private static Iterator<Integer> seriesIndexIterator(DataType dt) {
            return IntStream.range(0, dt.getSeriesCount()).iterator();
        }

        private static Function<Integer, TsData> dataFunc(Function<Integer, TsData> delegate, TsInformationType type) {
            return type.encompass(TsInformationType.Data)
                    ? delegate
                    : o -> TsData.empty("Data not requested");
        }

        private static Function<Integer, Map<String, String>> metaFunc(DataType dt, TsInformationType type) {
            return type.encompass(TsInformationType.MetaData)
                    ? o -> {
                        Map<String, String> result = new HashMap<>();
                        result.put("Type", dt.name());
                        result.put("Index", String.valueOf(o));
                        result.put("Sleep meta", String.valueOf(dt.getSleepDuration(TsInformationType.MetaData)));
                        result.put("Sleep data", String.valueOf(dt.getSleepDuration(TsInformationType.Data)));
                        return Collections.unmodifiableMap(result);
                    }
                    : o -> Collections.emptyMap();
        }

        private static Function<Integer, String> labelFunc(DataType dt) {
            return o -> dt.name() + "#" + o;
        }

        private void sleep(DataType dt, TsInformationType type) {
            try {
                TimeUnit.MILLISECONDS.sleep(dt.getSleepDuration(type));
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        private static Function<Integer, TsData> shiftingValues(List<TsData> list) {
            return o -> {
                TsData result = shiftValues(list.get(o));
                list.set(o, result);
                return result;
            };
        }

        private static TsData shiftValues(TsData input) {
            if (!input.isEmpty()) {
                double[] values = input.getValues().toArray();
                double first = values[0];
                System.arraycopy(values, 1, values, 0, values.length - 1);
                values[values.length - 1] = first;
                return TsData.ofInternal(input.getStart(), values);
            }
            return input;
        }

        private static List<TsData> createData(DataType dt) {
            return IntStream
                    .range(0, dt.getSeriesCount())
                    .mapToObj(o -> createData(dt, o))
                    .collect(Collectors.toList());
        }

        private static TsData createData(DataType dt, int seriesIndex) {
            TsDomain domain = dt.getDomain(seriesIndex);
            return TsData.ofInternal(domain.getStartPeriod(), vals(new Random(0), domain.getLength()));
        }

        private static double[] vals(Random rnd, int obsCount) {
            double[] data = new double[obsCount];
            double cur = rnd.nextDouble() + 100;
            for (int i = 0; i < obsCount; ++i) {
                cur = cur + rnd.nextDouble() - .5;
                data[i] = cur;
            }
            return data;
        }
    }

    private enum DataType {

        NORMAL(doms(
                new TsUnit[]{TsUnit.YEAR, TsUnit.HALF_YEAR, TsUnit.QUARTER, TsUnit.MONTH, TsUnit.DAY},
                new int[]{0, 1, 24, 60, 120})
        ),
        FAILING_DATA(60, 120),
        FAILING_META(60, 120),
        FAILING_DEF(60, 120),
        UPDATING(0, 1, 24, 60, 120),
        SLOW(60, 120);

        private final TsDomain[] domains;

        private DataType(int... obsCounts) {
            this.domains = doms(new TsUnit[]{TsUnit.MONTH}, obsCounts);
        }

        private DataType(TsDomain... domains) {
            this.domains = domains;
        }

        public int getSeriesCount() {
            return domains.length;
        }

        public TsDomain getDomain(int seriesIndex) {
            return domains[seriesIndex];
        }

        public long getSleepDuration(TsInformationType type) {
            switch (this) {
                case NORMAL:
                    return type.needsData() ? 2000 : 150;
                case FAILING_META:
                    return type.needsData() ? 2000 : 150;
                case FAILING_DATA:
                    return type.needsData() ? 2000 : 150;
                case FAILING_DEF:
                    return type.needsData() ? 2000 : 150;
                case UPDATING:
                    return 0;
                case SLOW:
                    return type.needsData() ? 5000 : 1000;
                default:
                    throw new RuntimeException();
            }
        }

        private static TsDomain[] doms(TsUnit[] units, int[] sizes) {
            return Stream
                    .of(units)
                    .flatMap(x -> IntStream.of(sizes).mapToObj(o -> dom(x, o)))
                    .toArray(TsDomain[]::new);
        }

        private static TsDomain dom(TsUnit unit, int size) {
            return TsDomain.of(TsPeriod.of(unit, 0), size);
        }
    }
}
