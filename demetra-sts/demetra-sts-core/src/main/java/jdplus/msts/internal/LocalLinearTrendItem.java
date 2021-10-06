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
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.sts.LocalLinearTrend;

/**
 *
 * @author palatej
 */
public class LocalLinearTrendItem extends StateItem {

    public final VarianceInterpreter lv, sv;

    public LocalLinearTrendItem(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
        super(name);
        lv = new VarianceInterpreter(name + ".lvar", lvar, lfixed, true);
        sv = new VarianceInterpreter(name + ".svar", svar, sfixed, true);
    }

    private LocalLinearTrendItem(LocalLinearTrendItem item) {
        super(item.name);
        this.lv = item.lv.duplicate();
        this.sv = item.sv.duplicate();
     }

    @Override
    public LocalLinearTrendItem duplicate() {
        return new LocalLinearTrendItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(lv);
        mapping.add(sv);
        mapping.add((p, builder) -> {
            double e1 = p.get(0);
            double e2 = p.get(1);
            StateComponent cmp = jdplus.sts.LocalLinearTrend.of(e1, e2);
            builder.add(name, cmp, LocalLinearTrend.defaultLoading());
            return 2;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(lv, sv);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double e1 = p.get(0);
        double e2 = p.get(1);
        return LocalLinearTrend.of(e1, e2);
    }

    @Override
    public int parametersCount() {
        return 2;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return LocalLinearTrend.defaultLoading();
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return 2;
    }
    
    @Override
    public boolean isScalable() {
        return !lv.isFixed() && ! sv.isFixed();
    }
}
