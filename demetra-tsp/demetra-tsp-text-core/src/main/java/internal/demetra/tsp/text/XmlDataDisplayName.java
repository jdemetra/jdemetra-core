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
import demetra.tsp.text.XmlBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataDisplayName;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.ResourceMap;

import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class XmlDataDisplayName implements HasDataDisplayName {

    private final String providerName;
    private final XmlParam param;
    private final ResourceMap<List<TsCollection>> resources;

    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        XmlBean bean = param.get(dataSource);
        return bean.getFile().getPath();
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Integer collectionIndex = param.getCollectionParam(dataSet.getDataSource()).get(dataSet);
        Integer seriesIndex = param.getSeriesParam(dataSet.getDataSource()).get(dataSet);

        List<TsCollection> data = resources.peek(dataSet.getDataSource());
        if (data != null) {
            TsCollection col = data.get(collectionIndex);
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return col.getName();
                case SERIES:
                    return col.getName() + " - " + col.get(seriesIndex).getName();
            }
        } else {
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return String.valueOf(collectionIndex);
                case SERIES:
                    return collectionIndex + " - " + seriesIndex;
            }
        }
        return "";
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Integer collectionIndex = param.getCollectionParam(dataSet.getDataSource()).get(dataSet);
        Integer seriesIndex = param.getSeriesParam(dataSet.getDataSource()).get(dataSet);

        List<TsCollection> data = resources.peek(dataSet.getDataSource());
        if (data != null) {
            TsCollection col = data.get(collectionIndex);
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return col.getName();
                case SERIES:
                    return col.get(seriesIndex).getName();
            }
        } else {
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return String.valueOf(collectionIndex);
                case SERIES:
                    return String.valueOf(seriesIndex);
            }
        }
        return "";
    }
}
