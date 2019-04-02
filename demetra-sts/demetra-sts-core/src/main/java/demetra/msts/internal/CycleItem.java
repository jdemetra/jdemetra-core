/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.msts.BoundedParameterInterpreter;
import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceInterpreter;
import demetra.sts.CyclicalComponent;
import java.util.Arrays;
import java.util.List;
import demetra.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class CycleItem extends AbstractModelItem {

    private final BoundedParameterInterpreter factor, period;
    private final VarianceInterpreter v;

    public CycleItem(String name, double dumpingFactor, double cyclicalPeriod, boolean fixedcycle, double cvar, boolean fixedvar) {
        super(name);
        factor = BoundedParameterInterpreter.builder()
                .name(name + ".factor")
                .value(cvar, fixedcycle)
                .bounds(0, 1, true)
                .build();
        period = BoundedParameterInterpreter.builder()
                .name(name + ".period")
                .value(cyclicalPeriod, fixedcycle)
                .bounds(2, Double.MAX_VALUE, false)
                .build();
        v = new VarianceInterpreter(name + ".var", cvar, fixedvar, true);
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
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(factor, period, v);
    }

}
