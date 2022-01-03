/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.information;

import demetra.DemetraVersion;
import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.sa.SaItem;
import demetra.sa.SaItems;
import demetra.sa.SaSpecification;
import demetra.util.NameManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaItemsMapping {

    public final String METADATA = "metadata", DOMAIN_SPECS = "domainspecs", TIMESTAMP = "TimeStamp", OWNER = "Owner";

    public NameManager<SaSpecification> defaultNameManager() {
        return new NameManager(SaSpecification.class, "spec", null);
    }

    public InformationSet write(SaItems mp, boolean verbose, DemetraVersion version) {
        InformationSet info = new InformationSet();
        Map<String, String> meta = mp.getMeta();
        if (!meta.isEmpty()) {
            InformationSet md = info.subSet(METADATA);
            mp.getMeta().forEach((k, v) -> md.set(k, v));
        }
        // build the maps of domain specs
        NameManager<SaSpecification> dic = defaultNameManager();
        int idx = 1;
        for (SaItem item : mp.getItems()) {
            InformationSet sinfo = SaItemMapping.write(item, dic, verbose, version);
            info.set("sa" + (idx++), sinfo);
        }
        // write the default specifications
        InformationSet defspec = info.subSet(DOMAIN_SPECS);
        for (String key : dic.getNames()) {
            defspec.set(key, SaSpecificationMapping.toInformationSet(dic.get(key), verbose, version));
        }
        return info;
    }

    public SaItems read(InformationSet info) {
        SaItems.Builder builder = SaItems.builder();
        // read the default specifications
        InformationSet defspec = info.getSubSet(DOMAIN_SPECS);
        if (defspec == null) {
            return null;
        }
        NameManager<SaSpecification> dic = defaultNameManager();
        List<Information<InformationSet>> dspecs = defspec.select(InformationSet.class);
        HashMap<String, String> equivalence = new HashMap<>();
        for (Information<InformationSet> dspec : dspecs) {
            SaSpecification cur = SaSpecificationMapping.of(dspec.getValue());
            if (cur != null) {
                // workaround to solve some old serialization problems
                String cname = dic.get(cur);
                if (cname != null) {
                    equivalence.put(dspec.getName(), cname);
                } else {
                    dic.set(dspec.getName(), cur);
                }
            }
        }
        InformationSet mset = info.getSubSet(METADATA);
        if (mset != null) {
            List<Information<String>> meta = mset.select(String.class);
            meta.forEach(v -> builder.meta(v.getName(), v.getValue()));
        }
        List<Information<InformationSet>> sas = info.select("sa*", InformationSet.class);
        for (Information<InformationSet> sa : sas) {
            SaItem cur = SaItemMapping.read(sa.getValue(), dic, equivalence);
            if (cur != null) {
                builder.item(cur).name(sa.getName());
            }
        }
        return builder.build();
    }

    public static final InformationSetSerializer<SaItems> SERIALIZER_V3 = new InformationSetSerializer<SaItems>() {
        @Override
        public InformationSet write(SaItems object, boolean verbose) {
            return SaItemsMapping.write(object, verbose, DemetraVersion.JD3);
        }

        @Override
        public SaItems read(InformationSet info) {
            return SaItemsMapping.read(info);
        }
        
        @Override 
        public boolean match(DemetraVersion version){
            return version == DemetraVersion.JD3;
        }
    };

    public static final InformationSetSerializer<SaItems> SERIALIZER_LEGACY = new InformationSetSerializer<SaItems>() {
        @Override
        public InformationSet write(SaItems object, boolean verbose) {
            return SaItemsMapping.write(object, verbose, DemetraVersion.JD2);
        }

        @Override
        public SaItems read(InformationSet info) {
            return SaItemsMapping.read(info);
        }

        @Override 
        public boolean match(DemetraVersion version){
            return version == DemetraVersion.JD2;
        }
    };
}
