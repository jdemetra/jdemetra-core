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
package demetra.tsp.extra.sdmx.web;

import demetra.timeseries.Ts;
import demetra.timeseries.TsInformationType;
import demetra.timeseries.TsProvider;
import demetra.tsp.extra.sdmx.HasSdmxProperties;
import demetra.tsprovider.*;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.cube.BulkCubeConnection;
import demetra.tsprovider.cube.CubeConnection;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.IOCacheFactoryLoader;
import demetra.tsprovider.util.ResourcePool;
import internal.tsp.extra.sdmx.SdmxCubeConnection;
import internal.tsp.extra.sdmx.SdmxPropertiesSupport;
import nbbrd.io.Resource;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
@ServiceProvider(TsProvider.class)
public final class SdmxWebProvider implements DataSourceLoader<SdmxWebBean>, HasSdmxProperties<SdmxWebManager> {

    private static final String NAME = "DOTSTAT";

    private final AtomicBoolean displayCodes;

    @lombok.experimental.Delegate
    private final HasSdmxProperties<SdmxWebManager> properties;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<SdmxWebBean> beanSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public SdmxWebProvider() {
        this.displayCodes = new AtomicBoolean(false);

        ResourcePool<CubeConnection> pool = CubeSupport.newConnectionPool();
        SdmxWebParam param = new SdmxWebParam.V1();

        this.properties = SdmxPropertiesSupport.of(SdmxWebManager::ofServiceLoader, pool::clear);
        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(NAME, pool.asFactory(o -> openConnection(o, properties, param)), param::getCubeIdParam);
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, pool::clear);
    }

    @Override
    public String getDisplayName() {
        return "SDMX Web Services";
    }

    public boolean isDisplayCodes() {
        return displayCodes.get();
    }

    public void setDisplayCodes(boolean displayCodes) {
        boolean old = this.displayCodes.get();
        if (this.displayCodes.compareAndSet(old, displayCodes)) {
            clearCache();
        }
    }

    @NonNull
    public Ts getTs(@NonNull SdmxWebBean bean, @NonNull Key key, @NonNull TsInformationType type) throws IOException {
        String[] dimsInNaturalOrder = dimsInNaturalOrder(getSdmxManager(), bean.getSource(), DataflowRef.parse(bean.getFlow()));

        if (!Key.ALL.equals(key) && key.size() != dimsInNaturalOrder.length) {
            throw new IllegalArgumentException("Invalid key '" + key + "'");
        }

        // we need to sort dimensions by wildcard in key because of provider issue
        Comparator<Integer> c = (l, r) -> key.isWildcard(l) ? (key.isWildcard(r) ? 0 : 1) : (key.isWildcard(r) ? -1 : 0);
        List<String> sortedDims = IntStream.range(0, dimsInNaturalOrder.length).boxed()
                .sorted(c.thenComparingInt(o -> o))
                .map(o -> dimsInNaturalOrder[o])
                .collect(Collectors.toList());
        bean.setDimensions(sortedDims);

        DataSet.Builder b = DataSet.builder(encodeBean(bean), key.isSeries() ? DataSet.Kind.SERIES : DataSet.Kind.COLLECTION);
        if (!Key.ALL.equals(key)) {
            IntStream.range(0, dimsInNaturalOrder.length)
                    .filter(o -> !key.isWildcard(o))
                    .forEach(o -> b.parameter(dimsInNaturalOrder[o], key.get(o)));
        }
        return getTs(toMoniker(b.build()), type);
    }

    private static String[] dimsInNaturalOrder(SdmxWebManager supplier, String source, DataflowRef flow) throws IOException {
        try (Connection c = supplier.getConnection(source)) {
            return c.getStructure(flow)
                    .getDimensions()
                    .stream()
                    .map(Dimension::getId)
                    .toArray(String[]::new);
        }
    }

    private static CubeConnection openConnection(DataSource source, HasSdmxProperties<SdmxWebManager> properties, SdmxWebParam param) throws IOException {
        SdmxWebBean bean = param.get(source);

        DataflowRef flow = DataflowRef.parse(bean.getFlow());

        Connection conn = properties.getSdmxManager().getConnection(bean.getSource());
        try {
            CubeConnection result = SdmxCubeConnection.of(conn, flow, bean.getDimensions(), bean.getLabelAttribute(), bean.getSource());
            return BulkCubeConnection.of(result, bean.getCache(), IOCacheFactoryLoader.get());
        } catch (IOException ex) {
            Resource.ensureClosed(ex, conn);
            throw ex;
        }
    }
}
