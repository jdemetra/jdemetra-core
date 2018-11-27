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
public class ArItem2 extends AbstractModelItem {

    private final ArParameters ar;
    private final VarianceParameter v;
    private final int nlags, nfcasts;

    public ArItem2(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, int nfcasts) {
        super(name);
        this.nlags = nlags;
        this.nfcasts = nfcasts;
        this.ar = new ArParameters(name + ".ar", ar, fixedar);
        this.v = new VarianceParameter(name + ".var", var, fixedvar, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(ar);
        mapping.add(v);
        mapping.add((p, builder) -> {
            int n = ar.getDomain().getDim();
            double[] par = p.extract(0, n).toArray();
            double w = p.get(n);
            SsfComponent cmp = SsfAr2.of(par, w, nlags, nfcasts);
            builder.add(name, cmp);
            return n + 1;
        });
    }

    @Override
    public List<IMstsParametersBlock> parameters() {
        return Arrays.asList(ar, v);
    }

}
