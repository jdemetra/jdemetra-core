/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.msts.BoundedParameter;
import demetra.msts.IMstsParametersBlock;
import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceParameter;
import demetra.sts.CyclicalComponent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author palatej
 */
public class CycleItem extends AbstractModelItem {

    private final BoundedParameter factor, period;
    private final VarianceParameter v;

    public CycleItem(String name, double dumpingFactor, double cyclicalPeriod, boolean fixedcycle, double cvar, boolean fixedvar) {
        super(name);
        factor = BoundedParameter.builder()
                .name(name + ".factor")
                .value(cvar, fixedcycle)
                .bounds(0, 1, true)
                .build();
        period = BoundedParameter.builder()
                .name(name + ".period")
                .value(cyclicalPeriod, fixedcycle)
                .bounds(2, Double.MAX_VALUE, false)
                .build();
        v = new VarianceParameter(name + ".var", cvar, fixedvar, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(factor);
        mapping.add(period);
        mapping.add(v);
        mapping.add((p, builder) -> {
            double f = p.get(0), l = p.get(1), var = p.get(2);
            builder.add(name, CyclicalComponent.of(f, l, var));
            return 3;
        });
    }

    @Override
    public List<IMstsParametersBlock> parameters() {
        return Arrays.asList(factor, period, v);
    }

}
