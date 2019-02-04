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
package demetra.ssf.dk.sqrt;

import demetra.ssf.UpdateInformation;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.akf.AugmentedState;
import demetra.ssf.dk.DiffuseUpdateInformation;

/**
 *
 * @author Jean Palate
 */
public class CompositeDiffuseSquareRootFilteringResults implements IDiffuseSquareRootFilteringResults {
    
    
    private final IDiffuseSquareRootFilteringResults[] subresults;
    public CompositeDiffuseSquareRootFilteringResults(final IDiffuseSquareRootFilteringResults... subresults){
        this.subresults=subresults;
    }

    @Override
    public void close(int pos) {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].close(pos);
        }
    }

    @Override
    public void clear() {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].clear();
        }
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].save(t, pe);
        }
    }

    @Override
    public void save(final int pos, final AugmentedState state, final StateInfo info) {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].save(pos, state, info);
        }
    }

    @Override
    public void save(int t, UpdateInformation pe) {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].save(t, pe);
        }
    }

    @Override
    public void save(final int pos, final State state, final StateInfo info) {
        for (int i=0; i<subresults.length; ++i){
            subresults[i].save(pos, state, info);
        }
    }
    
    @Override
    public int getEndDiffusePosition() {
        for (int i = 0; i < subresults.length; ++i) {
            int epos = subresults[i].getEndDiffusePosition();
            if (epos >= 0) {
                return epos;
            }
        }
        return -1;
    }

}
