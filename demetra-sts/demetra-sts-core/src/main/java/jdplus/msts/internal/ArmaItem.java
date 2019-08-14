/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.arima.ArimaModel;
import jdplus.arima.ssf.SsfArima;
import jdplus.maths.linearfilters.BackFilter;
import jdplus.maths.polynomials.Polynomial;
import jdplus.msts.ModelItem;
import jdplus.msts.MstsMapping;
import jdplus.msts.StablePolynomialInterpreter;
import jdplus.msts.VarianceInterpreter;
import jdplus.ssf.StateComponent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class ArmaItem extends StateItem{

    private final StablePolynomialInterpreter par, pma;
    private final VarianceInterpreter v;

    public ArmaItem(final String name, double[] ar, double[] ma, double var, boolean fixed) {
        super(name);
        int nar = ar == null ? 0 : ar.length, nma = ma == null ? 0 : ma.length;
        if (nar > 0) {
            par = new StablePolynomialInterpreter(name + ".ar", ar, fixed);
        } else {
            par = null;
        }
        if (nma > 0) {
            pma = new StablePolynomialInterpreter(name + ".ma", ma, fixed);
        } else {
            pma = null;
        }
        v = new VarianceInterpreter(name + ".var", var, true, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (par != null) {
            mapping.add(par);
        }
        if (pma != null) {
            mapping.add(pma);
        }
        mapping.add(v);
        mapping.add((p, builder) -> {
            BackFilter bar = BackFilter.ONE, bma = BackFilter.ONE;
            int pos = 0;
            if (par != null) {
                int nar=par.getDomain().getDim();
                Polynomial ar = Polynomial.valueOf(1, p.extract(0, nar).toArray());
                bar = new BackFilter(ar);
                pos += nar;
            }
            if (pma != null) {
                int nma=pma.getDomain().getDim();
                Polynomial ma = Polynomial.valueOf(1, p.extract(0, nma).toArray());
                bma = new BackFilter(ma);
                pos += nma;
            }
            double n = p.get(pos++);
            ArimaModel arima = new ArimaModel(bar, BackFilter.ONE, bma, n);
            StateComponent cmp = SsfArima.componentOf(arima);
            builder.add(name, cmp, null);
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(par, pma, v);
    }
}
