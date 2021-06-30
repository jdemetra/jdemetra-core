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
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

/**
 * @author Philippe Charles
 */
public interface SpreadSheetParam extends Param<DataSource, SpreadSheetBean> {

    @NonNull
    String getVersion();

    @NonNull
    Param<DataSet, String> getSheetParam(DataSource dataSource);

    @NonNull
    Param<DataSet, String> getSeriesParam(DataSource dataSource);

    final class V1 implements SpreadSheetParam {

        private final Param<DataSource, File> file = Param.onFile(new File(""), "file");
        private final Param<DataSource, ObsFormat> obsFormat = Param.onObsFormat(ObsFormat.DEFAULT, "locale", "datePattern", "numberPattern");
        private final Param<DataSource, ObsGathering> obsGathering = Param.onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final Param<DataSet, String> sheet = Param.onString("", "sheetName");
        private final Param<DataSet, String> series = Param.onString("", "seriesName");

        @Override
        public String getVersion() {
            return "20111201";
        }

        @Override
        public SpreadSheetBean defaultValue() {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(file.defaultValue());
            result.setObsFormat(obsFormat.defaultValue());
            result.setObsGathering(obsGathering.defaultValue());
            return result;
        }

        @Override
        public SpreadSheetBean get(DataSource dataSource) {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(file.get(dataSource));
            result.setObsFormat(obsFormat.get(dataSource));
            result.setObsGathering(obsGathering.get(dataSource));
            return result;
        }

        @Override
        public void set(IConfig.Builder<?, DataSource> builder, SpreadSheetBean value) {
            file.set(builder, value.getFile());
            // FIXME: NPE bug in jtss
            if (value.getObsFormat() != null) {
                obsFormat.set(builder, value.getObsFormat());
            }
            if (value.getObsGathering() != null) {
                obsGathering.set(builder, value.getObsGathering());
            }
        }

        @Override
        public Param<DataSet, String> getSheetParam(DataSource dataSource) {
            return sheet;
        }

        @Override
        public Param<DataSet, String> getSeriesParam(DataSource dataSource) {
            return series;
        }
    }
}
