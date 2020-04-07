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
package ec.tss.sa;

import com.google.common.collect.ForwardingList;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.TsInformationType;
import ec.tss.TsStatus;
import ec.tstoolkit.IDocumented;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.utilities.NameManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jean Palate
 */
public final class SaProcessing extends ForwardingList<SaItem> implements IDocumented, InformationSetSerializable {

    public static final String METADATA = "metadata", DOMAIN_SPECS = "domainspecs", TIMESTAMP = "TimeStamp", OWNER = "Owner";
    private static final AtomicInteger g_id = new AtomicInteger(0);
    private final int m_id;
    private final AtomicInteger m_key = new AtomicInteger(0);
    private final List<SaItem> items_;
    private final MetaData metadata_;
    private boolean dirty_;

    /// <summary>
    /// Create a new processing with an old one...
    /// </summary>
    /// <param name="oldprocessing"></param>
    /// <param name="policy"></param>
    /// <returns></returns>
//    public static SaProcessing createDiagnostics(SaProcessing oldprocessing, EstimationPolicyType policy) {
//        SaProcessing processing = new SaProcessing();
//        for (SaItem item : oldprocessing.items_) {
//            ISaSpecification nspec = SaManager.instance.createSpecification(item, null, policy);
//            processing.items_.add(item.newSpecification(nspec, policy));
//            processing.dirty_=true;
//        }
//        return processing;
//    }
    public static void updateSpecification(SaProcessing curprocessing) {
        for (SaItem item : curprocessing.items_) {
            SaManager.instance.updatePointSpecification(item);
        }
        curprocessing.dirty_ = true;
    }

//    public static void updateSummary(SaProcessing curprocessing) {
//        for (SaItem item : curprocessing.items_) {
//            SaManager.instance.updateSummary(item);
//        }
//        curprocessing.dirty_ = true;
//    }
    public SaProcessing() {
        this.m_id = g_id.incrementAndGet();
        this.items_ = new ArrayList<>();
        this.dirty_ = false;
        this.metadata_ = new MetaData();
    }

    // ForwardingList >
    @Override
    protected List<SaItem> delegate() {
        return items_;
    }

    @Override
    public boolean add(SaItem item) {
        item.setKey(nextKey());
        return dirty_ = items_.add(item);
    }

    @Override
    public boolean addAll(Collection<? extends SaItem> c) {
        for (SaItem item : c) {
            item.setKey(nextKey());
        }
        return dirty_ = items_.addAll(c);
    }

    @Override
    public void clear() {
        if (!items_.isEmpty()) {
            dirty_ = true;
        }
        items_.clear();
    }

    @Override
    public SaItem remove(int idx) {
        SaItem result = items_.remove(idx);
        dirty_ = result != null;
        return result;
    }

    @Override
    public boolean remove(Object item) {
        return dirty_ = items_.remove(item);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return dirty_ = items_.removeAll(collection);
    }

    @Override
    public SaItem set(int idx, SaItem value) {
        SaItem old = items_.get(idx);
        if (old == value) {
            return old;
        }
        if (!old.isLocked()) {
            dirty_ = true;
            value.setKey(old.getKey());
            return items_.set(idx, value);
        }
        return null;
    }

    @Override
    public SaItem[] toArray() {
        return items_.stream().toArray(SaItem[]::new);
    }

    int nextKey() {
        return m_key.getAndIncrement();
    }

    public String getDocumentId() {
        StringBuilder builder = new StringBuilder();
        builder.append("SaProcessing-").append(m_id);
        return builder.toString();
    }

    public List<SaItem> refresh(List<SaItem> items, EstimationPolicyType policy, boolean nospan) {
        if (policy == EstimationPolicyType.LastOutliers) {
            TsPeriodSelector sel = new TsPeriodSelector();
            sel.excluding(0, -1);
            return refresh(items, sel, nospan);
        }
        List<SaItem> nitems = new ArrayList<>();
        for (SaItem item : items) {
            if (!item.isLocked()) {
                int pos = indexOf(item);
                if (pos < 0) {
                    nitems.add(null);
                    continue;
                }
                Ts s = item.getTs().unfreeze();
                // createDiagnostics the new spec
                ISaSpecification nspec = SaManager.instance.createSpecification(item, null, policy, nospan);
                SaItem citem = item.newSpecification(s, nspec, policy);
                citem.setKey(item.getKey());
                nitems.add(citem);
                items_.set(pos, citem);
                dirty_ = true;
            }
        }
        return nitems;
    }

