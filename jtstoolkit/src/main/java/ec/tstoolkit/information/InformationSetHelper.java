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
package ec.tstoolkit.information;

import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.IProcResults;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Jean Palate
 */
public class InformationSetHelper {

    public static InformationSet fromMetaData(MetaData md) {
        if (md == null || md.isEmpty()) {
            return null;
        }
        InformationSet info = new InformationSet();
        for (Entry<String, String> entry : md.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                info.set(entry.getKey(), entry.getValue());
            }
        }
        return info;
    }

    public static void fillMetaData(InformationSet info, MetaData md) {
        List<Information<String>> sel = info.deepSelect(String.class);
        for (Information<String> sinfo : sel) {
            md.put(sinfo.name, sinfo.value);
        }
    }

    public static InformationSet fromProcResults(IProcResults rslts) {
        InformationSet info = new InformationSet();
        if (rslts != null) {
            Map<String, Class> dictionary = rslts.getDictionary();
            for (Entry<String, Class> entry : dictionary.entrySet()) {
                info.add(InformationSet.split(entry.getKey()), rslts.getData(entry.getKey(), entry.getValue()));
            }
        }
        return info;
    }

    public static <T> InformationSet fromProcResults(IProcResults rslts, Class<T> tclass) {
        InformationSet info = new InformationSet();
        if (rslts != null) {
            Map<String, Class> dictionary = rslts.getDictionary();
            for (Entry<String, Class> entry : dictionary.entrySet()) {
                if (tclass.isAssignableFrom(entry.getValue())) {
                    info.add(InformationSet.split(entry.getKey()), rslts.getData(entry.getKey(), entry.getValue()));
                }
            }
        }
        return info;
    }

    public static <T> InformationSet fromProcResults(IProcResults rslts, Set<String> sel) {
        InformationSet info = new InformationSet();
        if (rslts != null) {
            Map<String, Class> dictionary = rslts.getDictionary();
            for (Entry<String, Class> entry : dictionary.entrySet()) {
//            if (sel.contains(entry.getKey())) {
//                info.add(InformationSet.split(entry.getKey()), rslts.getData(entry.getKey(), entry.getValue()));
//            }
                for (String n : sel) {
                    if (InformationSet.wildCompare(n, entry.getKey())) {
                        info.set(InformationSet.split(entry.getKey()), rslts.getData(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        return info;
    }
}
