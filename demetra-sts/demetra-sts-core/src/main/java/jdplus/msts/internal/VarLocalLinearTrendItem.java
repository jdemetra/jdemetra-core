/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.msts.MstsMapping;
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.msts.ScaleInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.sts.LocalLinearTrend;
import jdplus.sts.VarLocalLinearTrend;

/**
 *
 * @author palatej
 */
public class VarLocalLinearTrendItem extends StateItem {

    public final ScaleInterpreter lscale, sscale;
    private final double[] lstd, sstd;

    public VarLocalLinearTrendItem(final String name, double[] lstd, double[] sstd, double lscale, double sscale, boolean lfixed, boolean sfixed) {
        super(name);
        this.lstd=lstd;
        this.sstd=sstd;
        this.lscale = new ScaleInterpreter(name + ".lvar", lscale, lfixed, true);
        this.sscale = new ScaleInterpreter(name + ".svar", sscale, sfixed, true);
    }

    private VarLocalLinearTrendItem(VarLocalLinearTrendItem item) {
        super(item.name);
        this.lscale = item.lscale.duplicate();
        this.sscale = item.sscale.duplicate();
        this.lstd=item.lstd;
        this.sstd=item.sstd;
     }

    @Override
    public VarLocalLinearTrendItem duplicate() {
        return new VarLocalLinearTrendItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(lscale);
        mapping.add(sscale);
        mapping.add((p, builder) -> {
            double e1 = p.get(0);
            double e2 = p.get(1);
            StateComponent cmp = jdplus.sts.VarLocalLinearTrend.of(lstd, sstd, e1, e2);
            builder.add(name, cmp, VarLocalLinearTrend.defaultLoading());
            return 2;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(lscale, sscale);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double e1 = p.get(0);
        double e2 = p.get(1);
        return jdplus.sts.VarLocalLinearTrend.of(lstd, sstd, e1, e2);
     }

    @Override
    public int parametersCount() {
        return 2;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return VarLocalLinearTrend.defaultLoading();
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
        return !lscale.isFixed() && !lscale.isFixed();
    }
}