    public List<SaItem> refresh(List<SaItem> items, TsPeriodSelector outliersCutoff, boolean nospan) {
        List<SaItem> nitems = new ArrayList<>();
        for (SaItem item : items) {
            if (!item.isLocked()) {
                int pos = indexOf(item);
                if (pos < 0) {
                    nitems.add(null);
                    continue;
                }
                TsDomain prevDomain = item.getTsData().getDomain();
                if (outliersCutoff != null) {
                    prevDomain = prevDomain.select(outliersCutoff);
                } else {
                    prevDomain = prevDomain.drop(0, prevDomain.getFrequency().intValue());
                }
                Ts s = item.getTs().unfreeze();
                // createDiagnostics the new spec
                ISaSpecification nspec = SaManager.instance.createSpecification(item, prevDomain, EstimationPolicyType.LastOutliers, nospan);
                SaItem citem = item.newSpecification(s, nspec, EstimationPolicyType.LastOutliers);
                nitems.add(citem);
                citem.setKey(item.getKey());
                items_.set(pos, citem);
                dirty_ = true;
            }
        }
        return nitems;
    }

    public void refresh(EstimationPolicyType policy, boolean nospan) {
        if (policy == EstimationPolicyType.LastOutliers) {
            TsPeriodSelector sel = new TsPeriodSelector();
            sel.excluding(0, -1);
            refresh(sel, nospan);
            return;
        }
        int n = items_.size();
        for (int i = 0; i < n; ++i) {
            SaItem item = items_.get(i);
            if (!item.isLocked()) {
                TsDomain newDomain = null;
                Ts s = item.getTs().unfreeze();
                if (policy == EstimationPolicyType.Current) {
                    if (s.hasData() == TsStatus.Undefined) {
                        s.load(TsInformationType.Data);
                    }
                    if (s.hasData() == TsStatus.Valid) {
                        newDomain = s.getTsData().getDomain();
                    }
                }
                // createDiagnostics the new spec
                ISaSpecification nspec = SaManager.instance.createSpecification(item, newDomain, policy, nospan);
                SaItem citem = item.newSpecification(s, nspec, policy);
                citem.setKey(item.getKey());
                items_.set(i, citem);
                dirty_ = true;
            }
        }
    }

    // 17/9/2010. Jean Palate. New refreshing policy:
    // The period selector defines the period that is frozen in the new outlier detection procedure.
    // Outliers that belong to that period will be maintained.
    public void refresh(TsPeriodSelector outliersCutoff, boolean nospan) {
        int n = items_.size();
        for (int i = 0; i < n; ++i) {
            SaItem item = items_.get(i);
            if (!item.isLocked() && item.getTsData() != null) {
                TsDomain prevDomain = item.getTsData().getDomain();
                if (outliersCutoff != null) {
                    prevDomain = prevDomain.select(outliersCutoff);
                } else {
                    prevDomain = prevDomain.drop(0, prevDomain.getFrequency().intValue());
                }
                Ts s = item.getTs().unfreeze();
                // createDiagnostics the new spec
                ISaSpecification nspec = SaManager.instance.createSpecification(item, prevDomain, EstimationPolicyType.LastOutliers, nospan);
                SaItem citem = item.newSpecification(s, nspec, EstimationPolicyType.LastOutliers);
                citem.setKey(item.getKey());
                items_.set(i, citem);
                dirty_ = true;
            }
        }
    }

    @Deprecated
    public int search(SaItem item) {
        return indexOf(item);
    }

    @Deprecated
    public void addRange(Collection<SaItem> items) {
        addAll(items);
    }

    @Deprecated
    public int getCount() {
        return size();
    }

    @Deprecated
    public SaItem[] currentItems() {
        return toArray();
    }

    @Deprecated
    public List<SaItem> items() {
        return this;
    }

    public void addRange(ISaSpecification defspec, Iterable<Ts> ts) {
        for (Ts s : ts) {
            SaItem item = new SaItem(defspec, s);
            item.setKey(nextKey());
            items_.add(item);
        }
        dirty_ = true;
    }

