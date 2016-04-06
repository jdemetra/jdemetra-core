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

import ec.satoolkit.GenericSaProcessingFactory;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.TsStatus;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.information.ProxyResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Arrays2;
import ec.tstoolkit.utilities.NameManager;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jean Palate
 */
public class SaItem {

    public static final String DOMAIN_SPEC = "domainspec", ESTIMATION_SPEC = "estimationspec", POINT_SPEC = "pointspec",
            TS = "ts", QUALITY = "quality", PRIORITY = "priority", POLICY = "policy", METADATA = "metadata";
    public static final String DIAGNOSTICS = "diagnostics";
    private static final String DIAGNOSTICS_INTERNAL = "__diagnostics";

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
    private final AtomicInteger id_ = new AtomicInteger(-1);
    private boolean dirty_ = true;
    private Ts ts_;
    private ISaSpecification pspec_, espec_, dspec_;
    private boolean cacheResults_ = true;
    private volatile CompositeResults rslts_;
    private EstimationPolicyType estimation_ = EstimationPolicyType.None;
    private Status status_ = Status.Unprocessed;
    private int priority_ = -1;
    private ProcQuality quality_ = ProcQuality.Undefined;
    private String[] warnings_;
    private InformationSet qsummary_;
    private MetaData metaData_;
    private boolean locked_;

    public SaItem makeCopy() {
        synchronized (id_) {
            SaItem n = new SaItem();
            n.dspec_ = dspec_;
            n.espec_ = espec_;
            n.pspec_ = pspec_;
            n.estimation_ = estimation_;
            n.locked_ = locked_;
            n.qsummary_ = qsummary_;
            n.priority_ = priority_;
            n.quality_ = quality_;
            n.rslts_ = rslts_;
            n.status_ = status_;
            n.ts_ = ts_;
            n.warnings_ = warnings_;
            n.cacheResults_ = cacheResults_;
            n.metaData_ = metaData_ == null ? null : metaData_.clone();
            return n;
        }
    }

    SaItem() {
    }

    void setKey(int key) {
        id_.set(key);
    }

    public SaItem(ISaSpecification dspec, EstimationPolicyType policy, ISaSpecification espec, Ts s) {
        dspec_ = dspec;
        estimation_ = policy;
        espec_ = espec;
        ts_ = s;
    }

    public SaItem(ISaSpecification dspec, Ts s) {
        dspec_ = dspec;
        estimation_ = EstimationPolicyType.Complete;
        ts_ = s;
    }

    public SaItem newSpecification(Ts s, ISaSpecification espec, EstimationPolicyType policy) {
        SaItem nitem = new SaItem();
        nitem.dspec_ = dspec_;
        nitem.ts_ = s;
        if (espec != null) {
            nitem.espec_ = espec;
            nitem.estimation_ = policy;
        } else {
            nitem.estimation_ = EstimationPolicyType.Complete;
        }
        nitem.priority_ = priority_;
        return nitem;
    }

    public SaItem newSpecification(ISaSpecification espec, EstimationPolicyType policy) {
        SaItem nitem = new SaItem();
        nitem.dspec_ = dspec_;
        nitem.ts_ = ts_;
        if (espec != null) {
            nitem.espec_ = espec;
            nitem.estimation_ = policy;
        } else {
            nitem.estimation_ = EstimationPolicyType.Complete;
        }
        nitem.priority_ = priority_;
        return nitem;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEstimationSpecification());
        if (ts_ != null) {
            String item = ts_.getName();
            if (item != null) {
                builder.append(" - ").append(item);
            }
        }
        return builder.toString();
    }

    public MetaData getMetaData() {
        return metaData_;
    }

    public void setMetaData(MetaData md) {
        metaData_ = md;
        dirty_ = true;
    }

    public int getPriority() {
        return priority_;
    }

    public void setPriority(int value) {
        if (value != priority_) {
            if (value < 0) {
                priority_ = 0;
            } else if (value > 10) {
                priority_ = 10;
            } else {
                priority_ = value;
            }
            dirty_ = true;
        }
    }

    public ProcQuality getQuality() {
        synchronized (id_) {
            return quality_;
        }
    }

    public void setQuality(ProcQuality value) {
        if (quality_ != value) {
            boolean recalc = quality_ == ProcQuality.Accepted && value == ProcQuality.Undefined;
            quality_ = value;
            if (recalc) {
                quality_ = ProcDiagnostic.summary(getDiagnostics());
            }
            dirty_ = true;
        }
    }

    public InformationSet getDiagnostics() {
        synchronized (id_) {
            if (qsummary_ == null) {
                process();
            }
            return qsummary_;
        }
    }

    public String[] getWarnings() {
        synchronized (id_) {
            if (qsummary_ == null) {
                process();
            }
            return warnings_ == null ? Arrays2.EMPTY_STRING_ARRAY : warnings_;
        }
    }

    public Ts getTs() {

        return ts_;
    }

    public TsMoniker getMoniker() {
        return ts_.getMoniker();
    }

    public Status getStatus() {
        synchronized (id_) {
            return status_;
        }
    }

    public void setStatus(Status value) {
        synchronized (id_) {
            status_ = value;
        }
    }

    public AlgorithmDescriptor getEstimationMethod() {
        ISaSpecification spec = getEstimationSpecification();
        if (spec == null) {
            return null;
        } else {
            return SaManager.instance.find(spec).getInformation();
        }
    }

