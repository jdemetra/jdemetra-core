/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceInterpreter;
import jdplus.ssf.SsfComponent;
import demetra.sts.LocalLevel;
import java.util.Collections;
import java.util.List;
import demetra.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class LocalLevelItem extends AbstractModelItem {

    public final VarianceInterpreter v;
    public final double initial;

    public LocalLevelItem(String name, final double lvar, final boolean fixed, final double initial) {
        super(name);
        this.initial = initial;
        this.v = new VarianceInterpreter(name + ".var", lvar, fixed, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            SsfComponent cmp = LocalLevel.of(e, initial);
            builder.add(name, cmp);
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

}
