/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.msts.ArInterpreter;
import jdplus.msts.ModelItem;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import jdplus.ssf.SsfComponent;
import jdplus.arima.ssf.SsfAr;
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
public class ArItem extends StateItem {

    private final ArInterpreter ar;
    private final VarianceInterpreter v;
    private final int nlags;
    private final boolean zeroinit;

    public ArItem(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, boolean zeroinit) {
        super(name);
        this.nlags = nlags;
        this.ar = new ArInterpreter(name + ".ar", ar, fixedar);
        this.v = new VarianceInterpreter(name + ".var", var, fixedvar, true);
        this.zeroinit = zeroinit;
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
        return SsfAr.stateComponent(par, w, nlags, zeroinit);
    }

    @Override
    public int parametersCount() {
        return 1 + ar.getDomain().getDim();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        } else {
            return SsfAr.loading();
        }
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return Math.max(nlags, ar.getDomain().getDim())+1;
    }

}
