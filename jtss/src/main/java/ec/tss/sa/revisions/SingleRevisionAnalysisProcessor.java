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
package ec.tss.sa.revisions;

import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaManager;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class SingleRevisionAnalysisProcessor {

    public static final String REF0 = "ref0", REF1 = "ref1", VER = "vintage";

    private final RevisionAnalysisSpec spec_;
    private final TsData s_;
    private final TsDomain refDomain_;
    private InformationSet rslts_;
    private CompositeResults ref0_, ref1_;
    private final List<CompositeResults> details_ = new ArrayList<>();

    public SingleRevisionAnalysisProcessor(RevisionAnalysisSpec spec, TsData s) {
        spec_ = spec;
        s_ = s;
        if (s_ == null) {
            refDomain_ = null;
        } else {
            refDomain_ = s_.getDomain();
        }
    }

    public InformationSet getReference() {
        return rslts_;
    }

    public boolean process() {
        rslts_ = null;
        details_.clear();
        SaItem cur = computeReferences();
        if (cur == null) {
            return false;
        }
        TsDomain dom = searchStartingDomain();
        TsPeriod end;
        int i;
        if (spec_.isOutOfSample()) {
            if (spec_.isTargetFinal()) {
                details_.add(cur.process());
            }
            i = 1;
            dom = dom.extend(0, 1);
            end = refDomain_.getEnd();
        } else {
            i = 0;
            end = refDomain_.getLast();
        }
        int freq = dom.getFrequency().intValue();
        while (dom.getEnd().isNotAfter(end)) {
            Ts tmp = TsFactory.instance.createTs("tmp" + i, null, s_.fittoDomain(dom));

            EstimationPolicyType policy;
            if (0 == i % (freq * spec_.getRevisionDelay())) {
                policy = spec_.getMainEstimation().getType();
            } else {
                policy = spec_.getIntermediateEstimation().getType();
            }
            ISaSpecification espec = SaManager.instance.createSpecification(cur, dom.drop(0, freq), policy, true);
            cur = cur.newSpecification(tmp, espec, policy);
            details_.add(cur.process());
            ++i;
            dom = dom.extend(0, 1);
        }
        if (!spec_.isOutOfSample() && !spec_.isTargetFinal()) {
            details_.add(ref1_);
        }
        return true;
    }

    private SaItem computeReferences() {
        SaItem ref1 = inSampleReference(), ref0 = outOfSampleReference();

        if (spec_.isOutOfSample()) {
            return ref0;
        } else {
            return ref1;
        }

    }

    private SaItem inSampleReference() {
        // compute the reference
        Ts tmp = TsFactory.instance.createTs("ref", null, s_);
        SaItem cur = new SaItem(spec_.getSaSpecification(), tmp);
        ref1_ = cur.process();
        return ref1_ != null ? cur : null;
    }

    private SaItem outOfSampleReference() {
        // compute the reference
        TsDomain fdom = searchStartingDomain();
        Ts tmp = TsFactory.instance.createTs("ref", null, s_.fittoDomain(fdom));
        SaItem cur = new SaItem(spec_.getSaSpecification(), tmp);
        ref0_ = cur.process();
        return ref0_ != null ? cur : null;
    }

    private TsDomain searchStartingDomain() {
        TsDomain start = refDomain_.drop(0, spec_.getAnalysisLength() * refDomain_.getFrequency().intValue() - 1);
        Day day = new Day(start.getLast().getYear(), spec_.getRevisionDay().month, spec_.getRevisionDay().day);
        int pos = start.search(day);
        int del = start.getLength() - pos;
        if (pos >= 0 && del != 1) {
            start = start.drop(0, del - 1);
        }
        return start;
    }

    public <T> List<T> items(String name, Class<T> tclass) {
        List<T> rslt = new ArrayList<>();
        for (CompositeResults r : details_) {
            rslt.add(r.getData(name, tclass));
        }
        return rslt;
    }

    public InformationSet search(Map<String, Class> dic) {
        InformationSet rslt = new InformationSet();
        for (String name : dic.keySet()) {
            Class cl = dic.get(name);
            String[] deepname = InformationSet.split(name);
            Object ref0 = ref0_.getData(name, cl);
            if (ref0 != null) {
                rslt.subSet(REF0).add(deepname, ref0);
            }
            int i = 0;
            for (CompositeResults x : details_) {
                Object v = x.getData(name, cl);
                if (v != null) {
                    rslt.subSet(VER + (i)).add(deepname, v);
                }
                ++i;
            }
            Object ref1 = ref1_.getData(name, cl);
            if (ref1 != null) {
                rslt.subSet(REF1).add(deepname, ref1);
            }
        }
        return rslt;
    }

}
