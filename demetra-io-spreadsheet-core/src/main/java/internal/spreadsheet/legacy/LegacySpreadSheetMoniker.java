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
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.TsMoniker;
import demetra.tsprovider.legacy.LegacyFileId;
import demetra.tsprovider.util.DataSourcePreconditions;
import internal.spreadsheet.SpreadSheetParam;
import java.io.File;

/**
 *
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
    public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        LegacyFileId id = LegacyFileId.parse(moniker.getId());
        return id != null ? toDataSource(new File(id.getFile())) : null;
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        LegacySpreadSheetId id = LegacySpreadSheetId.parse(moniker.getId());
        return id != null ? toDataSet(id) : null;
    }

    private DataSet toDataSet(LegacySpreadSheetId id) {
        DataSource source = toDataSource(new File(id.getFile()));
        if (id.isCollection()) {
            return DataSet.builder(source, DataSet.Kind.COLLECTION)
                    .put(param.getSheetParam(source), cleanSheetName(id.getSheetName()))
                    .build();
        }
        if (id.isSeriesByIndex()) {
            // not supported
            return null;
        }
        return DataSet.builder(source, DataSet.Kind.SERIES)
                .put(param.getSheetParam(source), cleanSheetName(id.getSheetName()))
                .put(param.getSeriesParam(source), id.getSeriesName())
                .build();
    }

    private DataSource toDataSource(File file) {
        SpreadSheetBean bean = new SpreadSheetBean();
        bean.setFile(file);
        return DataSource.builder(providerName, param.getVersion())
                .put(param, bean)
                .build();
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
