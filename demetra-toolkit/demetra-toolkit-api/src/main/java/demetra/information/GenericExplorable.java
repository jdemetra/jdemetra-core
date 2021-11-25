/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.information;

import demetra.util.WildCards;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
public interface GenericExplorable extends Explorable{
    /**
     * Indicates that the provider can provide information on the mentioned item
     *
     * @param id Information item
     * @return
     */
    @Override
    default boolean contains(String id) {
        Class<? extends Explorable> cl = this.getClass();
        return InformationExtractors.contains(cl, id);
    }

    /**
     * Gets the dictionary of all the possible results
     *
     * @param compact
     * @return
     */
    @Override
    default Map<String, Class> getDictionary() {
        Class<? extends Explorable> cl = this.getClass();
        LinkedHashMap<String, Class> dic = new LinkedHashMap();
        InformationExtractors.fillDictionary(cl, null, dic, true);
        return dic;
    }

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
    @Override
    default <T> T getData(String id, Class<T> tclass) {
        return InformationExtractors.getData(this.getClass(), this, id, tclass);
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
    @Override
    default <T> Map<String, T> searchAll(String pattern, Class<T> tclass) {
        Map<String, T> rslt = new LinkedHashMap<>();
        Class<? extends Explorable> cl = this.getClass();
        InformationExtractors.searchAll(cl, this, new WildCards(pattern), tclass, rslt);
        return rslt;
    }

}
