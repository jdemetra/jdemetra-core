/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.arima.ArimaModel;
import jdplus.arima.ssf.SsfArima;
import jdplus.msts.ModelItem;
import jdplus.msts.MstsMapping;
import jdplus.msts.SarimaInterpreter;
import jdplus.msts.VarianceInterpreter;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import jdplus.ssf.StateComponent;
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class SarimaItem extends AbstractModelItem {

    private final VarianceInterpreter v;
    private final SarimaInterpreter p;

    public SarimaItem(final String name, int period, int[] orders, int[] seasonal, double[] parameters, boolean fixed, double var, boolean fixedvar) {
        super(name);
        SarimaSpecification spec = new SarimaSpecification(period);
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
        SarimaSpecification spec = p.getDomain().getSpec();
        mapping.add((p, builder) -> {
            double var = p.get(0);
            int np = spec.getParametersCount();
            StateComponent cmp;
            SarimaModel sarima = SarimaModel.builder(spec)
                    .parameters(p.extract(1, np))
                    .build();
            if (var == 1) {
                cmp = SsfArima.componentOf(sarima);
            } else {
                ArimaModel arima = new ArimaModel(sarima.getStationaryAr(), sarima.getNonStationaryAr(), sarima.getMa(), var);
                cmp = SsfArima.componentOf(arima);
            }
            builder.add(name, cmp, null);
            return np + 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(v, p);
    }

}
