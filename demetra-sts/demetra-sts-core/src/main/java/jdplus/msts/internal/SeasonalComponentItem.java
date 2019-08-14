/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.ModelItem;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import jdplus.ssf.SsfComponent;
import jdplus.sts.SeasonalComponent;
import demetra.sts.SeasonalModel;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class SeasonalComponentItem extends StateItem {

    private final SeasonalModel model;
    private final int period;
    private final VarianceInterpreter v;

    public SeasonalComponentItem(String name, String smodel, int period, double seasvar, boolean fixed) {
        super(name);
        this.model = SeasonalModel.valueOf(smodel);
        this.period = period;
        this.v = new VarianceInterpreter(name + ".var", seasvar, fixed, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            SsfComponent cmp = SeasonalComponent.of(model, period, e);
            builder.add(name, cmp);
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

}
