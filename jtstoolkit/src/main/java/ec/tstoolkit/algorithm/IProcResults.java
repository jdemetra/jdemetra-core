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
import ec.tstoolkit.utilities.WildCards;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic description of the results of a processing
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface IProcResults {

    /**
     * Indicates that the provider can provide information on the mentioned item
     *
     * @param id Information item
     * @return
     */
    boolean contains(String id);

    /**
     * Gets the dictionary of all the possible results
     *
     * @param compact True if the dictionary contains wild cards, false otherwise
     * @return The Map with the output (identifier and class)
     */
    Map<String, Class> getDictionary(boolean compact);
    
    @Deprecated
    default Map<String, Class> getDictionary(){
        return getDictionary(false);
    }

    /**
     * . The identifier and the type should come from the dictionary provided by
     * this object
     *
     * @param <T>
     * @param id Name of information
     * @param tclass
     * @return
     */
    <T> T getData(String id, Class<T> tclass);

    /**
     * Gets all information corresponding to the given pattern and with the
     * right type
     *
     * @param <T>
     * @param pattern The pattern
     * @param tclass Type of information
     * @return
     */
    default <T> Map<String, T> searchAll(String pattern, Class<T> tclass) {
        Map<String, T> rslt = new LinkedHashMap<>();
        Map<String, Class> dic = getDictionary(false);
        WildCards wc = new WildCards(pattern);
        for (Map.Entry<String, Class> x : dic.entrySet()) {
            if (wc.match(x.getKey())) {
                if (tclass.isAssignableFrom(x.getValue())) {
                    rslt.put(x.getKey(), (T) getData(x.getKey(), tclass));
                }
            }
        }
        return rslt;
    }

    List<ProcessingInformation> getProcessingInformation();
}
