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
package ec.tss.tsproviders;

import ec.tss.ITsProvider;
import ec.tss.TsMoniker;
import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines a provider that is used to discover and browse DataSources and
 * DataSets. All the methods defined here are for consultation only. To allow
 * changes, a provider must implement {@link IDataSourceLoader}.
 *
 * @author Demortier Jeremy
 * @author Philippe Charles
 */
public interface IDataSourceProvider extends ITsProvider {

    /**
     * Gets a label for this provider.<br>Note that the result might change
     * according to the configuration of the provider.
     *
     * @return a non-null label.
     */
    @Nonnull
    String getDisplayName();

    /**
     * Gets the DataSources loaded by this provider.
     *
     * @return a list of DataSources; might be empty but never null.
     */
    @Nonnull
    List<DataSource> getDataSources();

    /**
     * Gets the children of the specified DataSource.
     *
     * @param dataSource
     * @return a list a DataSet; might be empty but never null.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     *                                  provider.
     * @throws IOException              if an internal exception prevented data retrieval.
     */
    @Nonnull
    List<DataSet> children(@Nonnull DataSource dataSource) throws IllegalArgumentException, IOException;

    /**
     * Gets the children of the specified DataSet.
     *
     * @param parent
     * @return a list of DataSet; might be empty but never null.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     *                                  provider.
     * @throws IOException              if an internal exception prevented data retrieval.
     */
    @Nonnull
    List<DataSet> children(@Nonnull DataSet parent) throws IllegalArgumentException, IOException;

    /**
     * Gets a label for the specified DataSource.<br>Note that the result might
     * change according to the configuration of the provider.
     *
     * @param dataSource
     * @return a non-null label.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     *                                  provider.
     */
    @Nonnull
    String getDisplayName(@Nonnull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Gets a full label of the specified DataSet. Use this method in a
     * list.<br>Note that the result might change according to the configuration
     * of the provider.
     *
     * @param dataSet
     * @return a non-null label.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     *                                  provider.
     */
    @Nonnull
    String getDisplayName(@Nonnull DataSet dataSet) throws IllegalArgumentException;

    /**
     * Gets a short label of the specified DataSet. Use this method in a
     * tree.<br>Note that the result might change according to the configuration
     * of the provider.
     *
     * @param dataSet
     * @return a non-null label.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     *                                  provider.
     */
    @Nonnull
    String getDisplayNodeName(@Nonnull DataSet dataSet) throws IllegalArgumentException;

    /**
     * Gets a label for an exception thrown by this provider.
     *
     * @param exception
     * @return a non-null label
     * @throws IllegalArgumentException if the exception doesn't belong to this
     *                                  provider.
     */
    @Nonnull
    String getDisplayName(@Nonnull IOException exception) throws IllegalArgumentException;

    /**
     * Adds a listener to the provider in order to receive change
     * notifications.<br>Note that the specified listener might be stored in a
     * {@link WeakHashMap} to avoid memory leak. It is up to you to keep it in a
     * strong ref to prevent garbage collection.
     *
     * @param listener
     */
    void addDataSourceListener(@Nonnull IDataSourceListener listener);

    /**
     * Removes a listener from the provider if that listener has been added.
     * Does nothing otherwise.
     *
     * @param listener
     */
    void removeDataSourceListener(@Nonnull IDataSourceListener listener);

    void reload(DataSource dataSource);

    /**
     * Creates a moniker from a DataSource. The resulting moniker can be used to
     * retrieve data.
     *
     * @param dataSource
     * @return a non-null moniker.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     *                                  provider.
     */
    @Nonnull
    TsMoniker toMoniker(@Nonnull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Creates a moniker from a DataSet. The resulting moniker can be used to
     * retrieve data.
     *
     * @param dataSet
     * @return a non-null moniker.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     *                                  provider.
     */
    @Nonnull
    TsMoniker toMoniker(@Nonnull DataSet dataSet) throws IllegalArgumentException;

    @Nullable
    DataSet toDataSet(@Nonnull TsMoniker moniker) throws IllegalArgumentException;

    @Nullable
    DataSource toDataSource(@Nonnull TsMoniker moniker) throws IllegalArgumentException;
}
