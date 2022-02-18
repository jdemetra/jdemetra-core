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
package demetra.tsp.extra.sdmx.file;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.CubeConnection;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import internal.util.Strings;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
interface SdmxFileParam extends DataSource.Converter<SdmxFileBean> {

    String getVersion();

    DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException;

    final class V1 implements SdmxFileParam {

        private static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(",");

        private final Function<CharSequence, Stream<String>> dimensionSplitter = o -> Strings.splitToStream(',', o).map(String::trim).filter(Strings::isNotEmpty);
        private final Function<Stream<CharSequence>, String> dimensionJoiner = o -> o.collect(COMMA_JOINER);

        private final Property<File> file = Property.of("f", new File(""), Parser.onFile(), Formatter.onFile());
        private final Property<File> structureFile = Property.of("s", new File(""), Parser.onFile(), Formatter.onFile());
        private final Property<String> dialect = Property.of("j", "", Parser.onString(), Formatter.onString());
        private final Property<List<String>> dimensionIds = Property.of("d", Collections.emptyList(), Parser.onStringList(dimensionSplitter), Formatter.onStringList(dimensionJoiner));
        private final Property<String> labelAttribute = Property.of("l", "", Parser.onString(), Formatter.onString());

        @Override
        public String getVersion() {
            return "v1";
        }

        @Override
        public SdmxFileBean getDefaultValue() {
            SdmxFileBean result = new SdmxFileBean();
            result.setFile(file.getDefaultValue());
            result.setStructureFile(structureFile.getDefaultValue());
            result.setDialect(dialect.getDefaultValue());
            result.setDimensions(dimensionIds.getDefaultValue());
            result.setLabelAttribute(labelAttribute.getDefaultValue());
            return result;
        }

        @Override
        public SdmxFileBean get(DataSource dataSource) {
            SdmxFileBean result = new SdmxFileBean();
            result.setFile(file.get(dataSource::getParameter));
            result.setStructureFile(structureFile.get(dataSource::getParameter));
            result.setDialect(dialect.get(dataSource::getParameter));
            result.setDimensions(dimensionIds.get(dataSource::getParameter));
            result.setLabelAttribute(labelAttribute.get(dataSource::getParameter));
            return result;
        }

        @Override
        public void set(DataSource.Builder builder, SdmxFileBean value) {
            file.set(builder::parameter, value.getFile());
            structureFile.set(builder::parameter, value.getStructureFile());
            dialect.set(builder::parameter, value.getDialect());
            dimensionIds.set(builder::parameter, value.getDimensions());
            labelAttribute.set(builder::parameter, value.getLabelAttribute());
        }

        @Override
        public DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException {
            return CubeSupport.idBySeparator(connection.getRoot(), ".", "k");
        }
    }
}
