/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq;

import demetra.highfreq.ExtendedAirlineDecompositionSpec;
import demetra.processing.ProcessingLog;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import jdplus.sa.modelling.TwoStepsDecomposition;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecompositionKernel {

    public static final String EA = "extended airline";

    private final ExtendedAirlineKernel preprocessor;
    private final DecompositionKernel decomposer;

    public ExtendedAirlineDecompositionKernel(ExtendedAirlineDecompositionSpec spec, ModellingContext context) {
        this.preprocessor = new ExtendedAirlineKernel(spec.getPreprocessing(), context);
        this.decomposer = new DecompositionKernel(spec.getDecomposition());
    }

    public ExtendedAirlineResults process(TsData y, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        ExtendedRegAirlineModel preprocessing = preprocessor.process(y, log);
        TsData lin = preprocessing.linearizedSeries();
        boolean mul=preprocessing.getDescription().isLogTransformation();
        ExtendedAirlineDecomposition decomp = decomposer.process(lin, mul, log);
        
        // compute the final decomposition
        SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, decomp.getFinalComponents());
        
        return ExtendedAirlineResults.builder()
                .preprocessing(preprocessing)
                .decomposition(decomp)
                .finals(finals)
                .log(log)
                .build();
    }
    
    
}
