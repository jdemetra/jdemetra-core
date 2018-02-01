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
package demetra.sql.odbc;

import demetra.sql.SqlTableAsCubeResource;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceLoader;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.HasDataSourceBean;
import demetra.tsprovider.HasDataSourceMutableList;
import demetra.tsprovider.TsProvider;
import demetra.tsprovider.cube.CubeAccessor;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.cube.TableAsCubeAccessor;
import demetra.tsprovider.cube.TableDataParams;
import demetra.tsprovider.cursor.HasTsCursor;
import demetra.tsprovider.cursor.TsCursorAsProvider;
import demetra.tsprovider.util.CacheProvider;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.IParam;
import ec.tstoolkit.utilities.GuavaCaches;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openide.util.lookup.ServiceProvider;
import sql.util.SqlConnectionSupplier;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = TsProvider.class)
public final class OdbcProvider implements DataSourceLoader<OdbcBean> {

    private static final String NAME = "ODBCPRVDR";

    private final AtomicReference<SqlConnectionSupplier> connectionSupplier;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<OdbcBean> beanSupport;

    @lombok.experimental.Delegate(excludes = HasTsCursor.class)
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public OdbcProvider() {
        this.connectionSupplier = new AtomicReference(OdbcManager.getDefault().getConnectionSupplier());

        ConcurrentMap<DataSource, CubeAccessor> cache = CacheProvider.getDefault().softValuesCacheAsMap();
        OdbcParam param = new OdbcParam.V1();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, cache::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(new OdbcCubeResource(cache, connectionSupplier, param));
        this.tsSupport = TsCursorAsProvider.of(NAME, cubeSupport, monikerSupport, cache::clear);
    }

    @Override
    public String getDisplayName() {
        return "ODBC DSNs";
    }

    @Nonnull
    public SqlConnectionSupplier getConnectionSupplier() {
        return connectionSupplier.get();
    }

    public void setConnectionSupplier(@Nullable SqlConnectionSupplier connectionSupplier) {
        SqlConnectionSupplier old = this.connectionSupplier.get();
        if (this.connectionSupplier.compareAndSet(old, connectionSupplier != null ? connectionSupplier : OdbcManager.getDefault().getConnectionSupplier())) {
            clearCache();
        }
    }

    @lombok.AllArgsConstructor
    private static final class OdbcCubeResource implements CubeSupport.Resource {

        private final ConcurrentMap<DataSource, CubeAccessor> cache;
        private final AtomicReference<SqlConnectionSupplier> supplier;
        private final OdbcParam param;

        @Override
        public CubeAccessor getAccessor(DataSource dataSource) throws IOException {
            DataSourcePreconditions.checkProvider(NAME, dataSource);
            CubeAccessor result = cache.get(dataSource);
            if (result == null) {
                result = load(dataSource);
                cache.put(dataSource, result);
            }
            return result;
        }

        @Override
        public IParam<DataSet, CubeId> getIdParam(DataSource dataSource) throws IOException {
            CubeId root = getAccessor(dataSource).getRoot();
            // FIXME: compatibility with previous code
            return CubeSupport.idByName(root);
        }

        private CubeAccessor load(DataSource key) throws IOException {
            OdbcBean bean = param.get(key);
            SqlTableAsCubeResource result = SqlTableAsCubeResource.of(supplier.get(), bean.getDsn(), bean.getTable(), CubeId.root(bean.getDimColumns()), toDataParams(bean), bean.getObsGathering(), bean.getLabelColumn());
            return TableAsCubeAccessor.create(result).bulk(bean.getCacheDepth(), GuavaCaches.ttlCacheAsMap(bean.getCacheTtl()));
        }

        private static TableDataParams toDataParams(OdbcBean bean) {
            return TableDataParams.builder()
                    .periodColumn(bean.getPeriodColumn())
                    .valueColumn(bean.getValueColumn())
                    .versionColumn(bean.getVersionColumn())
                    .obsFormat(bean.getObsFormat())
                    .build();
        }
    }
}
