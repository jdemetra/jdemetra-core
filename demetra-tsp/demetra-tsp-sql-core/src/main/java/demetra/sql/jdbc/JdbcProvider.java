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
package demetra.sql.jdbc;

import demetra.sql.HasSqlProperties;
import demetra.sql.SqlTableAsCubeResource;
import demetra.timeseries.TsProvider;
import demetra.tsprovider.*;
import demetra.tsprovider.cube.*;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.IOCacheFactoryLoader;
import demetra.tsprovider.util.ResourcePool;
import internal.sql.jdbc.JdbcParam;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.jdbc.SqlConnectionSupplier;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(TsProvider.class)
public final class JdbcProvider implements DataSourceLoader<JdbcBean>, HasSqlProperties {

    private static final String NAME = "JNDI-JDBC";

    @lombok.experimental.Delegate
    private final HasSqlProperties properties;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<JdbcBean> beanSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public JdbcProvider() {
        ResourcePool<CubeConnection> pool = CubeSupport.newConnectionPool();
        JdbcParam param = new JdbcParam.V1();

        this.properties = HasSqlProperties.of(SqlConnectionSupplier::ofJndi, pool::clear);
        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(NAME, pool.asFactory(o -> openConnection(o, properties, param)), param::getCubeIdParam);
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, pool::clear);
    }

    @Override
    public String getDisplayName() {
        return "JDBC resource";
    }

    private static CubeConnection openConnection(DataSource key, HasSqlProperties properties, JdbcParam param) {
        JdbcBean bean = param.get(key);

        SqlTableAsCubeResource sqlResource = SqlTableAsCubeResource.of(properties.getConnectionSupplier(), bean.getDatabase(), bean.getTable(), toRoot(bean), toDataParams(bean), bean.getCube().getObsGathering(), bean.getCube().getLabel());

        CubeConnection result = TableAsCubeConnection.of(sqlResource);
        return BulkCubeConnection.of(result, bean.getCache(), IOCacheFactoryLoader.get());
    }

    private static CubeId toRoot(JdbcBean bean) {
        return CubeId.root(bean.getCube().getDimensions());
    }

    private static TableDataParams toDataParams(JdbcBean bean) {
        return TableDataParams.builder()
                .periodColumn(bean.getCube().getTimeDimension())
                .valueColumn(bean.getCube().getMeasure())
                .versionColumn(bean.getCube().getVersion())
                .obsFormat(bean.getCube().getObsFormat())
                .build();
    }
}
