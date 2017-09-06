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
        String monikerId = moniker.getId();
        if (monikerId == null) {
            throw new IllegalArgumentException(moniker.toString());
        }
        return toDataSource(monikerId);
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);
        String monikerId = moniker.getId();
        if (monikerId == null) {
            throw new IllegalArgumentException(moniker.toString());
        }
        return toDataSet(monikerId);
    }

    private DataSource toDataSource(String monikerId) {
        LegacyFileId id = LegacyFileId.parse(monikerId);
        return id != null ? toSource(new File(id.getFile())) : null;
    }

    private DataSet toDataSet(String monikerId) {
        try {
            LegacySpreadSheetId id = LegacySpreadSheetId.parse(monikerId);
            DataSource dataSource = toSource(new File(id.getFile()));
            if (dataSource == null) {
                return null;
            }
            if (id.isCollection()) {
                return toCollection(dataSource, id.getSheetName());
            }
            return id.getIndexSeries() >= 0
                    ? toSeries(dataSource, id.getSheetName(), id.getIndexSeries())
                    : toSeries(dataSource, id.getSheetName(), id.getSeriesName());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private DataSource toSource(File file) {
        SpreadSheetBean bean = new SpreadSheetBean();
        bean.setFile(file);
        return DataSource.builder(providerName, param.getVersion())
                .put(param, bean)
                .build();
    }

    private DataSet toCollection(DataSource dataSource, String sheetName) {
        return DataSet.builder(dataSource, DataSet.Kind.COLLECTION)
                .put(param.getSheetParam(dataSource), cleanSheetName(sheetName))
                .build();
    }

    private DataSet toSeries(DataSource dataSource, String sheetName, String seriesName) {
        return DataSet.builder(dataSource, DataSet.Kind.SERIES)
                .put(param.getSheetParam(dataSource), cleanSheetName(sheetName))
                .put(param.getSeriesParam(dataSource), seriesName)
                .build();
    }

    private DataSet toSeries(DataSource dataSource, String sheetName, int seriesIndex) {
        // not supported
        return null;
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
