/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import jdplus.data.DataBlock;
import jdplus.maths.linearfilters.SymmetricFilter;
import demetra.x11.SeriesEvolution;
import demetra.x11.X11Context;
import static demetra.x11.X11Kernel.table;
import demetra.data.DoubleSeq;

/**
 *
 * @author Thomas Witthohn
 */
public class AutomaticHenderson {

    public static double calcICR(X11Context context, DoubleSeq s) {
        int freq = context.getPeriod();
        int filterLength = freq + 1;
        SymmetricFilter trendFilter = context.trendFilter(filterLength);

        int ndrop = filterLength / 2;
        double[] x = table(s.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        trendFilter.apply(s, out);

        DoubleSeq sc = out;
        DoubleSeq si = context.remove(s.extract(ndrop, sc.length()), sc);
        int nf = context.getForecastHorizon();
        int nb = context.getBackcastHorizon();
        sc = sc.drop(nb, nf);
        si = si.drop(nb, nf);
        double gc = SeriesEvolution.calcAbsMeanVariation(sc, 1, context.isMultiplicative());
        double gi = SeriesEvolution.calcAbsMeanVariation(si, 1, context.isMultiplicative());
        double icr = gi / gc;
        if (freq == 4) {
            icr *= 3.0;
        } else if (freq == 2) {
            icr *= 6.0;
        }
        return icr;

    }

    public static int selectFilter(double icr, final int freq) {
        if (freq == 2) {
            return 5;
        }
        if (icr >= 1 && icr < 3.5) {
            return freq + 1;
        }
        if (icr < 1) {
            if (freq == 12) {
                return 9;
            } else {
                return 5;
            }
        } else {
            return freq == 12 ? 23 : 7;
        }
    }
}
