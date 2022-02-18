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
import demetra.tsprovider.cube.*;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.TsProviders;
import demetra.util.List2;
import internal.util.Strings;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
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
public interface JdbcParam extends DataSource.Converter<JdbcBean> {

    String getVersion();

    DataSet.@NonNull Converter<CubeId> getCubeIdParam(@NonNull CubeConnection connection) throws IOException;

    final class V1 implements JdbcParam {

        private static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(",");

        private final Function<CharSequence, Stream<String>> dimensionSplitter = o -> Strings.splitToStream(',', o).map(String::trim).filter(Strings::isNotEmpty);
        private final Function<Stream<CharSequence>, String> dimensionJoiner = o -> o.collect(COMMA_JOINER);

        private final Property<String> dbName = Property.of("dbName", "", Parser.onString(), Formatter.onString());
        private final Property<String> tableName = Property.of("tableName", "", Parser.onString(), Formatter.onString());
        private final Property<List<String>> dimColumns = Property.of("dimColumns", List2.copyOf(Collections.emptyList()), Parser.onStringList(dimensionSplitter), Formatter.onStringList(dimensionJoiner));
        private final Property<String> periodColumn = Property.of("periodColumn", "", Parser.onString(), Formatter.onString());
        private final Property<String> valueColumn = Property.of("valueColumn", "", Parser.onString(), Formatter.onString());
        private final DataSource.Converter<ObsFormat> dataFormat = TsProviders.onObsFormat(ObsFormat.DEFAULT, "locale", "datePattern", "numberPattern");
        private final Property<String> versionColumn = Property.of("versionColumn", "", Parser.onString(), Formatter.onString());
        private final Property<String> labelColumn = Property.of("labelColumn", "", Parser.onString(), Formatter.onString());
        private final DataSource.Converter<ObsGathering> obsGathering = TsProviders.onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final DataSource.Converter<BulkCube> cacheConfig = TsProviders.onBulkCube(BulkCube.builder().ttl(Duration.ofMinutes(5)).depth(1).build(), "cacheTtl", "cacheDepth");

        @Override
        public String getVersion() {
            return "20131203";
        }

        @Override
        public JdbcBean getDefaultValue() {
            JdbcBean result = new JdbcBean();
            result.setDatabase(dbName.getDefaultValue());
            result.setTable(tableName.getDefaultValue());
            result.setCube(TableAsCube
                    .builder()
                    .dimensions(dimColumns.getDefaultValue())
                    .timeDimension(periodColumn.getDefaultValue())
                    .measure(valueColumn.getDefaultValue())
                    .obsFormat(dataFormat.getDefaultValue())
                    .version(versionColumn.getDefaultValue())
                    .label(labelColumn.getDefaultValue())
                    .obsGathering(obsGathering.getDefaultValue())
                    .build());
            result.setCache(cacheConfig.getDefaultValue());
            return result;
        }

        @Override
        public JdbcBean get(DataSource dataSource) {
            JdbcBean result = new JdbcBean();
            result.setDatabase(dbName.get(dataSource::getParameter));
            result.setTable(tableName.get(dataSource::getParameter));
            result.setCube(TableAsCube
                    .builder()
                    .dimensions(dimColumns.get(dataSource::getParameter))
                    .timeDimension(periodColumn.get(dataSource::getParameter))
                    .measure(valueColumn.get(dataSource::getParameter))
                    .obsFormat(dataFormat.get(dataSource))
                    .version(versionColumn.get(dataSource::getParameter))
                    .label(labelColumn.get(dataSource::getParameter))
                    .obsGathering(obsGathering.get(dataSource))
                    .build());
            result.setCache(cacheConfig.get(dataSource));
            return result;
        }

        @Override
        public void set(DataSource.Builder builder, JdbcBean value) {
            dbName.set(builder::parameter, value.getDatabase());
            tableName.set(builder::parameter, value.getTable());
            dimColumns.set(builder::parameter, value.getCube().getDimensions());
            periodColumn.set(builder::parameter, value.getCube().getTimeDimension());
            valueColumn.set(builder::parameter, value.getCube().getMeasure());
            dataFormat.set(builder, value.getCube().getObsFormat());
            versionColumn.set(builder::parameter, value.getCube().getVersion());
            labelColumn.set(builder::parameter, value.getCube().getLabel());
            obsGathering.set(builder, value.getCube().getObsGathering());
            cacheConfig.set(builder, value.getCache());
        }

        @Override
        public DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException {
            return CubeSupport.idByName(connection.getRoot());
        }
    }
}