    public void removeRange(int start, int count) {
        int imax = Math.min(start + count, items_.size()) - 1;
        for (int i = imax; i >= start; --i) {
            items_.remove(i);
        }
        dirty_ = true;
    }

    public boolean replace(SaItem olditem, SaItem newitem) {
        if (olditem.isLocked()) {
            return false;
        }
        for (int i = 0; i < items_.size(); ++i) {
            if (items_.get(i) == olditem) {
                newitem.setKey(olditem.getKey());
                newitem.setPriority(olditem.getPriority());
                items_.set(i, newitem);
                dirty_ = true;
                return true;
            }
        }
        return false;
    }

    public int getProcessedCount() {
        int n = 0;
        for (SaItem item : items_) {
            if (item.getStatus() != SaItem.Status.Unprocessed) {
                ++n;
            }
        }
        return n;
    }

    public boolean isInitialized() {
        for (SaItem item : items_) {
            if (item.getEstimationSpecification() == null || item.getTs() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isProcessed() {
        for (SaItem item : items_) {
            if (!item.isProcessed()) {
                return false;
            }
        }
        return true;
    }

    public boolean isNew() {
        String ts = metadata_.get(SaProcessing.TIMESTAMP);
        return ts == null;
    }

    public boolean isDirty() {
        if (dirty_) {
            return true;
        }
        for (SaItem item : items_) {
            if (item.isDirty()) {
                return true;
            }
        }
        return false;

    }

    public void resetDirty() {
        dirty_ = false;
        for (SaItem item : items_) {
            item.resetDirty();
        }
    }

    @Override
    public MetaData getMetaData() {
        return metadata_;
    }

    public SaProcessing makeCopy() {
        SaProcessing p = new SaProcessing();
        for (SaItem item : items_) {
            SaItem cpy = item.makeCopy();
            cpy.setKey(p.nextKey());
            p.items_.add(cpy);
        }
        p.dirty_ = !p.items_.isEmpty();
        return p;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        InformationSet mset = InformationSetHelper.fromMetaData(metadata_);
        if (mset != null) {
            info.set(METADATA, mset);
        }
        // build the maps of domain specs
        NameManager<ISaSpecification> dic = new NameManager(ISaSpecification.class, "spec", null);
        int idx = 1;
        for (SaItem item : items_) {
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
            InformationSetHelper.fillMetaData(mset, metadata_);
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
            ISaSpecification cur = SaManager.instance.createSpecification(dspec.value);
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
            SaItem cur = new SaItem();
            if (sa.value != null && cur.read(sa.value, dic, equivalence)) {
                cur.setKey(nextKey());
                items_.add(cur);
            } else {
                String msg = sa.name;
            }
        }
        return true;
    }

    public Map<Integer, List<AlgorithmDescriptor>> methods() {
        Map<Integer, List<AlgorithmDescriptor>> rslt = new HashMap<>();
        for (SaItem item : items_) {
            if (item.getTs().hasData() == TsStatus.Valid) {
                int freq = item.getTsData().getFrequency().intValue();
                List<AlgorithmDescriptor> desc = rslt.get(freq);
                if (desc == null) {
                    desc = new ArrayList<>();
                    rslt.put(freq, desc);
                }
                AlgorithmDescriptor alg = item.getEstimationMethod();
                if (!desc.contains(alg)) {
                    desc.add(alg);
                }
            }
        }
        return rslt;
    }

    public Map<Integer, Map<AlgorithmDescriptor, RegArimaReport>> createRegArimaReports() {
        Map<Integer, Map<AlgorithmDescriptor, RegArimaReport>> reports = new HashMap<>();
        for (SaItem item : items_) {
            if (item.isProcessed()) {
                CompositeResults rslt = item.process();
                if (rslt != null) {
                    int freq = item.getTs().getTsData().getFrequency().intValue();
                    Map<AlgorithmDescriptor, RegArimaReport> cur = reports.get(freq);
                    if (cur == null) {
                        cur = new LinkedHashMap<>();
                        reports.put(freq, cur);
                    }
                    RegArimaReport report = cur.get(item.getEstimationMethod());
                    if (report == null) {
                        report = new RegArimaReport(freq);
                        cur.put(item.getEstimationMethod(), report);
                    }
                    report.add(rslt);
                }
            }
        }
        return reports;
    }

}
