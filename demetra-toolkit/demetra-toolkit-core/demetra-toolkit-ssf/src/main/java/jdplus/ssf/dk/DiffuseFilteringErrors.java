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
package jdplus.ssf.dk;

import jdplus.ssf.StateInfo;
import jdplus.ssf.univariate.FilteringErrors;

/**
 *
 * @author Jean Palate
 */
public class DiffuseFilteringErrors extends FilteringErrors implements IDiffuseFilteringResults {

    private int enddiffuse;

    public DiffuseFilteringErrors(boolean normalized) {
        super(normalized);
    }

    @Override
    public void close(int pos) {
        enddiffuse = pos;
    }

    @Override
    public void save(final int pos, final DiffuseState state, final StateInfo info) {
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        super.save(t, pe);
    }

    @Override
    public void clear() {
        super.clear();
        enddiffuse = 0;
    }

    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }
}
