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
package internal.spreadsheet;

import demetra.spreadsheet.SpreadSheetBean;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataDisplayName;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.util.MultiLineNameUtil;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class SpreadSheetDataDisplayName implements HasDataDisplayName {

    private final String providerName;
    private final SpreadSheetParam resource;

    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        SpreadSheetBean bean = resource.get(dataSource);
        return bean.getFile().getPath() + toString(bean.getGathering());
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return getCollectionId(dataSet);
            case SERIES:
                return getCollectionId(dataSet) + MultiLineNameUtil.SEPARATOR + getSeriesId(dataSet);
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return getCollectionId(dataSet);
            case SERIES:
                return getSeriesId(dataSet);
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    private String getCollectionId(DataSet dataSet) {
        return resource.getSheetParam().get(dataSet);
    }

    private String getSeriesId(DataSet dataSet) {
        return resource.getSeriesParam().get(dataSet);
    }

    private static String toString(ObsGathering gathering) {
        return TsUnit.UNDEFINED.equals(gathering.getUnit()) ? "" : String.format("(%s/%s)", gathering.getUnit(), gathering.getAggregationType());
    }
}
