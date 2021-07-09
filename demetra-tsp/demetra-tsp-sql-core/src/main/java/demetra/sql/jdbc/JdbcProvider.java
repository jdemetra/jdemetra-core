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

import nbbrd.design.DirectImpl;
import demetra.sql.HasSqlProperties;
import internal.sql.jdbc.JdbcParam;
import demetra.sql.SqlTableAsCubeResource;
import demetra.timeseries.TsProvider;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceLoader;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.HasDataSourceBean;
import demetra.tsprovider.HasDataSourceMutableList;
import demetra.tsprovider.cube.BulkCubeAccessor;
import demetra.tsprovider.cube.CubeAccessor;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.cube.TableAsCubeAccessor;
import demetra.tsprovider.cube.TableDataParams;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.ResourceMap;
import java.io.IOException;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.util.JCacheFactory;

/**
 *
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
        ResourceMap<CubeAccessor> accessors = ResourceMap.newInstance();
        JdbcParam param = new JdbcParam.V1();

        this.properties = HasSqlProperties.of(SqlConnectionSupplier::ofJndi, accessors::clear);
        this.mutableListSupport = HasDataSourceMutableList.of(NAME, accessors::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(NAME, new JdbcCubeResource(accessors, properties, param));
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, accessors::clear);
    }

    @Override
    public String getDisplayName() {
        return "JDBC resource";
    }

    @lombok.AllArgsConstructor
    private static final class JdbcCubeResource implements CubeSupport.Resource {

        private final ResourceMap<CubeAccessor> accessors;
        private final HasSqlProperties properties;
        private final JdbcParam param;

        @Override
        public CubeAccessor getAccessor(DataSource dataSource) throws IOException {
            return accessors.computeIfAbsent(dataSource, this::load);
        }

        @Override
        public DataSet.Converter<CubeId> getIdParam(CubeId root) {
            return param.getCubeIdParam(root);
        }

        private CubeAccessor load(DataSource key) {
            JdbcBean bean = param.get(key);

            SqlTableAsCubeResource sqlResource = SqlTableAsCubeResource.of(properties.getConnectionSupplier(), bean.getDatabase(), bean.getTable(), toRoot(bean), toDataParams(bean), bean.getObsGathering(), bean.getLabelColumn());

            CubeAccessor result = TableAsCubeAccessor.of(sqlResource);
            return BulkCubeAccessor.of(result, bean.getCacheConfig(), JCacheFactory.bulkCubeCacheOf(key::toString));
        }

        private static CubeId toRoot(JdbcBean bean) {
            return CubeId.root(bean.getDimColumns());
        }

        private static TableDataParams toDataParams(JdbcBean bean) {
            return TableDataParams.builder()
                    .periodColumn(bean.getPeriodColumn())
                    .valueColumn(bean.getValueColumn())
                    .versionColumn(bean.getVersionColumn())
                    .obsFormat(bean.getObsFormat())
                    .build();
        }
    }
}
