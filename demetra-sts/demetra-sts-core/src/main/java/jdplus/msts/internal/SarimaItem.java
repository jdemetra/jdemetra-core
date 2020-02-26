/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import jdplus.arima.ArimaModel;
import jdplus.arima.ssf.SsfArima;
import jdplus.msts.MstsMapping;
import jdplus.msts.SarimaInterpreter;
import jdplus.msts.VarianceInterpreter;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import jdplus.ssf.StateComponent;
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;

/**
 *
 * @author palatej
 */
public class SarimaItem extends StateItem {

    private final VarianceInterpreter v;
    private final SarimaInterpreter p;

    public SarimaItem(final String name, int period, int[] orders, int[] seasonal, double[] parameters, boolean fixed, double var, boolean fixedvar) {
        super(name);
        SarimaOrders spec = new SarimaOrders(period);
        spec.setP(orders[0]);
        spec.setD(orders[1]);
        spec.setQ(orders[2]);
        if (seasonal != null) {
            spec.setBp(seasonal[0]);
            spec.setBd(seasonal[1]);
            spec.setBq(seasonal[2]);
        }
        v = new VarianceInterpreter(name + ".var", var, fixedvar, true);
        p = new SarimaInterpreter(name, spec, parameters, fixed);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add(p);
        SarimaOrders spec = p.getDomain().getSpec();
        mapping.add((p, builder) -> {
            double var = p.get(0);
            int np = spec.getParametersCount();
            StateComponent cmp;
            SarimaModel sarima = SarimaModel.builder(spec)
                    .parameters(p.extract(1, np))
                    .build();
            if (var == 1) {
                cmp = SsfArima.of(sarima);
            } else {
                ArimaModel arima = new ArimaModel(sarima.getStationaryAr(), sarima.getNonStationaryAr(), sarima.getMa(), var);
                cmp = SsfArima.of(arima);
            }
            builder.add(name, cmp, null);
            return np + 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(v, p);
    }

    @Override
    public StateComponent build(DoubleSeq x) {
        SarimaOrders spec = p.getDomain().getSpec();
        double var = x.get(0);
        int np = spec.getParametersCount();
        SarimaModel sarima = SarimaModel.builder(spec)
                .parameters(x.extract(1, np))
                .build();
        StateComponent cmp;
        if (var == 1) {
            cmp = SsfArima.of(sarima);
        } else {
            ArimaModel arima = new ArimaModel(sarima.getStationaryAr(), sarima.getNonStationaryAr(), sarima.getMa(), var);
            cmp = SsfArima.of(arima);
        }
        return cmp;
    }

    @Override
    public int parametersCount() {
        SarimaOrders spec = p.getDomain().getSpec();
        return spec.getParametersCount() + 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return SsfArima.defaultLoading();
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        SarimaOrders spec = p.getDomain().getSpec();
        int p = spec.getP()+spec.getD();
        int q=spec.getQ();
        int s=spec.getPeriod();
        if (s>0){
            p+=s*(spec.getBp()+spec.getBd());
            q+=s*spec.getBq();
        }
        return Math.max(p, q + 1);
    }
}
