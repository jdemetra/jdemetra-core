/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;
import demetra.ssf.UpdateInformation;
import demetra.ssf.akf.AugmentedState;
import demetra.ssf.dk.DiffuseState;
import demetra.ssf.dk.DiffuseUpdateInformation;
import demetra.ssf.dk.IDiffuseFilteringResults;
import demetra.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;

/**
 *
 * @author palatej
 */
public class StateFilteringResults extends StateStorage implements IDiffuseFilteringResults, IDiffuseSquareRootFilteringResults {

    public StateFilteringResults(final StateInfo info, final boolean cov) {
        super(info, cov);
    }

    private int enddiffuse;

    @Override
    public void save(int t, UpdateInformation pe) {
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
    }

    @Override
    public void save(int t, DiffuseState state, StateInfo info) {
        if (state.isDiffuse()) {
            P(t).set(Double.NaN);
            a(t).set(Double.NaN);
        } else {
            save(t, (State) state, info);
        }
    }

    @Override
    public void save(int pos, AugmentedState state, StateInfo info) {
       if (state.isDiffuse()) {
            P(pos).set(Double.NaN);
            a(pos).set(Double.NaN);
        } else {
            save(pos, (State) state, info);
        }
    }
    
    @Override
    public void close(int pos) {
        enddiffuse = pos;
    }

    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }


}
