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
package demetra.tsprovider.cursor;

import demetra.design.ThreadSafe;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines the ability to create a time series cursor on a DataSource or a
 * DataSet. Note that the implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface HasTsCursor {

    /**
     * Creates a cursor from a DataSource.
     *
     * @param dataSource the DataSource
     * @param type the type of data to return
     * @return a new cursor
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     * provider.
     * @throws IOException if an internal exception prevented data retrieval.
     */
    @NonNull
    TsCursor<DataSet> getData(@NonNull DataSource dataSource, @NonNull TsInformationType type) throws IllegalArgumentException, IOException;

    /**
     * Creates a cursor from a DataSet.
     *
     * @param dataSet the DataSet
     * @param type the type of data to return
     * @return a new cursor
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     * provider.
     * @throws IOException if an internal exception prevented data retrieval.
     */
    @NonNull
    TsCursor<DataSet> getData(@NonNull DataSet dataSet, @NonNull TsInformationType type) throws IllegalArgumentException, IOException;
}
