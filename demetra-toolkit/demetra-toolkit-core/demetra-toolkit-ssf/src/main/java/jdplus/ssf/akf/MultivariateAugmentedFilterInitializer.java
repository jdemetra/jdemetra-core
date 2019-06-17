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
package jdplus.ssf.akf;

import jdplus.ssf.State;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.IMultivariateSsfData;
import jdplus.ssf.multivariate.MultivariateOrdinaryFilter;

/**
 *
 * @author Jean Palate
 */
public class MultivariateAugmentedFilterInitializer implements MultivariateOrdinaryFilter.Initializer{
    
    private final IMultivariateAugmentedFilteringResults results;
    
    public MultivariateAugmentedFilterInitializer(IMultivariateAugmentedFilteringResults results){
        this.results=results;
    }

    @Override
    public int initialize(State state, IMultivariateSsf ssf, IMultivariateSsfData data) {
        MultivariateAugmentedFilter akf=new MultivariateAugmentedFilter(true);
        boolean ok = akf.process(ssf, data, results);
        if (! ok)
            return -1;
        AugmentedState astate = akf.getState();
        if (! results.collapse(astate))
            return -1;
        state.copy(astate);
        return akf.getCollapsingPosition();
    }
    
}
