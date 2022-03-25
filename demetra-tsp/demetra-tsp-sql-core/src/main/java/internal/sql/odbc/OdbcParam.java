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
package internal.sql.odbc;

import demetra.sql.odbc.OdbcBean;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.*;
import demetra.tsprovider.legacy.LegacyHandler;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.PropertyHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static demetra.tsprovider.util.PropertyHandler.onString;
import static demetra.tsprovider.util.PropertyHandler.onStringList;

/**
 * @author Philippe Charles
 */
public interface OdbcParam extends DataSource.Converter<OdbcBean> {

    String getVersion();

    DataSet.@NonNull Converter<CubeId> getIdParam(@NonNull CubeConnection connection) throws IOException;

    final class V1 implements OdbcParam {

        private static final BulkCube DEFAULT_BULK = BulkCube.builder().ttl(Duration.ofMinutes(5)).depth(1).build();

        @lombok.experimental.Delegate
        private final DataSource.Converter<OdbcBean> converter =
                OdbcBeanHandler
                        .builder()
                        .dsn(onString("dbName", ""))
                        .table(PropertyHandler.onString("tableName", ""))
                        .cube(
                                TableAsCubeHandler
                                        .builder()
                                        .dimensions(onStringList("dimColumns", Collections.emptyList(), ','))
                                        .timeDimension(onString("periodColumn", ""))
                                        .measure(onString("valueColumn", ""))
                                        .format(LegacyHandler.onObsFormat("locale", "datePattern", "numberPattern", ObsFormat.getSystemDefault()))
                                        .version(onString("versionColumn", ""))
                                        .label(onString("labelColumn", ""))
                                        .gathering(LegacyHandler.onObsGathering("frequency", "aggregationType", "cleanMissing", ObsGathering.DEFAULT))
                                        .build()
                        )
                        .cache(
                                BulkCubeHandler
                                        .builder()
                                        .ttl(PropertyHandler.onDurationInMillis("cacheTtl", DEFAULT_BULK.getTtl()))
                                        .depth(PropertyHandler.onInteger("cacheDepth", DEFAULT_BULK.getDepth()))
                                        .build()
                        )
                        .build()
                        .asDataSourceConverter();

        @Override
        public String getVersion() {
            return "20111201";
        }

        @Override
        public DataSet.Converter<CubeId> getIdParam(CubeConnection connection) throws IOException {
            return CubeSupport.idByName(connection.getRoot());
        }
    }

    @lombok.Builder(toBuilder = true)
    final class OdbcBeanHandler implements PropertyHandler<OdbcBean> {

        @lombok.NonNull
        private final PropertyHandler<String> dsn;

        @lombok.NonNull
        private final PropertyHandler<String> table;

        @lombok.NonNull
        private final PropertyHandler<TableAsCube> cube;

        @lombok.NonNull
        private final PropertyHandler<BulkCube> cache;

        @Override
        public @NonNull OdbcBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            OdbcBean result = new OdbcBean();
            result.setDsn(dsn.get(properties));
            result.setTable(table.get(properties));
            result.setCube(cube.get(properties));
            result.setCache(cache.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable OdbcBean value) {
            if (value != null) {
                dsn.set(properties, value.getDsn());
                table.set(properties, value.getTable());
                cube.set(properties, value.getCube());
                cache.set(properties, value.getCache());
            }
        }
    }
}
