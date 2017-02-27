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
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.design.Singleton;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.utilities.Jdk6;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Singleton()
public final class SaManager extends AlgorithmManager<ISaSpecification, TsData, CompositeResults, SaDocument<ISaSpecification>, ISaProcessingFactory<?>, ISaOutputFactory, ISaDiagnosticsFactory> {

    public static final SaManager instance = new SaManager();

    private SaManager() {
    }

    public <S extends ISaSpecification> void add(ISaProcessingFactory<S> fac) {
        super.addProcessor(fac);
    }

    public void add(ISaDiagnosticsFactory diag) {
        super.addDiagnostics(diag);
        sortDiagnostics(new ISaDiagnosticsFactory.DiagnosticOrdering());
    }

    public void add(ISaOutputFactory fac) {
        super.addOutput(fac);
    }

    @Override
    public CompositeResults process(ISaSpecification spec, TsData s) {
        return super.process(spec, s);
    }

    @Override
    public ISaProcessingFactory getProcessor(AlgorithmDescriptor desc) {
        return (ISaProcessingFactory) super.getProcessor(desc);
    }

    public ISaSpecification createSpecification(SaItem doc, TsDomain frozenPeriod, EstimationPolicyType policy, boolean nospan) {
        ISaProcessingFactory proc = getProcessor(doc.getEstimationMethod());
        if (proc == null) {
            return null;
        }
        else {
            return proc.createSpecification(doc, frozenPeriod, policy, nospan);
        }
    }

    public ISaSpecification createSpecification(InformationSet info) {
        AlgorithmDescriptor desc = info.get(ISaSpecification.ALGORITHM, AlgorithmDescriptor.class);
        ISaProcessingFactory proc = getProcessor(desc);
        if (proc == null) {
            return null;
        }
        else {
            return proc.createSpecification(info);
        }
    }

    public <S extends ISaSpecification> SaDocument<S> refreshDocument(SaDocument<S> doc, TsDomain frozenPeriod, EstimationPolicyType policy, boolean nospan) {
        // createDiagnostics a new temporary SaItem
        SaItem tmp = new SaItem(doc.getSpecification(), doc.getInput());
        tmp.unsafeFill(doc.getResults());
        if (!updatePointSpecification(tmp)) {
            return null;
        }
        ISaSpecification nspec = createSpecification(tmp, frozenPeriod, policy, nospan);
        if (nspec == null || !doc.getProcessor().canHandle(nspec)) {
            return null;
        }
        SaDocument<S> ndoc = doc.clone();
        ndoc.setSpecification((S) nspec);
        ndoc.setInput(doc.getInput());
        return ndoc;
    }

    public boolean updatePointSpecification(SaItem item) {
        ISaProcessingFactory proc = getProcessor(item.getEstimationMethod());
        if (proc == null) {
            return false;
        }
        else {
            return proc.updatePointSpecification(item);
        }
    }

//    public boolean updateSummary(SaItem item) {
//        ISaProcessingFactory proc = getProcessor(item.getEstimationMethod());
//        if (proc == null) {
//            return false;
//        }
//        else {
//            return proc.updateSummary(item);
//        }
//    }

    private static Information<InformationSet> create(ISaDiagnosticsFactory dfac, CompositeResults sa) {
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
            set.set(test, item);
        }
        List<String> warnings = diags.getWarnings();
        if (warnings != null && !warnings.isEmpty()) {
            set.set(InformationSet.WARNINGS, Jdk6.Collections.toArray( warnings, String.class));
        }
        return new Information<>(diags.getName(), set);
    }

    public static InformationSet createDiagnostics(CompositeResults sa) {
        InformationSet summary = new InformationSet();
        for (IDiagnosticsFactory diag : SaManager.instance.getDiagnostics()) {
            if (diag.isEnabled()) {
                summary.add(create((ISaDiagnosticsFactory) diag, sa));
            }
        }
        return summary;
    }
    
    @Override
    public String getFamily() {
        return GenericSaProcessingFactory.FAMILY;
    }
}
