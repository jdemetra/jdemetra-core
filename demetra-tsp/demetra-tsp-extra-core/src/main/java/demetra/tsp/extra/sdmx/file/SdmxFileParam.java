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
import demetra.tsprovider.util.PropertyHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
interface SdmxFileParam extends DataSource.Converter<SdmxFileBean> {

    String getVersion();

    DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException;

    final class V1 implements SdmxFileParam {

        @lombok.experimental.Delegate
        private final DataSource.Converter<SdmxFileBean> converter =
                SdmxFileBeanHandler
                        .builder()
                        .file(PropertyHandler.onFile("f", new File("")))
                        .structureFile(PropertyHandler.onFile("s", new File("")))
                        .dialect(PropertyHandler.onString("j", ""))
                        .dimensions(PropertyHandler.onStringList("d", Collections.emptyList(), ','))
                        .labelAttribute(PropertyHandler.onString("l", ""))
                        .build()
                        .asDataSourceConverter();

        @Override
        public String getVersion() {
            return "v1";
        }

        @Override
        public DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException {
            return CubeSupport.idBySeparator(connection.getRoot(), ".", "k");
        }
    }

    @lombok.Builder(toBuilder = true)
    final class SdmxFileBeanHandler implements PropertyHandler<SdmxFileBean> {

        @lombok.NonNull
        private final PropertyHandler<File> file;

        @lombok.NonNull
        private final PropertyHandler<File> structureFile;

        @lombok.NonNull
        private final PropertyHandler<String> dialect;

        @lombok.NonNull
        private final PropertyHandler<List<String>> dimensions;

        @lombok.NonNull
        private final PropertyHandler<String> labelAttribute;

        @Override
        public @NonNull SdmxFileBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            SdmxFileBean result = new SdmxFileBean();
            result.setFile(file.get(properties));
            result.setStructureFile(structureFile.get(properties));
            result.setDialect(dialect.get(properties));
            result.setDimensions(dimensions.get(properties));
            result.setLabelAttribute(labelAttribute.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable SdmxFileBean value) {
            if (value != null) {
                file.set(properties, value.getFile());
                structureFile.set(properties, value.getStructureFile());
                dialect.set(properties, value.getDialect());
                dimensions.set(properties, value.getDimensions());
                labelAttribute.set(properties, value.getLabelAttribute());
            }
        }
    }
}
