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
package jdplus.ssf.ckms;

import jdplus.ssf.akf.AugmentedFilterInitializer;
import jdplus.ssf.akf.AugmentedPredictionErrorDecomposition;
import jdplus.ssf.dk.DiffusePredictionErrorDecomposition;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import jdplus.ssf.univariate.ILikelihoodComputer;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinaryFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CkmsToolkit {

    public static ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer() {
        return (ISsf ssf, ISsfData data) -> {
            DiffusePredictionErrorDecomposition decomp = new DiffusePredictionErrorDecomposition(false);
            CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(new DiffuseSquareRootInitializer(decomp));
            CkmsFilter ffilter = new CkmsFilter(ff);
            ffilter.process(ssf, data, decomp);
            return decomp.likelihood();
        };
    }

}
