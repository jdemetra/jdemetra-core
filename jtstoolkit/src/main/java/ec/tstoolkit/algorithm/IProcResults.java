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
import java.util.List;
import java.util.Map;

/**
 * Generic description of the results of a processing
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface IProcResults {
    /**
     * Indicates that the provider can provide information on the mentioned item
     * @param id Information item
     * @return 
     */
    boolean contains(String id);

    /**
     * Gets the dictionary of all the possible results
     * @return The Map with the output (identifier and class)
     */
    Map<String, Class> getDictionary();

    /**
     * Gets information corresponding to the requested id and with the right
     * type. The identifier and the type should come from the dictionary
     * provided by this object
     *
     * @param <T>
     * @param id Name of information
     * @param tclass Type of information
     * @return
     */
    <T> T getData(String id, Class<T> tclass);
    
    List<ProcessingInformation> getProcessingInformation();
}
