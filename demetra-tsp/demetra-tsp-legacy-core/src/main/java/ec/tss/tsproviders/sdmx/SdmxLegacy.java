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
package ec.tss.tsproviders.sdmx;

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.utils.Parsers;
import ec.tss.tsproviders.utils.Parsers.Parser;
import java.io.File;

/**
 *
 * @author Kristof Bayens
 */
final class SdmxLegacy {

    private SdmxLegacy() {
        // static class
    }

    static DataSource newDataSource(SdmxDataSourceId id) {
        SdmxBean bean = new SdmxBean();
        bean.setFactory(id.getFactory());
        bean.setFile(new File(id.getUrl()));
        return bean.toDataSource(SdmxProvider.SOURCE, SdmxProvider.VERSION);
    }

    static Parsers.Parser<DataSource> dataSourceParser() {
        return new Parser<DataSource>() {
            @Override
            public DataSource parse(CharSequence input) throws NullPointerException {
                SdmxDataSourceId id = SdmxDataSourceId.parse(input);
                return id != null ? newDataSource(id) : null;
            }
        };
    }

    static Parsers.Parser<DataSet> dataSetParser() {
        final Parsers.Parser<DataSource> tmp = dataSourceParser();
        return new Parser<DataSet>() {
            @Override
            public DataSet parse(CharSequence input) throws NullPointerException {
                SdmxDataSourceId id = SdmxDataSourceId.parse(input.toString());
                if (id == null) {
                    return null;
                }
                DataSource dataSource = tmp.parse(id.getUrl());
                return dataSource != null ? DataSet.of(dataSource, DataSet.Kind.SERIES) : null;
            }
        };
    }
}
