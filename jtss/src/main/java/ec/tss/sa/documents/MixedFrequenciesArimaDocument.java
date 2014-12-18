/*
 * Copyright 2013-2014 National Bank of Belgium
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

import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.tss.Ts;
import ec.tss.documents.MultiTsDocument;
import ec.tss.sa.processors.MixedFrequenciesArimaProcessor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.arima.special.mixedfrequencies.MixedFrequenciesModelDecomposition;
import ec.tstoolkit.arima.special.mixedfrequencies.MixedFrequenciesModelEstimation;
import ec.tstoolkit.arima.special.mixedfrequencies.MixedFrequenciesSpecification;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequenciesArimaDocument  extends MultiTsDocument<MixedFrequenciesSpecification, CompositeResults> {

    public MixedFrequenciesArimaDocument() {
        super(MixedFrequenciesArimaProcessor.instance);
        setSpecification(new MixedFrequenciesSpecification());
    }

    public MixedFrequenciesArimaDocument(ProcessingContext context) {
        super(MixedFrequenciesArimaProcessor.instance, context);
        setSpecification(new MixedFrequenciesSpecification());
    }

    @Override
    public void setInput(Ts[] s) {
        if (s != null && s.length != 2) {
            throw new IllegalArgumentException("Mixed frequencies Arima model requires 2 time series");
        }
        super.setInput(s);
    }
    
    public MixedFrequenciesModelEstimation getPreprocessingPart(){
        CompositeResults results = getResults();
        if (results == null)
            return null;
        return results.get(MixedFrequenciesArimaProcessor.PROCESSING, MixedFrequenciesModelEstimation.class);
    }
    
    public MixedFrequenciesModelDecomposition getDecompositionPart(){
        CompositeResults results = getResults();
        if (results == null)
            return null;
        return results.get(MixedFrequenciesArimaProcessor.DECOMPOSITION, MixedFrequenciesModelDecomposition.class);
    }
    
    public ISeriesDecomposition getFinalDecomposition() {
        CompositeResults results = getResults();
        if (results == null)
            return null;
        return results.get(MixedFrequenciesArimaProcessor.FINAL, DefaultSeriesDecomposition.class);
    }

    public SaBenchmarkingResults getBenchmarking() {
       CompositeResults results = getResults();
        if (results == null)
            return null;
        return results.get(MixedFrequenciesArimaProcessor.BENCHMARKING, SaBenchmarkingResults.class);
    }
    
}