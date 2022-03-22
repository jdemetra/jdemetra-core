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
package internal.spreadsheet;

import demetra.spreadsheet.SpreadSheetBean;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.legacy.LegacyHandler;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.PropertyHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
public interface SpreadSheetParam extends DataSource.Converter<SpreadSheetBean> {

    @NonNull String getVersion();

    DataSet.@NonNull Converter<String> getSheetParam();

    DataSet.@NonNull Converter<String> getSeriesParam();

    final class V1 implements SpreadSheetParam {

        @lombok.experimental.Delegate
        private final DataSource.Converter<SpreadSheetBean> converter =
                SpreadSheetBeanHandler
                        .builder()
                        .file(PropertyHandler.onFile("file", new File("")))
                        .format(LegacyHandler.onObsFormat("locale", "datePattern", "numberPattern", ObsFormat.getSystemDefault()))
                        .gathering(LegacyHandler.onObsGathering("frequency", "aggregationType", "cleanMissing", ObsGathering.DEFAULT))
                        .build()
                        .asDataSourceConverter();

        @lombok.Getter
        private final String version = "20111201";

        @lombok.Getter
        private final DataSet.Converter<String> sheetParam = PropertyHandler.onString("sheetName", "").asDataSetConverter();

        @lombok.Getter
        private final DataSet.Converter<String> seriesParam = PropertyHandler.onString("seriesName", "").asDataSetConverter();
    }

    @lombok.Builder(toBuilder = true)
    final class SpreadSheetBeanHandler implements PropertyHandler<SpreadSheetBean> {

        @lombok.NonNull
        private final PropertyHandler<File> file;

        @lombok.NonNull
        private final PropertyHandler<ObsFormat> format;

        @lombok.NonNull
        private final PropertyHandler<ObsGathering> gathering;

        @Override
        public @NonNull SpreadSheetBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(file.get(properties));
            result.setFormat(format.get(properties));
            result.setGathering(gathering.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable SpreadSheetBean value) {
            if (value != null) {
                file.set(properties, value.getFile());
                // FIXME: NPE bug in jtss
                if (value.getFormat() != null) {
                    format.set(properties, value.getFormat());
                }
                if (value.getGathering() != null) {
                    gathering.set(properties, value.getGathering());
                }
            }
        }
    }
}
