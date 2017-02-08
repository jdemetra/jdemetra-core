/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.xml.sa.x13;

import ec.demetra.xml.core.XmlInformationSet;
import ec.demetra.xml.core.XmlTs;
import ec.demetra.xml.processing.XmlProcessingContext;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x13.X13Specification;
import ec.tss.Ts;
import ec.tss.TsInformationType;
import ec.tss.TsStatus;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.algorithm.implementation.RegArimaProcessingFactory;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Jean Palate
 */
public class X13XmlProcessor {

    private static final String[] DEF_SA = new String[]{"final.y", "final.t", "final.s", "final.i", "final.sa", "final.ycal", "final.y_f", "final.t_f", "final.s_f", "final.i_f", "final.sa_f"};

    public XmlInformationSet process(XmlRegArimaRequest request) {
        // specification
        RegArimaSpecification spec = specification(request);
        Ts s = XmlTs.TS_UNMARSHALLER.unmarshal(request.series);
        if (s == null) {
            return null;
        }
        if (s.hasData() == TsStatus.Undefined) {
            s.load(TsInformationType.Data);
        }
        if (spec == null || s.getTsData() == null) {
            return null;
        }
        ProcessingContext ctx = context(request.context);
        IProcessing<TsData, PreprocessingModel> processing = RegArimaProcessingFactory.instance.generateProcessing(spec, ctx);
        PreprocessingModel model = processing.process(s.getTsData());
        TreeSet<String> set = new TreeSet<>();
        List<String> items = request.getOutputFilter();
        set.addAll(items);
        InformationSet info = InformationSetHelper.fromProcResults(model, set);
        XmlInformationSet xinfo = new XmlInformationSet();
        if (request.getFlat()) {
            xinfo.flatCopy(info);
        } else {
            xinfo.copy(info);
        }
        return xinfo;
    }

    public XmlInformationSet process(XmlX13Request request) {
        // specification
        X13Specification spec = specification(request);
        Ts s = XmlTs.TS_UNMARSHALLER.unmarshal(request.series);
        if (s == null) {
            return null;
        }
        if (s.hasData() == TsStatus.Undefined) {
            s.load(TsInformationType.Data);
        }
        if (spec == null || s.getTsData() == null) {
            return null;
        }
        ProcessingContext ctx = context(request.context);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(spec, ctx);
        CompositeResults rslt = processing.process(s.getTsData());
        TreeSet<String> set = new TreeSet<>();
        if (request.outputFilter != null) {
            List<String> items = request.getOutputFilter();
            set.addAll(items);
        } else {
            for (int i = 0; i < DEF_SA.length; ++i) {
                set.add(DEF_SA[i]);
            }
        }
        InformationSet info = InformationSetHelper.fromProcResults(rslt, set);
        XmlInformationSet xinfo = new XmlInformationSet();
        if (request.getFlat()) {
            xinfo.flatCopy(info);
        } else {
            xinfo.copy(info);
        }
        return xinfo;
    }

    public XmlInformationSet process(final XmlRegArimaRequests request) {
        ProcessingContext ctx = context(request.context);
        Stream<XmlRegArimaAtomicRequest> stream = request.getParallelProcessing()
                ? request.getItems().parallelStream() : request.getItems().stream();
        List<PreprocessingModel> models = stream.map(
                o -> {
                    try {
                        RegArimaSpecification spec = specification(o);
                        Ts s = XmlTs.TS_UNMARSHALLER.unmarshal(o.series);
                        if (s == null) {
                            return null;
                        }
                        if (s.hasData() == TsStatus.Undefined) {
                            s.load(TsInformationType.Data);
                        }
                        if (spec == null || s.getTsData() == null) {
                            return null;
                        }
                        IProcessing<TsData, PreprocessingModel> processing = RegArimaProcessingFactory.instance.generateProcessing(spec, ctx);
                        PreprocessingModel model = processing.process(s.getTsData());
                        return model;
                    } catch (Exception err) {
                        return null;
                    }
                }
        ).collect(Collectors.toList());

        TreeSet<String> set = new TreeSet<>();
        List<String> items = request.getOutputFilter();
        set.addAll(items);
        InformationSet infos = new InformationSet();
        int icur = 0;
        for (PreprocessingModel cur : models) {
            ++icur;
            if (cur != null) {
                InformationSet info = InformationSetHelper.fromProcResults(cur, set);
                infos.set("series" + icur, info);
            }
        }
        XmlInformationSet xinfo = new XmlInformationSet();
        if (request.getFlat()) {
            xinfo.flatCopy(infos);
        } else {
            xinfo.copy(infos);
        }
        return xinfo;
    }

    public XmlInformationSet process(XmlX13Requests request) {
        ProcessingContext ctx = context(request.context);
        Stream<XmlX13AtomicRequest> stream = request.getParallelProcessing()
                ? request.getItems().parallelStream() : request.getItems().stream();
        List<CompositeResults> models = stream.map(
                o -> {
                    try {
                        X13Specification spec = specification(o);
                        Ts s = XmlTs.TS_UNMARSHALLER.unmarshal(o.series);
                        if (s == null) {
                            return null;
                        }
                        if (s.hasData() == TsStatus.Undefined) {
                            s.load(TsInformationType.Data);
                        }
                        if (spec == null || s.getTsData() == null) {
                            return null;
                        }
                        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(spec, ctx);
                        CompositeResults rslt = processing.process(s.getTsData());
                        return rslt;
                    } catch (Exception err) {
                        return null;
                    }
                }
        ).collect(Collectors.toList());

        TreeSet<String> set = new TreeSet<>();
        if (request.outputFilter != null) {
            List<String> items = request.getOutputFilter();
            set.addAll(items);
        } else {
            for (int i = 0; i < DEF_SA.length; ++i) {
                set.add(DEF_SA[i]);
            }
        }
        InformationSet infos = new InformationSet();
        int icur = 0;
        for (CompositeResults cur : models) {
            ++icur;
            if (cur != null) {
                InformationSet info = InformationSetHelper.fromProcResults(cur, set);
                infos.set("series" + icur, info);
            }
        }
        XmlInformationSet xinfo = new XmlInformationSet();
        if (request.getFlat()) {
            xinfo.flatCopy(infos);
        } else {
            xinfo.copy(infos);
        }
        return xinfo;
    }

    public static RegArimaSpecification specification(XmlRegArimaAtomicRequest request) {
        if (request.defaultSpecification != null) {
            return RegArimaSpecification.fromString(request.defaultSpecification);
        } else {
            RegArimaSpecification spec = new RegArimaSpecification();
            XmlRegArimaSpecification.UNMARSHALLER.unmarshal(request.specification, spec);
            return spec;
        }
    }

    public static ProcessingContext context(XmlProcessingContext ctx) {
        if (ctx == null) {
            return ProcessingContext.getActiveContext();
        } else {
            ProcessingContext c = new ProcessingContext();
            XmlProcessingContext.UNMARSHALLER.unmarshal(ctx, c);
            return c;
        }
    }

    public static X13Specification specification(XmlX13AtomicRequest request) {
        if (request.defaultSpecification != null) {
            return X13Specification.fromString(request.defaultSpecification);
        } else {
            X13Specification spec = new X13Specification();
            XmlX13Specification.UNMARSHALLER.unmarshal(request.specification, spec);
            return spec;
        }
    }
}
