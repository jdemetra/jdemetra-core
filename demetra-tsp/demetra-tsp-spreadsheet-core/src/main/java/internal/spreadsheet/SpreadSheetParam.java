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
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.ObsFormat;
import demetra.timeseries.util.ObsGathering;
import static demetra.tsprovider.util.Params.onFile;
import static demetra.tsprovider.util.Params.onObsFormat;
import static demetra.tsprovider.util.Params.onObsGathering;
import static demetra.tsprovider.util.Params.onString;
import java.io.File;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public interface SpreadSheetParam extends IParam<DataSource, SpreadSheetBean> {

    @NonNull
    String getVersion();

    @NonNull
    IParam<DataSet, String> getSheetParam(DataSource dataSource);

    @NonNull
    IParam<DataSet, String> getSeriesParam(DataSource dataSource);

    public static final class V1 implements SpreadSheetParam {

        private final IParam<DataSource, File> file = onFile(new File(""), "file");
        private final IParam<DataSource, ObsFormat> obsFormat = onObsFormat(ObsFormat.DEFAULT, "locale", "datePattern", "numberPattern");
        private final IParam<DataSource, ObsGathering> obsGathering = onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final IParam<DataSet, String> sheet = onString("", "sheetName");
        private final IParam<DataSet, String> series = onString("", "seriesName");

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
        public IParam<DataSet, String> getSheetParam(DataSource dataSource) {
            return sheet;
        }

        @Override
        public IParam<DataSet, String> getSeriesParam(DataSource dataSource) {
            return series;
        }
    }
}
