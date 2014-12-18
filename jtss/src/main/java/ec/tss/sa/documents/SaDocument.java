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

package ec.tss.sa.documents;

import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.tss.Ts;
import ec.tss.documents.TsDocument;
import ec.tss.sa.ISaProcessingFactory;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author pcuser
 */
public abstract class SaDocument<S extends ISaSpecification> extends TsDocument<S, CompositeResults> {

    protected SaDocument(ISaProcessingFactory<S> factory) {
        super(factory);
    }

    protected SaDocument(ISaProcessingFactory<S> factory, ProcessingContext context) {
        super(factory, context);
    }
    
    public SaDocument<S> clone() {
        return (SaDocument<S>) super.clone();
    }
    
    public PreprocessingModel getPreprocessingPart() {
        CompositeResults rslts = getResults();
        return GenericSaResults.getPreprocessingModel(rslts);
    }

    public abstract IProcResults getDecompositionPart();

    public ISeriesDecomposition getFinalDecomposition() {
        CompositeResults rslts = getResults();
        return GenericSaResults.getFinalDecomposition(rslts);

    }

    public SaBenchmarkingResults getBenchmarking() {
        CompositeResults rslts = getResults();
        return GenericSaResults.getBenchmarking(rslts);
    }

    public boolean unsafeFill(Ts ts, ISaSpecification estimationSpecification, CompositeResults process) {
        if (!getProcessor().canHandle(estimationSpecification)) {
            return false;
        }
        setTs(ts);
        setSpecification((S) estimationSpecification);
        results(process);
        updateLinks();
        return true;
    }
}
