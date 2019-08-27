/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import demetra.data.DoubleSeq;
import java.util.ArrayList;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.msts.StateItem;
import jdplus.ssf.CompositeLoading;
import jdplus.ssf.CompositeState;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
public class AggregationItem extends StateItem {

    private final StateItem[] cmps;

    public AggregationItem(String name, StateItem[] cmps) {
        super(name);
        this.cmps = cmps;
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        CompositeState.Builder builder = CompositeState.builder();
        int pos = 0;
        for (int i = 0; i < cmps.length; ++i) {
            int n = cmps[i].parametersCount();
            builder.add(cmps[i].build(p.extract(pos, n)));
            pos += n;
        }
        return builder.build();
    }

    @Override
    public int parametersCount() {
        int n = 0;
        for (int i = 0; i < cmps.length; ++i) {
            n += cmps[i].parametersCount();
        }
        return n;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        }
        int[] dim = new int[cmps.length];
        ISsfLoading[] l = new ISsfLoading[cmps.length];

        for (int i = 0; i < cmps.length; ++i) {
            dim[i] = cmps[i].stateDim();
            l[i] = cmps[i].defaultLoading(0);
        }
        return new CompositeLoading(dim, l);
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        List<ParameterInterpreter> all = new ArrayList<>();
        for (int i = 0; i < cmps.length; ++i) {
            all.addAll(cmps[i].parameters());
        }
        return all;
    }

    @Override
    public int stateDim() {
        int dim = 0;
        for (int i = 0; i < cmps.length; ++i) {
            dim += cmps[i].stateDim();
        }
        return dim;
    }

}
