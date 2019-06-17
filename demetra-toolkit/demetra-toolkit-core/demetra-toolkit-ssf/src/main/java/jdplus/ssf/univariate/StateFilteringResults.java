/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.univariate;

import jdplus.ssf.State;
import jdplus.ssf.StateInfo;
import jdplus.ssf.StateStorage;
import jdplus.ssf.UpdateInformation;
import jdplus.ssf.akf.AugmentedState;
import jdplus.ssf.dk.DiffuseState;
import jdplus.ssf.dk.DiffuseUpdateInformation;
import jdplus.ssf.dk.IDiffuseFilteringResults;
import jdplus.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;

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
