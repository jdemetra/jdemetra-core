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
package demetra.ssf.akf;

import demetra.ssf.State;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.OrdinaryFilter;

/**
 *
 * @author Jean Palate
 */
public class AugmentedFilterInitializer implements OrdinaryFilter.Initializer{
    
    private final IAugmentedFilteringResults results;
    
    public AugmentedFilterInitializer(IAugmentedFilteringResults results){
        this.results=results;
    }

    @Override
    public int initialize(State state, ISsf ssf, ISsfData data) {
        AugmentedFilter akf=new AugmentedFilter(true);
        boolean ok = akf.process(ssf, data, results);
        if (! ok)
            return -1;
        AugmentedState astate = akf.getState();
        state.copy(astate);
        int nd=akf.getCollapsingPosition();
        return nd;
    }
    
}
