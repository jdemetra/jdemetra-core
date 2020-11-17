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
import jdplus.ssf.implementations.Noise;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class WeightedNoiseItem extends StateItem {

    private final VarianceInterpreter v;
    private final double[] w;

    public WeightedNoiseItem(String name, double[] w, double var, boolean fixed) {
        super(name);
        this.v = new VarianceInterpreter(name + ".var", var, fixed, true);
        this.w=w;
    }
    
    private WeightedNoiseItem(WeightedNoiseItem item){
        super(item.name);
        v=item.v.duplicate();
        this.w=item.w;
    }
    
    @Override
    public WeightedNoiseItem duplicate(){
        return new WeightedNoiseItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            StateComponent cmp = Noise.of(e, w);
            builder.add(name, cmp, Noise.defaultLoading());
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
            double e = p.get(0);
            return Noise.of(e, w);
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        } else {
            return Noise.defaultLoading();
        }
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim(){
        return 1;
    }

}
