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
package jdplus.ssf.univariate;

import jdplus.ssf.UpdateInformation;
import jdplus.ssf.State;
import jdplus.ssf.StateInfo;

/**
 *
 * @author Jean Palate
 */
public class CompositeFilteringResults implements IFilteringResults {
    
    
    private final IFilteringResults[] subresults;
    public CompositeFilteringResults(final IFilteringResults... subresults){
        this.subresults=subresults;
    }

    @Override
    public void save(int t, UpdateInformation pe) {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].save(t, pe);
        }
    }

    @Override
    public void save(final int t, final State state, final StateInfo info) {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].save(t, state, info);
        }
    }

    @Override
    public void clear() {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].clear();
        }
    }
    
}
