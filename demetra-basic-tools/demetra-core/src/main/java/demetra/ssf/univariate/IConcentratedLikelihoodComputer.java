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
package demetra.ssf.univariate;

import demetra.likelihood.IConcentratedLikelihood;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <L>
 */
public interface IConcentratedLikelihoodComputer<L extends IConcentratedLikelihood> extends ILikelihoodComputer<L>{
    L compute(SsfRegressionModel model);
    
    @Override
    default L compute(ISsf ssf, ISsfData data){
        SsfRegressionModel model=new SsfRegressionModel(ssf, data);
        return compute(model);
    }
}