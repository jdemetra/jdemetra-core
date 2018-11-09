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
import demetra.ssf.models.LocalLevel;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author palatej
 */
public class LocalLevelItem extends AbstractModelItem {

    public final VarianceParameter v;
    public final double initial;

    public LocalLevelItem(String name, final double lvar, final boolean fixed, final double initial) {
        super(name);
        this.initial = initial;
        this.v = new VarianceParameter(name + ".var", lvar, fixed, true);
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
    public List<IMstsParametersBlock> parameters() {
        return Collections.singletonList(v);
    }

}
