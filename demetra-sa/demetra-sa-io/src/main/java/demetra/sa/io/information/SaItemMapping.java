/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.information;

import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.processing.ProcQuality;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaDefinition;
import demetra.sa.SaItem;
import demetra.sa.SaSpecification;
import demetra.timeseries.Ts;
import demetra.util.NameManager;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaItemMapping {

    final String DOMAIN_SPEC = "domainspec", ESTIMATION_SPEC = "estimationspec", POINT_SPEC = "pointspec",
            TS = "ts", QUALITY = "quality", PRIORITY = "priority", POLICY = "policy", METADATA = "metadata", NAME = "name", COMMENT = "comment";
    final String DIAGNOSTICS = "diagnostics";
    final String DIAGNOSTICS_INTERNAL = "__diagnostics";

    public SaItem read(InformationSet info, NameManager<SaSpecification> defaults, Map<String, String> equivalence) {

        SaDefinition.Builder dbuilder = SaDefinition.builder();
        SaItem.Builder builder = SaItem.builder();
        Ts ts = info.get(TS, Ts.class);
        if (ts == null) {
            return null;
        }
        String dname = info.get(DOMAIN_SPEC, String.class);
        if (dname == null) {
            return null;
        }
        SaSpecification dspec = defaults.get(dname);
        if (dspec == null) {
            // search for an equivalence
            String ename = equivalence.get(dname);
            if (ename != null) {
                dspec = defaults.get(ename);
            }
            if (dspec == null) {
                return null;
            }
        }
        String e = info.get(POLICY, String.class);
        EstimationPolicyType policy = EstimationPolicyType.None;
        if (e != null) {
            policy = EstimationPolicyType.valueOf(e);
        }
        dbuilder = dbuilder.ts(ts).domainSpec(dspec).policy(policy);
        InformationSet einfo = info.getSubSet(ESTIMATION_SPEC);
        if (einfo != null) {
            SaSpecification espec = SaSpecificationMapping.of(einfo);
            if (espec != null) {
                dbuilder = dbuilder.estimationSpec(espec);
            }
        }
        InformationSet pinfo = info.getSubSet(POINT_SPEC);
        if (pinfo != null) {
            SaSpecification pspec = SaSpecificationMapping.of(pinfo);
            if (pspec != null) {
                dbuilder = dbuilder.pointSpec(pspec);
            }
        }

        builder.definition(dbuilder.build());
        Integer p = info.get(PRIORITY, Integer.class);
        if (p != null) {
            builder.priority(p);
        }
        String q = info.get(QUALITY, String.class);
        if (q != null) {
            ProcQuality quality = ProcQuality.valueOf(q);
            builder.quality(quality);
        } else {
            builder.quality(ProcQuality.Undefined);
        }
        InformationSet md = info.getSubSet(METADATA);
        if (md != null) {
            List<Information<String>> sel = md.select(String.class);
            sel.forEach(v -> builder.meta(v.getName(), v.getValue()));
        }
        String name = info.get(NAME, String.class);
        if (name != null) {
            builder.name(name);
        } else {
            builder.name(ts.getName());
        }
        return builder.build();
    }

    private final String NONAME = "";

    public InformationSet write(SaItem item, NameManager<SaSpecification> defaults, boolean verbose) {
        InformationSet info = new InformationSet();
        if (!item.getName().isEmpty()) {
            info.set(NAME, item.getName());
        }
        SaDefinition def = item.getDefinition();

        Ts ts = def.getTs();
        info.set(TS, ts.freeze());
        String dname = defaults.get(def.getDomainSpec());
        if (dname == null) {
            dname = defaults.nextName();
            defaults.set(dname, def.getDomainSpec());
        }
        info.set(DOMAIN_SPEC, dname);

        if (def.getEstimationSpec() != null) {
            info.set(ESTIMATION_SPEC, SaSpecificationMapping.toInformationSet(def.getEstimationSpec(), verbose));
        }
        if (def.getPointSpec() != null) {
            info.set(POINT_SPEC, SaSpecificationMapping.toInformationSet(def.getPointSpec(), verbose));
        }
        if (item.getPriority() > 0 || verbose) {
            info.set(PRIORITY, item.getPriority());
        }
        if (item.getQuality() != ProcQuality.Undefined || verbose) {
            info.set(QUALITY, item.getQuality().name());
        }
        if (def.getPolicy() != EstimationPolicyType.None) {
            info.set(POLICY, def.getPolicy().name());
        }
        Map<String, String> meta = item.getMeta();
        if (!meta.isEmpty()) {
            InformationSet md = info.subSet(METADATA);
            meta.forEach((k, v) -> md.set(k, v));
        }
        return info;
    }
}
