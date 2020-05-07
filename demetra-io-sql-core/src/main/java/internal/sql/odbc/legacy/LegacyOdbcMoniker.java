/*
 * Copyright 2018 National Bank of Belgium
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
package internal.sql.odbc.legacy;

import demetra.design.DemetraPlusLegacy;
import demetra.sql.odbc.OdbcBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataMoniker;
import demetra.timeseries.TsMoniker;
import demetra.tsprovider.util.DataSourcePreconditions;
import internal.sql.odbc.OdbcParam;
import java.util.Arrays;

/**
 *
 * @author Philippe Charles
 */
@DemetraPlusLegacy
@lombok.AllArgsConstructor(staticName = "of")
public final class LegacyOdbcMoniker implements HasDataMoniker {

    private final String providerName;
    private final OdbcParam param;

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

        LegacyOdbcId id = LegacyOdbcId.parse(moniker.getId());
        return id != null ? toDataSource(id) : null;
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        LegacyOdbcId id = LegacyOdbcId.parse(moniker.getId());
        return id != null ? toDataSet(id) : null;
    }

    private DataSource toDataSource(LegacyOdbcId id) {
        return DataSource
                .builder(providerName, param.getVersion())
                .put(param, toBean(id))
                .build();
    }

    private OdbcBean toBean(LegacyOdbcId id) {
        OdbcBean result = new OdbcBean();
        result.setDsn(id.getDbName());
        result.setTable(id.getTable());
        result.setDimColumns(Arrays.asList(id.getDomainColumn(), id.getSeriesColumn()));
        result.setPeriodColumn(id.getPeriodColumn());
        result.setValueColumn(id.getValueColumn());
        return result;
    }

    private DataSet toDataSet(LegacyOdbcId id) {
        DataSource source = toDataSource(id);
        if (id.isMultiCollection()) {
            return DataSet.of(source, DataSet.Kind.COLLECTION);
        }
        if (id.isCollection()) {
            return DataSet
                    .builder(source, DataSet.Kind.COLLECTION)
                    .put(id.getDomainColumn(), id.getDomainName())
                    .build();
        }
        return DataSet
                .builder(source, DataSet.Kind.SERIES)
                .put(id.getDomainColumn(), id.getDomainName())
                .put(id.getSeriesColumn(), id.getSeriesName())
                .build();
    }
}
