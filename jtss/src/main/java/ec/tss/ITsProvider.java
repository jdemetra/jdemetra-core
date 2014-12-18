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

package ec.tss;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.ServiceDefinition;

/**
 * Generic interface for providers of time series.
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@ServiceDefinition
public interface ITsProvider {

    /**
     * Requests to the provider to clear any cached information.
     */
    void clearCache();

    /**
     * Called when the provider is removed from the TSFactory. Should be used to
     * release any used resources.
     */
    void dispose();

    /**
     * Synchronous query of the information about a tscollection (with or
     * without data)
     * 
     * @param collection
     * @return
     * @see TSCollectionInformation
     */
    boolean get(TsCollectionInformation collection);

    // / <summary>
    // /
    // / </summary>
    // / <param name="identifier">Identifier of the series</param>
    // / <returns>The kind of information returned by the provider</returns>
    /**
     * Synchronous query of the information about a ts (with or without data)
     * 
     * @param tsinfo
     *            The requested information. This object is used as input
     * @return
     * @see TSInformation
     */
    boolean get(TsInformation tsinfo);

    // / <summary>
    // / Asynchronous mode
    // / </summary>
    /**
     * Gets the asynchronous mode of the provider.
     * 
     * @return The way the provider might handle asynchronous calls. If the
     *         provider doesn't support asynchronous calls, the returned value
     *         is TSAsyncMode.None
     * @see TSAsyncMode
     */
    TsAsyncMode getAsyncMode();

    /**
     * Gets the identifier of the source.
     * 
     * @return A unique identifier for the provider. The source is used as first
     *         string in the moniker of a series provided by this provider.
     */
    String getSource();

    /**
     * Checks if the provider is able to provide information.
     * 
     * @return True if the provider is available, false otherwise (missing
     *         modules, missing or unavailable resources...)
     */
    boolean isAvailable();

    /**
     * Asynchronous query of a ts (metadata and tsdata) When the ts is
     * available, the provider must use the services of the tseventmanager to
     * dispatch information, through a "TSEvent".
     * 
     * @param ts
     * @param type
     * @return
     * @see TSCollectionInformation
     */
    boolean queryTs(TsMoniker ts, TsInformationType type);

    /**
     * Asynchronous query of the information about a tscollection (with or
     * without data). When the tscollection is available, the provider must use
     * the services of the tseventmanager to dispatch information, through a
     * "TSCollectionEvent".
     * 
     * @param collection
     * @param info
     * @return
     * 
     * @see TSInformationType
     */
    boolean queryTsCollection(TsMoniker collection, TsInformationType info);
}
