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

import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataMoniker;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class FallbackDataMoniker implements HasDataMoniker {

    private final HasDataMoniker first;
    private final HasDataMoniker second;

    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        return first.toMoniker(dataSource);
    }

    @Override
    public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
        return first.toMoniker(dataSet);
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        DataSource result = first.toDataSource(moniker);
        return result != null ? result : second.toDataSource(moniker);
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSet result = first.toDataSet(moniker);
        return result != null ? result : second.toDataSet(moniker);
    }
}