//        public void SetContext(TSContext context)
//        {
//            lock (m_id)
//            {
//
//                if (dspec_ != null)
//                    dspec_.Context = context;
//                if (espec_ != null)
//                    espec_.Context = context;
//                if (pspec_ != null)
//                    pspec_.Context = context;
//            }
//        }
    public ISaSpecification getEstimationSpecification() {
        return espec_ != null ? espec_ : dspec_;
    }

    public ISaSpecification getActiveSpecification() {
        synchronized (id_) {
            if (pspec_ == null) {
                return getEstimationSpecification();
//        }
//        if (espec_ != null) {
//            if (pspec_.getInformation().equals(espec_.getInformation())) {
//                return pspec_;
//            } else {
//                return espec_;
//            }
            } else {
                return pspec_;
            }
        }
    }

    public ISaSpecification getPointSpecification() {
        synchronized (id_) {
            return pspec_;
        }
    }

    public void setPointSpecification(ISaSpecification pspec) {
        pspec_ = pspec;
    }

    public EstimationPolicyType getEstimationPolicy() {
        return estimation_;
    }

    /// <summary>
    /// Reference Specification
    /// </summary>
    public ISaSpecification getDomainSpecification() {
        return dspec_;
    }

    /// <summary>
    /// Reference time series
    /// </summary>
    public TsData getTsData() {
//        if (ts_.hasData() == TsStatus.Undefined) {
//            ts_.load(TsInformationType.Data);
//        }
        return ts_.getTsData();
    }

    public void compress() {
        rslts_ = null;
    }

    private void update() {
        if (rslts_ != null && rslts_.get(GenericSaProcessingFactory.DECOMPOSITION) != null) {
            status_ = Status.Valid;
            if (pspec_ == null) {
                SaManager.instance.updatePointSpecification(this);
            }
            qsummary_ = SaManager.createDiagnostics(rslts_);
            if (quality_ != ProcQuality.Accepted) {
                quality_ = ProcDiagnostic.summary(qsummary_);
            }
            warnings_ = qsummary_.warnings().stream().toArray(String[]::new);
            rslts_.put(DIAGNOSTICS_INTERNAL, new ProxyResults(qsummary_, null), DIAGNOSTICS);
        } else {
            status_ = Status.Invalid;
        }
    }

    public CompositeResults process() {
        synchronized (id_) {
            if (rslts_ != null || status_.isError()) {
                return rslts_;
            }
            if (status_ == Status.Pending) {
                return null;
            }
            if (quality_ == ProcQuality.Undefined) { // never processed
                dirty_ = true;
            }
            if (espec_ == null && dspec_ == null) {
                status_ = Status.NoSpec;
                return null;
            }
            status_ = Status.Pending;
        }
        if (ts_.hasData() == TsStatus.Undefined) {
            ts_.load(TsInformationType.Data);
        }
        if (ts_.getTsData() == null) {
            status_ = Status.NoData;
            return null;
        }

        CompositeResults rslts = SaManager.instance.process(getActiveSpecification(), ts_.getTsData());
        synchronized (id_) {
            rslts_ = rslts;
            update();
            if (!cacheResults_) {
                rslts_ = null;
            }
            return rslts;
        }
    }
