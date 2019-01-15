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
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Date;

/**
 *
 * @author Jean Palate
 */
public class TsDocument<S extends IProcSpecification, R extends IProcResults> extends ActiveDocument<S, Ts, R> {

    public static final String SERIES = "series";
    private final IProcessingFactory<S, TsData, R> factory_;

    public TsDocument(IProcessingFactory<S, TsData, R> factory) {
        super(factory.getInformation().name);
        factory_ = factory;
    }

    public TsDocument(IProcessingFactory<S, TsData, R> factory, ProcessingContext context) {
        super(factory.getInformation().name, context);
        factory_ = factory;
    }

    @Override
    protected R recalc(S spec, Ts s) {
        if (s.hasData() == TsStatus.Undefined) {
            s.load(TsInformationType.Data);
        }
        TsData d = s.getTsData();
        if (d == null) {
            throw new TsException(s.getRawName() + ": No data");
        }
        IProcessing<TsData, R> processing = factory_.generateProcessing(spec, getContext());
        return processing.process(d);
    }

    public IProcessingFactory<S, TsData, R> getProcessor() {
        return factory_;
    }

    @Deprecated
    public void setTs(Ts s) {
        setInput(s);
    }

    @Override
    public void setInput(Ts s) {
        if (getInput() != null && getInput().getMoniker().equals(s.getMoniker())) {
            return;
        }
        super.setInput(s.freeze());
    }

    public boolean isTsFrozen() {
        if (getInput() == null) {
            return false;
        }
        return getInput().isFrozen();
    }

    public void unfreezeTs() {
        Ts ts = getInput();
        if (ts == null || !ts.isFrozen()) {
            return;
        }
        ts = ts.unfreeze();
        setInput(ts);
        getMetaData().put(MetaData.DATE, new Date().toString());
    }

    public TsData getSeries() {
        Ts ts = getInput();
        if (ts == null) {
            return null;
        }
        if (ts.hasData() == TsStatus.Undefined) {
            ts.load(TsInformationType.Data);
        }
        R rslts = getResults();
        if (rslts instanceof CompositeResults) {
            CompositeResults crslts = (CompositeResults) rslts;
            SingleTsData input = crslts.get(INPUT, SingleTsData.class);
            if (input != null) {
                return input.getSeries();
            } else {
                return getInput().getTsData();
            }
        } else {
            return ts.getTsData();
        }
    }

    @Deprecated
    public Ts getTs() {
        return getInput();
    }

    public TsMoniker getMoniker() {
        Ts ts = getInput();

        return ts == null ? null : ts.getMoniker();
    }

    @Override
    public TsDocument<S, R> clone() {
        TsDocument<S, R> cl = (TsDocument<S, R>) super.clone();
        cl.setInput(clone(getInput()), true);
        return cl;
    }

    @Override
    public void setSpecification(S spec) {
        super.setSpecification(spec);
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = super.write(verbose);
        info.add(ALGORITHM, factory_.getInformation());
        Ts ts = getInput();
        if (ts != null) {
            TsInformation tsinfo;
            if (ts.getMoniker().isAnonymous()) {
                tsinfo = ts.toInfo(TsInformationType.All);
            } else {
                tsinfo = ts.freeze().toInfo(TsInformationType.All);
            }
            info.subSet(INPUT).add(SERIES, tsinfo);
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
            TsInformation tsinfo = input.get(SERIES, TsInformation.class);
            if (tsinfo != null) {
                super.setInput(TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker, tsinfo.metaData, tsinfo.data), true);
            }
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

    protected void updateLinks() {
        DocumentManager.instance.update(this);
    }

    @Override
    public String getDescription() {
        return factory_.getInformation().name; //To change body of generated methods, choose Tools | Templates.
    }

    public static Ts clone(Ts s) {
        if (!s.isFrozen()) {
            return s;
        } else {
            return TsFactory.instance.createTs(s.getRawName(), s.getMetaData().clone(), s.getTsData());
        }
    }

}
