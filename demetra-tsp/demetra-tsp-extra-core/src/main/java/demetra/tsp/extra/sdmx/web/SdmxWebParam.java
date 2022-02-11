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
package demetra.tsp.extra.sdmx.web;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.BulkCube;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.util.TsProviders;
import internal.util.Strings;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;

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
interface SdmxWebParam extends DataSource.Converter<SdmxWebBean> {

    String getVersion();

    DataSet.Converter<CubeId> getCubeIdParam(CubeId root);

    final class V1 implements SdmxWebParam {

        private static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(",");

        private final Function<CharSequence, Stream<String>> dimensionSplitter = o -> Strings.splitToStream(',', o).map(String::trim).filter(Strings::isNotEmpty);
        private final Function<Stream<CharSequence>, String> dimensionJoiner = o -> o.collect(COMMA_JOINER);

        private final Property<String> dbName = Property.of("dbName", "", Parser.onString(), Formatter.onString());
        private final Property<String> flowRef = Property.of("tableName", "", Parser.onString(), Formatter.onString());
        private final Property<List<String>> dimensionIds = Property.of("dimColumns", Collections.emptyList(), Parser.onStringList(dimensionSplitter), Formatter.onStringList(dimensionJoiner));
        private final Property<String> labelAttribute = Property.of("l", "", Parser.onString(), Formatter.onString());
        private final DataSource.Converter<BulkCube> cacheConfig = TsProviders.onBulkCube(BulkCube.builder().ttl(Duration.ofMinutes(5)).depth(1).build(), "cacheTtl", "cacheDepth");

        @Override
        public String getVersion() {
            return "20150203";
        }

        @Override
        public SdmxWebBean getDefaultValue() {
            SdmxWebBean result = new SdmxWebBean();
            result.setSource(dbName.getDefaultValue());
            result.setFlow(flowRef.getDefaultValue());
            result.setDimensions(dimensionIds.getDefaultValue());
            result.setLabelAttribute(labelAttribute.getDefaultValue());
            result.setCacheConfig(cacheConfig.getDefaultValue());
            return result;
        }

        @Override
        public SdmxWebBean get(DataSource dataSource) {
            SdmxWebBean result = new SdmxWebBean();
            result.setSource(dbName.get(dataSource::getParameter));
            result.setFlow(flowRef.get(dataSource::getParameter));
            result.setDimensions(dimensionIds.get(dataSource::getParameter));
            result.setLabelAttribute(labelAttribute.get(dataSource::getParameter));
            result.setCacheConfig(cacheConfig.get(dataSource));
            return result;
        }

        @Override
        public void set(DataSource.Builder builder, SdmxWebBean value) {
            dbName.set(builder::parameter, value.getSource());
            flowRef.set(builder::parameter, value.getFlow());
            dimensionIds.set(builder::parameter, value.getDimensions());
            labelAttribute.set(builder::parameter, value.getLabelAttribute());
            cacheConfig.set(builder, value.getCacheConfig());
        }

        @Override
        public DataSet.Converter<CubeId> getCubeIdParam(CubeId root) {
            return CubeSupport.idByName(root);
        }
    }
}
