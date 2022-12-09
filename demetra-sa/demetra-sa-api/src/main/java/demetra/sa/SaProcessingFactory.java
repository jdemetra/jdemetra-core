/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sa;

import demetra.processing.ProcDiagnostic;
import demetra.timeseries.TsDomain;
import java.util.List;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import demetra.information.Explorable;
import demetra.processing.AlgorithmDescriptor;
import demetra.toolkit.dictionaries.Dictionary;

/**
 *
 * @author PALATEJ
 * @param <I> Specification
 * @param <R> Output
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE, mutability = Mutability.NONE, singleton = true)
public interface SaProcessingFactory<I extends SaSpecification, R extends Explorable> {

    AlgorithmDescriptor descriptor();
    
    /**
     * If a processor can handle a given specification, it should be able to
     * execute all the methods of the interface, starting from that
     * specification
     *
     * @param spec
     * @return
     */
    boolean canHandle(SaSpecification spec);

    /**
     * Translate the given specification in a specification that a processor
     * generated
     * by this factory can understand
     *
     * @param spec
     * @return When the given specification is of type I, the same spec is
     * returned.
     * When the given specification cannot be interpreted, null is returned
     */
    I decode(SaSpecification spec);

    /**
     * Creates a processor corresponding to a given specification.
     *
     * @param spec
     * @return A new processor corresponding to that specification
     */
    SaProcessor<R> processor(I spec);

    /**
     * Creates a specification that corresponds exactly to the given result
     * (which
     * means a completely specified model that will generate the same results)
     *
     * @param spec The specification used to generate the results
     * @param estimation
     * @return
     */
    I generateSpec(I spec, R estimation);

    SaSpecification refreshSpec(I currentSpec, I domainSpec, EstimationPolicyType policy, TsDomain frozen);

    List<SaDiagnosticsFactory<?, R> > diagnosticFactories();

    void resetDiagnosticFactories(List<SaDiagnosticsFactory<?, R>> factories);

    default void fillDiagnostics(List<ProcDiagnostic> tests, R sa) {
        for (SaDiagnosticsFactory diag : diagnosticFactories()) {
            if (diag.isActive()) {
                diag.fill(tests, sa, diag.getName());
            }
        }
    }
    
    Dictionary outputDictionary();

}
