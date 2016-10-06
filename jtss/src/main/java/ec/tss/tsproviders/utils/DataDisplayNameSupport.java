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

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataDisplayName;
import ec.tss.tsproviders.utils.Formatters.Formatter;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Supporting class for {@link HasDataDisplayName}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public final class DataDisplayNameSupport implements HasDataDisplayName {

    /**
     * Creates a new instance of HasDataDisplayName using uri parser/formatter.
     *
     * @param providerName a non-null provider name
     * @return a non-null instance
     */
    @Nonnull
    public static HasDataDisplayName usingUri(@Nonnull String providerName) {
        return new DataDisplayNameSupport(providerName, DataSource.uriFormatter(), DataSet.uriFormatter());
    }

    private final String providerName;
    private final Formatter<DataSource> dataSourceFormatter;
    private final Formatter<DataSet> dataSetFormatter;

    private DataDisplayNameSupport(String providerName, Formatter<DataSource> dataSourceFormatter, Formatter<DataSet> dataSetFormatter) {
        this.providerName = Objects.requireNonNull(providerName);
        this.dataSourceFormatter = dataSourceFormatter;
        this.dataSetFormatter = dataSetFormatter;
    }

    //<editor-fold defaultstate="collapsed" desc="HasDataDisplayName">
    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        String result = dataSourceFormatter.formatAsString(dataSource);
        if (result == null) {
            throw new IllegalArgumentException("Cannot format DataSource");
        }
        return result;
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        String result = dataSetFormatter.formatAsString(dataSet);
        if (result == null) {
            throw new IllegalArgumentException("Cannot format DataSet");
        }
        return result;
    }
    //</editor-fold>
}
