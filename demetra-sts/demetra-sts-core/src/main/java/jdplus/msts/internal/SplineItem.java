/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.sts.splines.SplineComponent;
import jdplus.ssf.sts.splines.SplineData;

/**
 *
 * @author palatej
 */
public class SplineItem extends StateItem {

    private final VarianceInterpreter v;
    private final SplineData data;
    private final int startpos;

    public SplineItem(String name, SplineData data, int startpos,double cvar, boolean fixedvar) {
        super(name);
        v = new VarianceInterpreter(name + ".var", cvar, fixedvar, true);
        this.data = data;
        this.startpos = startpos;
    }

    private SplineItem(SplineItem item) {
        super(item.name);
        this.data = item.data;
        this.startpos = item.startpos;
        v = item.v.duplicate();
    }

    @Override
    public SplineItem duplicate() {
        return new SplineItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double var = p.get(0);
            builder.add(name, SplineComponent.stateComponent(data, var, startpos), SplineComponent.loading(data, startpos));
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double var = p.get(0);
        return SplineComponent.stateComponent(data, var, startpos);
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        }
        return SplineComponent.loading(data, startpos);
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return data.getDim();
    }

    @Override
    public boolean isScalable() {
        return !v.isFixed();
    }

}
