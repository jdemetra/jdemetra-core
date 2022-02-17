/*
 * Copyright 2017 National Bank of Belgium
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
package internal.spreadsheet.legacy;

import demetra.design.DemetraPlusLegacy;
import demetra.spreadsheet.SpreadSheetBean;
import demetra.timeseries.TsMoniker;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.legacy.LegacyFileId;
import demetra.tsprovider.util.DataSourcePreconditions;
import internal.spreadsheet.SpreadSheetParam;

import java.io.File;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@DemetraPlusLegacy
@lombok.AllArgsConstructor(staticName = "of")
public final class LegacySpreadSheetMoniker implements HasDataMoniker {

    private final String providerName;
    private final SpreadSheetParam param;

    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        throw new IllegalArgumentException("Not supported yet.");
    }

    @Override
    public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        throw new IllegalArgumentException("Not supported yet.");
    }

    @Override
    public Optional<DataSource> toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        LegacyFileId id = LegacyFileId.parse(moniker.getId());
        return id != null ? Optional.of(toDataSource(new File(id.getFile()))) : Optional.empty();
    }

    @Override
    public Optional<DataSet> toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        LegacySpreadSheetId id = LegacySpreadSheetId.parse(moniker.getId());
        return id != null ? Optional.of(toDataSet(id)) : Optional.empty();
    }

    private DataSet toDataSet(LegacySpreadSheetId id) {
        DataSource source = toDataSource(new File(id.getFile()));
        if (id.isCollection()) {
            DataSet.Builder result = DataSet.builder(source, DataSet.Kind.COLLECTION);
            param.getSheetParam().set(result, cleanSheetName(id.getSheetName()));
            return result.build();
        }
        if (id.isSeriesByIndex()) {
            // not supported
            return null;
        }
        DataSet.Builder result = DataSet.builder(source, DataSet.Kind.SERIES);
        param.getSheetParam().set(result, cleanSheetName(id.getSheetName()));
        param.getSeriesParam().set(result, id.getSeriesName());
        return result.build();
    }

    private DataSource toDataSource(File file) {
        SpreadSheetBean bean = new SpreadSheetBean();
        bean.setFile(file);
        DataSource.Builder result = DataSource.builder(providerName, param.getVersion());
        param.set(result, bean);
        return result.build();
    }

    private static String cleanSheetName(String name) {
        // probably we should change the CharSet, but it is not very clear how and which one
        int l = name.lastIndexOf('$');
        if (l < 0) {
            return name;
        }
        name = name.substring(0, l);
        if (name.charAt(0) == '\'') {
            name = name.substring(1);
        }
        return name.replace('#', '.');
    }
}
