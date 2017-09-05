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
import ec.tss.tsproviders.HasDataDisplayName;
import ec.tss.tsproviders.utils.DataSourcePreconditions;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tss.tsproviders.utils.ObsGathering;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Philippe Charles
 */
final class SpreadSheetDataDisplayName implements HasDataDisplayName {

    private final String providerName;
    private final SpreadSheetParam resource;

    SpreadSheetDataDisplayName(String providerName, SpreadSheetParam resource) {
        this.providerName = providerName;
        this.resource = resource;
    }

    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        SpreadSheetBean2 bean = resource.get(dataSource);
        return bean.getFile().getPath() + toString(bean.getObsGathering());
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
        return resource.getSheetParam(dataSet.getDataSource()).get(dataSet);
    }

    private String getSeriesId(DataSet dataSet) {
        return resource.getSeriesParam(dataSet.getDataSource()).get(dataSet);
    }

    private static String toString(ObsGathering gathering) {
        return TsFrequency.Undefined == gathering.getFrequency() ? "" : (" (" + gathering.getFrequency() + "/" + gathering.getAggregationType() + ")");
    }
}
