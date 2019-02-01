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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author palatej
 */
public class LocalLinearTrendItem extends AbstractModelItem {

    public final VarianceParameter lv, sv;

    public LocalLinearTrendItem(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
        super(name);
        lv = new VarianceParameter(name + ".lvar", lvar, lfixed, true);
        sv = new VarianceParameter(name + ".svar", svar, sfixed, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(lv);
        mapping.add(sv);
        mapping.add((p, builder) -> {
            double e1 = p.get(0);
            double e2 = p.get(1);
            SsfComponent cmp = demetra.sts.LocalLinearTrend.of(e1, e2);
            builder.add(name, cmp);
            return 2;
        });
    }

    @Override
    public List<IMstsParametersBlock> parameters() {
        return Arrays.asList(lv, sv);
    }

}
