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
package internal.demetra.tsp.text;

import demetra.timeseries.TsCollection;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.tsp.text.TxtBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataDisplayName;
import demetra.tsprovider.util.DataSourcePreconditions;

import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class TxtDataDisplayName implements HasDataDisplayName {

    private final String providerName;
    private final TxtParam param;
    private final Function<DataSource, TsCollection> data;

    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        TxtBean bean = param.get(dataSource);
        return bean.getFile().getPath() + toString(bean.getObsGathering());
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Integer index = param.getSeriesParam().get(dataSet);

        TsCollection data = this.data.apply(dataSet.getDataSource());
        return data != null ? data.get(index).getName() : "Column " + index;
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) {
        return getDisplayName(dataSet);
    }

    private static String toString(ObsGathering gathering) {
        return TsUnit.UNDEFINED.equals(gathering.getUnit()) ? "" : String.format("(%s/%s)", gathering.getUnit(), gathering.getAggregationType());
    }
}
