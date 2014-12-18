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

package ec.tstoolkit.algorithm;

import ec.tstoolkit.design.Development;

/**
 * Generic interface that describes the making of output from a processing.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IOutput<D extends IProcDocument<?, ?, ?>> {

    /**
     * Name of the output generating tool
     *
     * @return
     */
    String getName();

    /**
     * Controls the availability of the tool
     *
     * @return
     */
    boolean isAvailable();

    /**
     * Creates the actual output for a given document
     *
     * @param document The considered document
     */
    void process(D document)throws Exception;

    /**
     * Starts the processing of the item identified by the given id;
     *
     * @param id The identifier of handled information
     */
    void start(Object context)throws Exception;

    /**
     * Finishes the processing of the item identified by the given id;
     *
     * @param id The identifier of handled information
     */
    void end(Object context)throws Exception;
}
