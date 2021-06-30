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
package internal.sql.jdbc;

import demetra.sql.jdbc.JdbcBean;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.BulkCubeConfig;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.Param;
import internal.util.Strings;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public interface JdbcParam extends Param<DataSource, JdbcBean> {

    String getVersion();

    @NonNull
    Param<DataSet, CubeId> getCubeIdParam(@NonNull CubeId root);

    final class V1 implements JdbcParam {

        private static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(",");

        private final Function<CharSequence, Stream<String>> dimensionSplitter = o -> Strings.splitToStream(',', o).map(String::trim).filter(Strings::isNotEmpty);
        private final Function<Stream<CharSequence>, String> dimensionJoiner = o -> o.collect(COMMA_JOINER);

        private final Param<DataSource, String> dbName = Param.onString("", "dbName");
        private final Param<DataSource, String> tableName = Param.onString("", "tableName");
        private final Param<DataSource, List<String>> dimColumns = Param.onStringList(Collections.emptyList(), "dimColumns", dimensionSplitter, dimensionJoiner);
        private final Param<DataSource, String> periodColumn = Param.onString("", "periodColumn");
        private final Param<DataSource, String> valueColumn = Param.onString("", "valueColumn");
        private final Param<DataSource, ObsFormat> dataFormat = Param.onObsFormat(ObsFormat.DEFAULT, "locale", "datePattern", "numberPattern");
        private final Param<DataSource, String> versionColumn = Param.onString("", "versionColumn");
        private final Param<DataSource, String> labelColumn = Param.onString("", "labelColumn");
        private final Param<DataSource, ObsGathering> obsGathering = Param.onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final Param<DataSource, BulkCubeConfig> cacheConfig = Param.onBulkCubeConfig(BulkCubeConfig.of(Duration.ofMinutes(5), 1), "cacheTtl", "cacheDepth");

        @Override
        public String getVersion() {
            return "20131203";
        }

        @Override
        public JdbcBean defaultValue() {
            JdbcBean result = new JdbcBean();
            result.setDatabase(dbName.defaultValue());
            result.setTable(tableName.defaultValue());
            result.setDimColumns(dimColumns.defaultValue());
            result.setPeriodColumn(periodColumn.defaultValue());
            result.setValueColumn(valueColumn.defaultValue());
            result.setObsFormat(dataFormat.defaultValue());
            result.setVersionColumn(versionColumn.defaultValue());
            result.setLabelColumn(labelColumn.defaultValue());
            result.setObsGathering(obsGathering.defaultValue());
            result.setCacheConfig(cacheConfig.defaultValue());
            return result;
        }

        @Override
        public JdbcBean get(DataSource dataSource) {
            JdbcBean result = new JdbcBean();
            result.setDatabase(dbName.get(dataSource));
            result.setTable(tableName.get(dataSource));
            result.setDimColumns(dimColumns.get(dataSource));
            result.setPeriodColumn(periodColumn.get(dataSource));
            result.setValueColumn(valueColumn.get(dataSource));
            result.setObsFormat(dataFormat.get(dataSource));
            result.setVersionColumn(versionColumn.get(dataSource));
            result.setLabelColumn(labelColumn.get(dataSource));
            result.setObsGathering(obsGathering.get(dataSource));
            result.setCacheConfig(cacheConfig.get(dataSource));
            return result;
        }

        @Override
        public void set(IConfig.Builder<?, DataSource> builder, JdbcBean value) {
            dbName.set(builder, value.getDatabase());
            tableName.set(builder, value.getTable());
            dimColumns.set(builder, value.getDimColumns());
            periodColumn.set(builder, value.getPeriodColumn());
            valueColumn.set(builder, value.getValueColumn());
            dataFormat.set(builder, value.getObsFormat());
            versionColumn.set(builder, value.getVersionColumn());
            labelColumn.set(builder, value.getLabelColumn());
            obsGathering.set(builder, value.getObsGathering());
            cacheConfig.set(builder, value.getCacheConfig());
        }

        @Override
        public Param<DataSet, CubeId> getCubeIdParam(CubeId root) {
            return CubeSupport.idByName(root);
        }
    }
}
