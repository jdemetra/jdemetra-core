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
package demetra.spreadsheet;

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.IConfig;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.ObsGathering;
import static ec.tss.tsproviders.utils.Params.onDataFormat;
import static ec.tss.tsproviders.utils.Params.onFile;
import static ec.tss.tsproviders.utils.Params.onObsGathering;
import static ec.tss.tsproviders.utils.Params.onString;
import java.io.File;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
interface SpreadSheetParam extends IParam<DataSource, SpreadSheetBean2> {

    @Nonnull
    String getVersion();

    @Nonnull
    IParam<DataSet, String> getSheetParam(DataSource dataSource);

    @Nonnull
    IParam<DataSet, String> getSeriesParam(DataSource dataSource);

    static final class V1 implements SpreadSheetParam {

        private final IParam<DataSource, File> file;
        private final IParam<DataSource, DataFormat> obsFormat;
        private final IParam<DataSource, ObsGathering> obsGathering;
        private final IParam<DataSet, String> sheet;
        private final IParam<DataSet, String> series;

        V1() {
            this.file = onFile(new File(""), "file");
            this.obsFormat = onDataFormat(DataFormat.DEFAULT, "locale", "datePattern", "numberPattern");
            this.obsGathering = onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
            this.sheet = onString("", "sheetName");
            this.series = onString("", "seriesName");
        }

        @Override
        public String getVersion() {
            return "20111201";
        }

        @Override
        public SpreadSheetBean2 defaultValue() {
            SpreadSheetBean2 result = new SpreadSheetBean2();
            result.setFile(file.defaultValue());
            result.setObsFormat(obsFormat.defaultValue());
            result.setObsGathering(obsGathering.defaultValue());
            return result;
        }

        @Override
        public SpreadSheetBean2 get(DataSource dataSource) {
            SpreadSheetBean2 result = new SpreadSheetBean2();
            result.setFile(file.get(dataSource));
            result.setObsFormat(obsFormat.get(dataSource));
            result.setObsGathering(obsGathering.get(dataSource));
            return result;
        }

        @Override
        public void set(IConfig.Builder<?, DataSource> builder, SpreadSheetBean2 value) {
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
