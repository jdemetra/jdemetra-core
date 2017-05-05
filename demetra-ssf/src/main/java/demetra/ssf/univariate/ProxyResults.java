/*
 * Copyright 2016-2017 National Bank of Belgium
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

import demetra.ssf.UpdateInformation;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ProxyResults implements IFilteringResults {
    
    private final StateStorage states;
    
    public ProxyResults(StateStorage states) {
        this.states = states;
    }
    
    @Override
    public void save(int t, UpdateInformation pe) {
    }
    
    @Override
    public void clear() {
    }
    
    @Override
    public void save(int pos, State state, StateInfo info) {
        states.save(pos, state, info);
    }
    
}