//

    public boolean isProcessed() {
        return status_.isProcessed();
    }

    public boolean isDirty() {
        return dirty_;
    }

    public boolean isLocked() {
        return locked_;
    }

    public void setLocked(boolean value) {
        if (value != locked_) {
            locked_ = value;
            dirty_ = true;
        }
    }

    public void resetDirty() {
        dirty_ = false;
    }

    public int getKey() {
        return id_.get();
    }

    public boolean fillDocument(SaDocument<?> doc) {
        if (!MetaData.isNullOrEmpty(metaData_)) {
            doc.getMetaData().copy(metaData_);
        }
        return doc.unsafeFill(getTs(), getEstimationSpecification(), process());
    }

    public SaDocument<ISaSpecification> toDocument() {
        ISaSpecification xspec = getEstimationSpecification();
        ISaProcessingFactory processor = (ISaProcessingFactory) SaManager.instance.find(xspec);
        SaDocument<ISaSpecification> doc = processor.createDocument();
        if (doc.unsafeFill(getTs(), xspec, process())) {
            if (!MetaData.isNullOrEmpty(metaData_)) {
                doc.getMetaData().copy(metaData_);
            }
            return doc;
        } else {
            return null;
        }
    }

    public void unsafeFill(CompositeResults rslts) {
        synchronized (id_) {
            rslts_ = rslts;
            update();
            if (!cacheResults_) {
                rslts_ = null;
            }
        }
    }

    boolean read(InformationSet info, NameManager<ISaSpecification> defaults, HashMap<String, String> equivalence) {
        TsInformation tsinfo = info.get(TS, TsInformation.class);
        if (tsinfo == null) {
            return false;
        }
        if (tsinfo.data != null) {
            ts_ = TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker, tsinfo.metaData, tsinfo.data);
        } else {
            ts_ = TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker, TsInformationType.None);
        }
        String dname = info.get(DOMAIN_SPEC, String.class);
        if (dname == null) {
            return false;
        }
        dspec_ = defaults.get(dname);
        if (dspec_ == null) {
            // search for an equivalence
            String ename = equivalence.get(dname);
            if (ename != null) {
                dspec_ = defaults.get(ename);
            }
            if (dspec_ == null) {
                return false;
            }
        }
        InformationSet pspec = info.getSubSet(POINT_SPEC);
        if (pspec != null) {
            pspec_ = SaManager.instance.createSpecification(pspec);
        }
        InformationSet espec = info.getSubSet(ESTIMATION_SPEC);
        if (espec != null) {
            espec_ = SaManager.instance.createSpecification(espec);
        }
        Integer p = info.get(PRIORITY, Integer.class);
        if (p != null) {
            priority_ = p;
        }
        String q = info.get(QUALITY, String.class);
        if (q != null) {
            quality_ = ProcQuality.valueOf(q);
        }
        String e = info.get(POLICY, String.class);
        if (e != null) {
            estimation_ = EstimationPolicyType.valueOf(e);
        }
        InformationSet md = info.getSubSet(METADATA);
        if (md != null) {
            metaData_ = new MetaData();
            InformationSetHelper.fillMetaData(md, metaData_);
        }
        return true;
    }

    boolean write(InformationSet info, NameManager<ISaSpecification> defaults, boolean verbose) {
        TsInformation tsinfo;
        if (ts_.getMoniker().isAnonymous()) {
            tsinfo = new TsInformation(ts_, TsInformationType.All);
        } else if (status_ == SaItem.Status.Unprocessed) {
            tsinfo = new TsInformation(ts_, TsInformationType.Definition);
        } else {
            tsinfo = new TsInformation(ts_.freeze(), TsInformationType.All);
        }
        info.set(TS, tsinfo);
        String dname = defaults.get(dspec_);
        if (dname == null) {
            dname = defaults.nextName();
            defaults.set(dname, dspec_);
        }
        info.set(DOMAIN_SPEC, dname);

        if (pspec_ != null) {
            info.set(POINT_SPEC, pspec_.write(verbose));
        }
        if (espec_ != null) {
            info.set(ESTIMATION_SPEC, espec_.write(verbose));
        }
        if (priority_ >= 0 || verbose) {
            info.set(PRIORITY, priority_);
        }
        if (quality_ != ProcQuality.Undefined || verbose) {
            info.set(QUALITY, quality_.name());
        }
        if (estimation_ != EstimationPolicyType.None) {
            info.set(POLICY, estimation_.name());
        }
        if (!MetaData.isNullOrEmpty(metaData_)) {
            info.set(METADATA, InformationSetHelper.fromMetaData(metaData_));
        }
        return true;
    }
}
