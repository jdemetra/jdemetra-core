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

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.utilities.Jdk6;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author pcuser
 */
public class ProcUtilities {


    public static InformationSet write(IProcResults rslts, List<String> items) {
        InformationSet info = new InformationSet();
        Map<String, Class> map = rslts.getDictionary();
        for (String s : items) {
            Class c = map.get(s);
            if (c != null) {
                Object obj = rslts.getData(s, c);
                if (s != null) {
                    info.add(InformationSet.split(s), obj);
                }
            }
        }
        return info;
    }

    public static InformationSet write(IProcResults rslts) {
        InformationSet info = new InformationSet();
        Map<String, Class> map = rslts.getDictionary();
        for (Entry<String, Class> entry : map.entrySet()) {
            Object obj = rslts.getData(entry.getKey(), entry.getValue());
            if (obj != null) {
                info.add(InformationSet.split(entry.getKey()), obj);
            }
        }
        return info;
    }

    public static void fillDictionary(Map<String, Class> dic, String prefix, Map<String, Class> subdic) {
        if (Jdk6.isNullOrEmpty(prefix)) {
            dic.putAll(subdic);
        } else {
            for (Entry<String, Class> o : subdic.entrySet()) {
                dic.put(InformationSet.item(prefix, o.getKey()), o.getValue());
            }
        }
    }
}
