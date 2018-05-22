/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved
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

import demetra.datatypes.Ts;
import ec.satoolkit.ISaSpecification;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.utilities.NameManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class SaItemType {

    public static final String DOMAIN_SPEC = "domainspec", ESTIMATION_SPEC = "estimationspec", POINT_SPEC = "pointspec",
            TS = "ts", QUALITY = "quality", PRIORITY = "priority", POLICY = "policy", METADATA = "metadata", NAME = "name", COMMENT = "comment";
    public static final String DIAGNOSTICS = "diagnostics";

    private static final String[] EMPTY = new String[0];
    private static final String NONAME = "";

    public static enum Status {

        Unprocessed,
        NoSpec,
        NoData,
        Pending,
        Valid,
        Invalid;

        public boolean isError() {
            return isProcessed() && this != Valid;
        }

        public boolean isProcessed() {
            return this != Unprocessed && this != Pending;
        }
    }

    @lombok.NonNull
    private Ts ts;

    private ISaSpecification pointSpec;
    private ISaSpecification estimationSpec;

    @lombok.NonNull
    private ISaSpecification domainSpec;

    @lombok.NonNull
    @lombok.Builder.Default
    private EstimationPolicyType estimationPolicy = EstimationPolicyType.None;

    @lombok.NonNull
    @lombok.Builder.Default
    private Status status = Status.Unprocessed;

    @lombok.Builder.Default
    private int priority = -1;

    @lombok.NonNull
    @lombok.Builder.Default
    private ProcQuality quality = ProcQuality.Undefined;

    @lombok.NonNull
    @lombok.Builder.Default
    private String[] warnings = EMPTY;

    @lombok.NonNull
    @lombok.Builder.Default
    private String name = NONAME;

    @lombok.Singular("meta")
    private Map<String, String> metaData;

    static SaItemType read(InformationSet info, NameManager<ISaSpecification> defaults, HashMap<String, String> equivalence) {
        Builder builder = SaItemType.builder();
        Ts s = info.get(TS, Ts.class);
        if (s == null) {
            return null;
        }
        builder.ts(s);
        String dname = info.get(DOMAIN_SPEC, String.class);
        if (dname == null) {
            return null;
        }
        ISaSpecification dspec = defaults.get(dname);
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
        builder.domainSpec(dspec);
        InformationSet pspec = info.getSubSet(POINT_SPEC);
        if (pspec != null) {
            builder.pointSpec( SaManager.createSpecification(pspec));
        }
        InformationSet espec = info.getSubSet(ESTIMATION_SPEC);
        if (espec != null) {
            builder.estimationSpec(SaManager.createSpecification(espec));
        }
        Integer p = info.get(PRIORITY, Integer.class);
        if (p != null) {
            builder.priority(p);
        }
        String q = info.get(QUALITY, String.class);
        if (q != null) {
            builder.quality(ProcQuality.valueOf(q));
        }
        String e = info.get(POLICY, String.class);
        if (e != null) {
            builder.estimationPolicy(EstimationPolicyType.valueOf(e));
        }
        InformationSet md = info.getSubSet(METADATA);
        if (md != null) {
            List<Information<String>> sel = info.deepSelect(String.class);
            for (Information<String> sinfo : sel) {
                builder.meta(sinfo.name, sinfo.value);
            }
            builder.name(info.get(NAME, String.class));
        }
        return builder.build();
    }

    boolean write(InformationSet info, NameManager<ISaSpecification> defaults, boolean verbose) {
        if (!name.isEmpty()) {
            info.set(NAME, name);
        }
        info.set(TS, ts);
        String dname = defaults.get(domainSpec);
        if (dname == null) {
            dname = defaults.nextName();
            defaults.set(dname, domainSpec);
        }
        info.set(DOMAIN_SPEC, dname);

        if (pointSpec != null) {
            info.set(POINT_SPEC, pointSpec.write(verbose));
        }
        if (estimationSpec != null) {
            info.set(ESTIMATION_SPEC, estimationSpec.write(verbose));
        }
        if (priority >= 0 || verbose) {
            info.set(PRIORITY, priority);
        }
        if (quality != ProcQuality.Undefined || verbose) {
            info.set(QUALITY, quality.name());
        }
        if (estimationPolicy != EstimationPolicyType.None) {
            info.set(POLICY, estimationPolicy.name());
        }
        if (!metaData.isEmpty()) {
            InformationSet minfo = info.subSet(METADATA);
            for (Map.Entry<String, String> entry : metaData.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    info.set(entry.getKey(), entry.getValue());
                }
            }
        }
        return true;
    }
}
