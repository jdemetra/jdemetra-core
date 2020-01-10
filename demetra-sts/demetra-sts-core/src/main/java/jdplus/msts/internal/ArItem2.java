/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.msts.ArInterpreter;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import jdplus.arima.ssf.SsfAr2;
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class ArItem2 extends StateItem {

    private final ArInterpreter ar;
    private final VarianceInterpreter v;
    private final int nlags, nfcasts;

    public ArItem2(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, int nfcasts) {
        super(name);
        this.nlags = nlags;
        this.nfcasts = nfcasts;
        this.ar = new ArInterpreter(name + ".ar", ar, fixedar);
        this.v = new VarianceInterpreter(name + ".var", var, fixedvar, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(ar);
        mapping.add(v);
        mapping.add((p, builder) -> {
            int n = ar.getDomain().getDim();
            double[] par = p.extract(0, n).toArray();
            double w = p.get(n);
            StateComponent cmp = SsfAr2.of(par, w, nlags, nfcasts);
            builder.add(name, cmp, SsfAr2.defaultLoading(nlags));
            return n + 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(ar, v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        int n = ar.getDomain().getDim();
        double[] par = p.extract(0, n).toArray();
        double w = p.get(n);
        return SsfAr2.of(par, w, nlags, nfcasts);
    }

    @Override
    public int parametersCount() {
        return 1 + ar.getDomain().getDim();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return SsfAr2.defaultLoading(nlags);
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        int n = ar.getDomain().getDim();
        if (nfcasts >= n) {
            n = nfcasts+1;
        }
        return n + nlags;
    }
}
