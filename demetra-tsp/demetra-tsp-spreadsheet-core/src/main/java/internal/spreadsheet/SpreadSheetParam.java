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
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.TsProviders;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

/**
 * @author Philippe Charles
 */
public interface SpreadSheetParam extends DataSource.Converter<SpreadSheetBean> {

    @NonNull String getVersion();

    DataSet.@NonNull Converter<String> getSheetParam();

    DataSet.@NonNull Converter<String> getSeriesParam();

    final class V1 implements SpreadSheetParam {

        private final Property<File> file = Property.of("file", new File(""), Parser.onFile(), Formatter.onFile());
        private final DataSource.Converter<ObsFormat> obsFormat = TsProviders.onObsFormat(ObsFormat.DEFAULT, "locale", "datePattern", "numberPattern");
        private final DataSource.Converter<ObsGathering> obsGathering = TsProviders.onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final Property<String> sheet = Property.of("sheetName", "", Parser.onString(), Formatter.onString());
        private final Property<String> series = Property.of("seriesName", "", Parser.onString(), Formatter.onString());

        @Override
        public String getVersion() {
            return "20111201";
        }

        @Override
        public SpreadSheetBean getDefaultValue() {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(file.getDefaultValue());
            result.setObsFormat(obsFormat.getDefaultValue());
            result.setObsGathering(obsGathering.getDefaultValue());
            return result;
        }

        @Override
        public SpreadSheetBean get(DataSource dataSource) {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(file.get(dataSource::getParameter));
            result.setObsFormat(obsFormat.get(dataSource));
            result.setObsGathering(obsGathering.get(dataSource));
            return result;
        }

        @Override
        public void set(DataSource.Builder builder, SpreadSheetBean value) {
            file.set(builder::parameter, value.getFile());
            // FIXME: NPE bug in jtss
            if (value.getObsFormat() != null) {
                obsFormat.set(builder, value.getObsFormat());
            }
            if (value.getObsGathering() != null) {
                obsGathering.set(builder, value.getObsGathering());
            }
        }

        @Override
        public DataSet.Converter<String> getSheetParam() {
            return TsProviders.dataSetConverterOf(sheet);
        }

        @Override
        public DataSet.Converter<String> getSeriesParam() {
            return TsProviders.dataSetConverterOf(series);
        }
    }
}
