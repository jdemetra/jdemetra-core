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
package ec.tss.documents;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.TsStatus;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Jean
 * @param <S>
 * @param <R>
 */
public abstract class MultiTsDocument<S extends IProcSpecification, R extends IProcResults> extends ActiveDocument<S, Ts[], R> {

    public static final String SERIES = "series";
    protected IProcessingFactory<S, TsData[], R> factory_;

    public MultiTsDocument(IProcessingFactory<S, TsData[], R> factory) {
        super(factory.getInformation().name);
        this.factory_ = factory;
    }

    public MultiTsDocument(IProcessingFactory<S, TsData[], R> factory, ProcessingContext context) {
        super(factory.getInformation().name, context);
        this.factory_ = factory;
    }

    @Deprecated
    public Ts[] getTs() {
        return getInput();
    }

    @Deprecated
    void setTs(Ts[] ts) {
        setInput(ts);
    }

    @Override
    public void setInput(Ts[] s) {
        if (isLocked() || Arrays.equals(getMonikers(getInput()), getMonikers(s))) {
            return;
        }
        super.setInput(s);
    }

    @Override
    protected R recalc(S spec, Ts[] input) {
        if (input == null) {
            return null;
        }
//        for (int i = 0; i < input.length; ++i) {
//            if (input[i] == null) {
//                return null;
//            }
//        }
        TsData[] dinput = new TsData[input.length];
        for (int i = 0; i < input.length; ++i) {
            Ts s = input[i];
            if (s != null) {
                if (s.hasData() == TsStatus.Undefined) {
                    s.load(TsInformationType.Data);
                }
                TsData d = s.getTsData();
                if (d == null) {
                    throw new TsException(s.getRawName() + ": No data");
                } else {
                    dinput[i] = d;
                }
            } else {
                dinput[i] = null;
            }
        }
        IProcessing<TsData[], R> processing = factory_.generateProcessing(spec, getContext());
        return processing.process(dinput);
    }

    @Override
    public MultiTsDocument<S, R> clone() {
        MultiTsDocument<S, R> cl = (MultiTsDocument<S, R>) super.clone();
        cl.setInput(clone(getInput()), true);
        return cl;
    }

    public IProcessingFactory<S, TsData[], R> getProcessor() {
        return factory_;
    }

    public TsMoniker[] getMonikers() {
        return getMonikers(getInput());
    }

    private static TsMoniker[] getMonikers(Ts[] ts) {
        if (ts == null) {
            return null;
        }
        TsMoniker[] monikers = new TsMoniker[ts.length];
        for (int i = 0; i < ts.length; ++i) {
            monikers[i] = ts[i] != null ? ts[i].getMoniker() : null;
        }
        return monikers;
    }

    public boolean isTsFrozen() {
        Ts[] ts = getInput();
        if (ts == null) {
            return false;
        }
        for (int i = 0; i < ts.length; ++i) {
            if (ts[i].isFrozen()) {
                return true;
            }
        }
        return false;
    }

    public void freezeTs() {
        Ts[] ts = getInput();
        if (ts == null) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < ts.length; ++i) {
            if (!ts[i].isFrozen()) {
                ts[i] = ts[i].freeze();
                changed = true;
            }
        }
        if (changed) {
            super.setInput(ts, true);
        }
    }

    public void unfreezeTs() {
        Ts[] ts = getInput();
        if (ts == null) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < ts.length; ++i) {
            if (ts[i].isFrozen()) {
                ts[i] = ts[i].unfreeze();
                changed = true;
            }
        }
        if (changed) {
            super.setInput(ts, false);
            getMetaData().put(MetaData.DATE, new Date().toString());
        }
    }

    @Override
    public void setSpecification(S spec) {
        super.setSpecification(spec);
    }

    @Override
    public InformationSet write(boolean verbose) {
        Ts[] ts = getInput();
        InformationSet info = super.write(verbose);
        info.add(ALGORITHM, factory_.getInformation());
        if (ts != null) {
            TsInformation tsinfo;
            for (int i = 0; i < ts.length; ++i) {
                if (ts[i].getMoniker().isAnonymous()) {
                    tsinfo = new TsInformation(ts[i], TsInformationType.All);
                } else {
                    tsinfo = new TsInformation(ts[i].freeze(), TsInformationType.All);
                }
                info.subSet(INPUT).add(SERIES + i, tsinfo);
            }
        }
        S spec = getSpecification();
        if (spec != null) {
            info.set(SPEC, spec.write(verbose));
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (!super.read(info)) {
            return false;
        }
        AlgorithmDescriptor algorithm = info.get(ALGORITHM, AlgorithmDescriptor.class);
        if (algorithm == null || !factory_.getInformation().isCompatible(algorithm)) {
            return false;
        }
        InformationSet input = info.getSubSet(INPUT);
        if (input != null) {
            List<Information<TsInformation>> sel = input.select(SERIES + '*', TsInformation.class);
            Collections.sort(sel, new Information.IndexedNameSorter(SERIES));
            Ts[] s = new Ts[sel.size()];
            for (int i = 0; i < s.length; ++i) {
                TsInformation tsinfo = sel.get(i).value;
                s[i] = TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker, tsinfo.metaData, tsinfo.data);
            }
            setInput(s, true);
        }
        InformationSet ispec = info.getSubSet(SPEC);
        if (ispec != null) {
            S spec = getSpecification();
            if (spec == null) {
                return false;
            } else {
                return spec.read(ispec);
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        return factory_.getInformation().name; //To change body of generated methods, choose Tools | Templates.
    }

    public static Ts[] clone(Ts[] s) {
        if (s == null || s.length == 0) {
            return s;
        } else {
            Ts[] ns = s.clone();
            for (int i = 0; i < ns.length; ++i) {
                ns[i] = TsDocument.clone(s[i]);
            }
            return ns;
        }
    }
}
