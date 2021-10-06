/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.msts.MstsMapping;
import jdplus.sts.LocalLevel;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.msts.ScaleInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.sts.VarLocalLevel;

/**
 *
 * @author palatej
 */
public class VarLocalLevelItem extends StateItem {

    public final ScaleInterpreter scale;
    private final double[] stde;
    public final double initial;

    public VarLocalLevelItem(String name, final double[] stde, final double lscale, final boolean fixed, final double initial) {
        super(name);
        this.initial = initial;
        this.stde = stde;
        this.scale = new ScaleInterpreter(name + ".scale", lscale, fixed, true);
    }

    private VarLocalLevelItem(VarLocalLevelItem item) {
        super(item.name);
        this.scale = item.scale.duplicate();
        this.initial = item.initial;
        this.stde = item.stde;
    }

    @Override
    public VarLocalLevelItem duplicate() {
        return new VarLocalLevelItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(scale);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            StateComponent cmp = VarLocalLevel.of(stde, e, initial);
            builder.add(name, cmp, VarLocalLevel.defaultLoading());
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(scale);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double e = p.get(0);
        return VarLocalLevel.of(stde, e, initial);
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return LocalLevel.defaultLoading();
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return 1;
    }

    @Override
    public boolean isScalable() {
        return !scale.isFixed();
    }

}
