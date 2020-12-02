/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.processing;

import nbbrd.design.Development;
import demetra.util.WildCards;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic description of the results of a processing.
 * Except in trivial cases, all processing should generate results that
 * implements this interface
 * The returned objects should belong to the general API
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface ProcResults {

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
     * @return
     */
    Map<String, Class> getDictionary();

    /**
     * Gets information related to the specified id
     * The identifier and the type should come from the dictionary provided by
     * this object
     *
     * @param <T>
     * @param id Name of information
     * @param tclass Class of the information
     * @return null if this information is not available
     */
    <T> T getData(String id, Class<T> tclass);
    
    default Object getData(String id){
        return getData(id, Object.class);
    }

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
        Map<String, Class> dic = getDictionary();
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

    public static final char SEP = '.';


    /**
     * Concatenates arrays of strings without separator
     * 
     * @param s
     * @return 
     */
    public static String paste(String... s) {
        switch (s.length) {
            case 0:
                return "";
            case 1:
                return s[0];
            default:
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length; ++i) {
                    builder.append(s[i]);
                }
                return builder.toString();
        }
    }
    
    /**
     * Concatenates arrays of strings with the default separator ('.')
     *
     * @param s
     * @return
     */
    public static String spaste(String... s) {
        switch (s.length) {
            case 0:
                return "";
            case 1:
                return s[0];
            default:
                boolean first = true;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length; ++i) {
                    if (s[i] != null) {
                        if (!first) {
                            builder.append(SEP);
                        } else {
                            first = false;
                        }
                        builder.append(s[i]);
                    }
                }
                return builder.toString();
        }
    }

}
