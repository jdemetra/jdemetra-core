/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import demetra.data.DoubleSeq;
import java.util.List;
import jdplus.benchmarking.ssf.SsfCumulator;
import jdplus.msts.MstsMapping;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
public class CumulatorItem extends StateItem {

    private final StateItem core;
    private final int period, start;

    public CumulatorItem(String name, StateItem core, int period, int start) {
        super(name);
        this.core = core;
        this.period = period;
        this.start = start;
    }

    @Override
    public void addTo(MstsMapping mapping) {
        List<ParameterInterpreter> parameters = core.parameters();
        for (ParameterInterpreter p : parameters) {
            mapping.add(p);
        }
        mapping.add((p, builder) -> {
            StateComponent cmp = core.build(p);
            ISsfLoading l = core.defaultLoading(0);
            builder.add(name, SsfCumulator.stateComponent(cmp, l, period, start), SsfCumulator.loading(l, period, start));
            return core.parametersCount();
        });

    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return core.parameters();
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        StateComponent cmp = core.build(p);
        ISsfLoading l = core.defaultLoading(0);
        return SsfCumulator.stateComponent(cmp, l, period, start);
    }

    @Override
    public int parametersCount() {
        return core.parametersCount();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        }
        ISsfLoading l = core.defaultLoading(0);
        return SsfCumulator.loading(l, period, start);
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

}
