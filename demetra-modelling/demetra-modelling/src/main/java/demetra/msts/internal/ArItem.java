/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.msts.ArParameters;
import demetra.msts.IMstsParametersBlock;
import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceParameter;
import demetra.ssf.SsfComponent;
import demetra.ssf.models.SsfAr;
import demetra.ssf.models.SsfAr2;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author palatej
 */
public class ArItem extends AbstractModelItem {

    private final ArParameters ar;
    private final VarianceParameter v;
    private final int nlags;
    private final boolean zeroinit;

    public ArItem(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, boolean zeroinit) {
        super(name);
        this.nlags = nlags;
        this.ar = new ArParameters(name + ".ar", ar, fixedar);
        this.v = new VarianceParameter(name + ".var", var, fixedvar, true);
        this.zeroinit = zeroinit;
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(ar);
        mapping.add(v);
        mapping.add((p, builder) -> {
            int n = ar.getDomain().getDim();
            double[] par = p.extract(0, n).toArray();
            double w = p.get(n);
            SsfComponent cmp = SsfAr.of(par, w, nlags, zeroinit);
            builder.add(name, cmp);
            return n + 1;
        });
    }

    @Override
    public List<IMstsParametersBlock> parameters() {
        return Arrays.asList(ar, v);
    }

}
