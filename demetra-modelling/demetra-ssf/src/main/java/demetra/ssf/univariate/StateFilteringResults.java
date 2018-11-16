/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;
import demetra.ssf.UpdateInformation;

/**
 *
 * @author palatej
 */
public class StateFilteringResults extends StateStorage implements IFilteringResults{
    
    public StateFilteringResults(final StateInfo info, final boolean cov){
        super(info, cov);
    }

    @Override
    public void save(int t, UpdateInformation pe) {
    }
    
}
