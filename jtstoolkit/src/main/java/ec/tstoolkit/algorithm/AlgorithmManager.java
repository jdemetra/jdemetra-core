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

package ec.tstoolkit.algorithm;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The algorithm manager class offer services for generic algorithms:
 *
 * management of - the processors - the diagnostics factories - the output
 * factories
 *
 *
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AlgorithmManager<S extends IProcSpecification, I, R extends IProcResults, D extends IProcDocument<? extends S, ?, ? extends R>, P extends IProcessingFactory<? extends S, I, R>, O extends IOutputFactory<? extends D>, T extends IDiagnosticsFactory<R>> 
implements IAlgorithmManager <S, I, R, P>{

    private ArrayList<P> processors_ = new ArrayList<>();
    private ArrayList<O> output_ = new ArrayList<>();
    private ArrayList<T> tests_ = new ArrayList<>();

    public static InformationSet getActiveContext(){
        return null;
    }
       
    public InformationSet diagnostic(R sa) {
        InformationSet summary = new InformationSet();
        for (IDiagnosticsFactory<R> diag : tests_) {
            if (diag.isEnabled()) {
                summary.add(create(diag, sa));
            }
        }
        return summary;
    }

    private Information<InformationSet> create(IDiagnosticsFactory<R> dfac, R sa) {
        if (sa == null) {
            return null;
        }
        IDiagnostics diags = dfac.create(sa);
        if (diags == null) {
            return null;
        }
        InformationSet set = new InformationSet();
        for (String test : diags.getTests()) {
            double val = diags.getValue(test);
            ProcDiagnostic item = new ProcDiagnostic(val, diags.getDiagnostic(test));
            set.set(test.toLowerCase(), item);
        }
        List<String> warnings = diags.getWarnings();
        if (warnings != null) {
            for (String w : warnings) {
                set.addWarning(w);
            }
        }
        return new Information<>(diags.getName().toLowerCase(), set);
    }

    public void sortProcessors(Comparator<P> cmp) {
        Collections.sort(processors_, cmp);
    }

    public void sortOutput(Comparator<O> cmp) {
        Collections.sort(output_, cmp);
    }

    public void sortDiagnostics(Comparator<T> cmp) {
        Collections.sort(tests_, cmp);
    }
    public <T extends S> R process(T spec, I input) {
        return process(spec, input, null);
    }
    
    public <T extends S> R process(T spec, I input, ProcessingContext context) {
        for (IProcessingFactory processor : processors_) {
            if (processor.canHandle(spec)) {
                IProcessing<I, R> processing = processor.generateProcessing(spec, context);
                return processing.process(input);
            }
        }
        return null;
    }
    
    public <T extends S> IProcessingFactory find(T spec){
        for (IProcessingFactory processor : processors_) {
            if (processor.canHandle(spec)) {
                return processor;
            }
        }
        return null;
    }

    @Override
    public List<P> getProcessors() {
        return Collections.unmodifiableList(processors_);
    }

    public List<O> getOutput() {
        return Collections.unmodifiableList(output_);
    }

    public List<T> getDiagnostics() {
        return Collections.unmodifiableList(tests_);
    }

    public P getProcessor(AlgorithmDescriptor desc) {
        if (desc == null)
            return null;
        for (P processor : processors_) {
            if (desc.isCompatible(processor.getInformation())) {
                return processor;
            }
        }
        return null;
    }

    protected T addDiagnostics(String module, String impl) {
        T diag = (T) ec.tstoolkit.design.InterfaceLoader.create(module, IDiagnosticsFactory.class, impl);
        if (diag != null) {
            tests_.add(diag);
        }
        return diag;
    }

    protected O addOutput(String module, String impl) {
        O o = (O) ec.tstoolkit.design.InterfaceLoader.create(module, IOutputFactory.class, impl);
        if (o != null) {
            output_.add(o);
        }
        return o;
    }

    protected void addDiagnostics(T diag) {
        for (T t : tests_) {
            if (t.getClass().equals(diag.getClass())) {
                return;
            }
        }
        tests_.add(diag);
    }

    protected void addOutput(O diag) {
        for (IOutputFactory<? extends D> o : output_) {
            if (o.getClass().equals(diag.getClass())) {
                return;
            }
        }
        output_.add(diag);
    }

    protected void addProcessor(P proc) {
        processors_.add(proc);
    }

    public AlgorithmManager() {
    }

    @Override
    public void dispose() {
        for (O output : output_) {
            output.dispose();
        }
        for (T diag : tests_) {
            diag.dispose();
        }
        for (P processor : processors_) {
            processor.dispose();
        }
    }
}
