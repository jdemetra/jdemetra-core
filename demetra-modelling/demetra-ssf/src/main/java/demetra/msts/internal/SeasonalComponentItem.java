/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.msts.IMstsParametersBlock;
import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceParameter;
import demetra.ssf.SsfComponent;
import demetra.sts.SeasonalComponent;
import demetra.sts.SeasonalModel;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author palatej
 */
public class SeasonalComponentItem extends AbstractModelItem {

    private final SeasonalModel model;
    private final int period;
    private final VarianceParameter v;

    public SeasonalComponentItem(String name, String smodel, int period, double seasvar, boolean fixed) {
        super(name);
        this.model = SeasonalModel.valueOf(smodel);
        this.period = period;
        this.v = new VarianceParameter(name + ".var", seasvar, fixed, true);
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
    public List<IMstsParametersBlock> parameters() {
        return Collections.singletonList(v);
    }

}