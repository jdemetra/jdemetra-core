/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
package ec.tss.tsproviders.common.txt;

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.legacy.FileDataSourceId;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.Parsers;
import internal.demetra.tsp.text.TxtLegacyId;

/**
 * @author Philippe Charles
 */
final class TxtLegacy {

    private TxtLegacy() {
        // static class
    }

    static DataSource newDataSource(FileDataSourceId id) {
        return id.fill(new TxtBean()).toDataSource(TxtProvider.SOURCE, TxtProvider.VERSION);
    }

    static Parsers.Parser<DataSource> dataSourceParser() {
        return new Parsers.Parser<DataSource>() {
            @Override
            public DataSource parse(CharSequence input) throws NullPointerException {
                FileDataSourceId id = FileDataSourceId.parse(input);
                return id != null ? newDataSource(id) : null;
            }
        };
    }

    static Parsers.Parser<DataSet> dataSetParser() {
        final IParser<DataSource> tmp = dataSourceParser();
        return new Parsers.Parser<DataSet>() {
            @Override
            public DataSet parse(CharSequence input) throws NullPointerException {
                TxtLegacyId id = TxtLegacyId.parse(input.toString());
                if (id == null) {
                    return null;
                }
                DataSource dataSource = tmp.parse(id.getFile());
                if (dataSource == null) {
                    return null;
                }
                if (!id.isSeries()) {
                    return DataSet.of(dataSource, DataSet.Kind.COLLECTION);
                }
                return DataSet.builder(dataSource, DataSet.Kind.SERIES)
                        .put(TxtProvider.Z_SERIESINDEX, id.getSeriesIndex())
                        .build();
            }
        };
    }
}
