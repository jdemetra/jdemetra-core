/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import demetra.data.DoubleSeq;
import jdplus.arima.ArimaModel;
import jdplus.arima.ssf.SsfArima;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.msts.MstsMapping;
import jdplus.msts.StablePolynomialInterpreter;
import jdplus.msts.VarianceInterpreter;
import jdplus.ssf.StateComponent;
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;

/**
 *
 * @author palatej
 */
public class ArmaItem extends StateItem {

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
                int nar = par.getDomain().getDim();
                Polynomial ar = Polynomial.valueOf(1, p.extract(0, nar).toArray());
                bar = new BackFilter(ar);
                pos += nar;
            }
            if (pma != null) {
                int nma = pma.getDomain().getDim();
                Polynomial ma = Polynomial.valueOf(1, p.extract(0, nma).toArray());
                bma = new BackFilter(ma);
                pos += nma;
            }
            double n = p.get(pos++);
            ArimaModel arima = new ArimaModel(bar, BackFilter.ONE, bma, n);
            StateComponent cmp = SsfArima.of(arima);
            builder.add(name, cmp, SsfArima.defaultLoading());
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(par, pma, v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        BackFilter bar = BackFilter.ONE, bma = BackFilter.ONE;
        int pos = 0;
        if (par != null) {
            int nar = par.getDomain().getDim();
            Polynomial ar = Polynomial.valueOf(1, p.extract(0, nar).toArray());
            bar = new BackFilter(ar);
            pos += nar;
        }
        if (pma != null) {
            int nma = pma.getDomain().getDim();
            Polynomial ma = Polynomial.valueOf(1, p.extract(0, nma).toArray());
            bma = new BackFilter(ma);
            pos += nma;
        }
        double n = p.get(pos++);
        ArimaModel arima = new ArimaModel(bar, BackFilter.ONE, bma, n);
        return SsfArima.of(arima);
    }

    @Override
    public int parametersCount() {
        int n = 1;
        if (par != null) {
            int nar = par.getDomain().getDim();
            n += nar;
        }
        if (pma != null) {
            int nma = pma.getDomain().getDim();
            n += nma;
        }
        return n;
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
        int p = 0;
        if (par != null) {
            p = par.getDomain().getDim();
        }
        int q = 0;
        if (pma != null) {
            q = pma.getDomain().getDim();
        }
        return Math.max(p, q + 1);
    }
}
