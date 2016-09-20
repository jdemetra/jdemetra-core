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
package ec.tss.tsproviders.utils;

import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataMoniker;
import ec.tss.tsproviders.utils.Formatters.Formatter;
import ec.tss.tsproviders.utils.Parsers.Parser;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Supporting class for {@link HasDataMoniker}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public final class DataMonikerSupport implements HasDataMoniker {

    /**
     * Creates a new instance of this class using uri parser/formatter.
     *
     * @param providerName a non-null provider name
     * @return a non-null instance
     */
    @Nonnull
    public static DataMonikerSupport usingUri(@Nonnull String providerName) {
        return new DataMonikerSupport(providerName, DataSource.uriFormatter(), DataSet.uriFormatter(), DataSource.uriParser(), DataSet.uriParser());
    }

    private final String providerName;
    private final Formatter<DataSource> dataSourceFormatter;
    private final Formatter<DataSet> dataSetFormatter;
    private final Parser<DataSource> dataSourceParser;
    private final Parser<DataSet> dataSetParser;

    private DataMonikerSupport(String providerName, Formatter<DataSource> dataSourceFormatter, Formatter<DataSet> dataSetFormatter, Parser<DataSource> dataSourceParser, Parser<DataSet> dataSetParser) {
        this.providerName = Objects.requireNonNull(providerName);
        this.dataSourceFormatter = dataSourceFormatter;
        this.dataSetFormatter = dataSetFormatter;
        this.dataSourceParser = dataSourceParser;
        this.dataSetParser = dataSetParser;
    }

    //<editor-fold defaultstate="collapsed" desc="HasDataMoniker">
    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        String id = dataSourceFormatter.formatAsString(dataSource);
        if (id == null) {
            throw new IllegalArgumentException("Cannot format DataSource");
        }
        return new TsMoniker(providerName, id);
    }

    @Override
    public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        String id = dataSetFormatter.formatAsString(dataSet);
        if (id == null) {
            throw new IllegalArgumentException("Cannot format DataSource");
        }
        return new TsMoniker(providerName, id);
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);
        String id = moniker.getId();
        return id != null ? dataSetParser.parse(id) : null;
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);
        String id = moniker.getId();
        return id != null ? dataSourceParser.parse(id) : null;
    }
    //</editor-fold>
}
