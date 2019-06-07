/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import jdplus.arima.ArimaModel;
import demetra.arima.ssf.SsfArima;
import jdplus.maths.linearfilters.BackFilter;
import jdplus.maths.polynomials.Polynomial;
import demetra.msts.ModelItem;
import demetra.msts.MstsMapping;
import demetra.msts.StablePolynomialInterpreter;
import demetra.msts.VarianceInterpreter;
import demetra.ssf.StateComponent;
import java.util.ArrayList;
import java.util.List;
import demetra.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class ArimaItem extends AbstractModelItem {

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
            StateComponent cmp = SsfArima.componentOf(arima);
            builder.add(name, cmp, null);
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

}
