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
package demetra.datatypes.sa;

import ec.satoolkit.ISaSpecification;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.utilities.NameManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@lombok.Data
public class SaProcessingType implements InformationSetSerializable {

    public static final String METADATA = "metadata", DOMAIN_SPECS = "domainspecs", TIMESTAMP = "TimeStamp", OWNER = "Owner";

    private final Map<String, String> metaData = new HashMap<>();

    private final List<SaItemType> items = new ArrayList<>();

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (!metaData.isEmpty()) {
            InformationSet mset = new InformationSet();
            for (Map.Entry<String, String> entry : metaData.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    mset.set(entry.getKey(), entry.getValue());
                }
            }
            info.set(METADATA, mset);
        }
        // build the maps of domain specs
        NameManager<ISaSpecification> dic = new NameManager(ISaSpecification.class, "spec", null);
        int idx = 1;
        for (SaItemType item : items) {
            item.write(info.subSet("sa" + (idx++)), dic, verbose);
        }
        // write the default specifications
        InformationSet defspec = info.subSet(DOMAIN_SPECS);
        for (String key : dic.getNames()) {
            defspec.set(key, dic.get(key).write(verbose));
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        InformationSet mset = info.getSubSet(METADATA);
        if (mset != null) {
            List<Information<String>> sel = mset.deepSelect(String.class);
            for (Information<String> sinfo : sel) {
                metaData.put(sinfo.name, sinfo.value);
            }
        }
        // read the default specifications
        InformationSet defspec = info.getSubSet(DOMAIN_SPECS);
        if (defspec == null) {
            return false;
        }
        NameManager<ISaSpecification> dic = new NameManager(ISaSpecification.class, "spec", null);
        List<Information<InformationSet>> dspecs = defspec.select(InformationSet.class);
        HashMap<String, String> equivalence = new HashMap<>();
        for (Information<InformationSet> dspec : dspecs) {
            ISaSpecification cur = SaManager.createSpecification(dspec.value);
            if (cur != null) {
                // workaround to solve some old serialization problems
                String cname = dic.get(cur);
                if (cname != null) {
                    equivalence.put(dspec.name, cname);
                } else {
                    dic.set(dspec.name, cur);
                }
            }
        }
        List<Information<InformationSet>> sas = info.select("sa*", InformationSet.class);
        for (Information<InformationSet> sa : sas) {
            SaItemType cur=SaItemType.read(sa.value, dic, equivalence);
            if (cur != null){
              items.add(cur);
            }
        }
        return true;
    }

}
