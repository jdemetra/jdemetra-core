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
package demetra.tsprovider.util;

import demetra.tsprovider.TsMoniker;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A collection of preconditions for DataSource, DataSet and TsMoniker.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class DataSourcePreconditions {

    /**
     * Ensures that a data source has a specific provider name.
     *
     * @param providerName
     * @param dataSource
     * @return
     * @throws IllegalArgumentException
     */
    @NonNull
    public DataSource checkProvider(@NonNull String providerName, @NonNull DataSource dataSource) throws IllegalArgumentException {
        checkProvider(providerName, dataSource.getProviderName());
        return dataSource;
    }

    /**
     * Ensures that a dataset has a specific provider name.
     *
     * @param providerName
     * @param dataSet
     * @return
     * @throws IllegalArgumentException
     */
    @NonNull
    public DataSet checkProvider(@NonNull String providerName, @NonNull DataSet dataSet) throws IllegalArgumentException {
        checkProvider(providerName, dataSet.getDataSource().getProviderName());
        return dataSet;
    }

    /**
     * Ensures that a moniker has a specific provider name.
     *
     * @param providerName
     * @param moniker
     * @return
     * @throws IllegalArgumentException
     */
    @NonNull
    public TsMoniker checkProvider(@NonNull String providerName, @NonNull TsMoniker moniker) throws IllegalArgumentException {
        checkProvider(providerName, moniker.getSource());
        return moniker;
    }

    private void checkProvider(@NonNull String expected, @Nullable String found) throws IllegalArgumentException {
        if (!expected.equals(found)) {
            throw new IllegalArgumentException("Invalid provider name; expected: '" + expected + "' found: '" + found + "'");
        }
    }
}
