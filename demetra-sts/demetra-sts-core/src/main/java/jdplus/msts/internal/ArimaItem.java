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
import java.util.ArrayList;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;

/**
 *
 * @author palatej
 */
public class ArimaItem extends StateItem {

    private final StablePolynomialInterpreter par, pma;
    private final VarianceInterpreter v;
    private final BackFilter bdiff;

    public ArimaItem(String name, double[] ar, boolean fixedar, double[] diff, double[] ma, boolean fixedma, double var, boolean fixedvar) {
        super(name);
        if (ar != null) {
            par = new StablePolynomialInterpreter(name + ".ar", ar, fixedar);
        } else {
            par = null;
        }
        if (ma != null) {
            pma = new StablePolynomialInterpreter(name + ".ma", ma, fixedma);
        } else {
            pma = null;
        }
        v = new VarianceInterpreter(name + ".var", var, fixedvar, true);
        if (diff != null) {
            Polynomial pdiff = Polynomial.valueOf(1, diff);
            bdiff = new BackFilter(pdiff);
        } else {
            bdiff = BackFilter.ONE;
        }
    }
    
    private ArimaItem(ArimaItem item){
        super(item.name);
        this.par=item.par == null ? null : item.par.duplicate();
        this.pma=item.pma == null ? null : item.pma.duplicate();
        this.v=item.v.duplicate();
        this.bdiff=item.bdiff;
    }
    
    @Override
    public ArimaItem duplicate(){
        return new ArimaItem(this);
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
            double var = p.get(pos++);
            ArimaModel arima = new ArimaModel(bar, bdiff, bma, var);
            StateComponent cmp = SsfArima.of(arima);
            builder.add(name, cmp, SsfArima.defaultLoading());
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        List<ParameterInterpreter> list = new ArrayList<>();
        if (par != null) {
            list.add(par);
        }
        if (pma != null) {
            list.add(pma);
        }
        list.add(v);
        return list;
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
        double var = p.get(pos++);
        ArimaModel arima = new ArimaModel(bar, bdiff, bma, var);
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
        if (bdiff != null) {
            p += bdiff.getDegree();
        }
        int q = 0;
        if (pma != null) {
            q = pma.getDomain().getDim();
        }
        return Math.max(p, q + 1);
    }
}
