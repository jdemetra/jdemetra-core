/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Thomas Witthohn
 */
public class ICRatioComputer {

    /**
     * Calculate the I/C-Ratio and write it into the InformationSet
     * <code>info</code> as <code>X11Kernel.D12_IC</code> in the
     * SubInformationSet <code>X11Kernel.D</code>.
     *
     * @param context
     * @param s
     * @param info The InformationSet this method writes into.
     */
    public static void writeICR(X11Context context, TsData s, InformationSet info) {
        int freq = context.getFrequency();

        int filterLength = freq + 1;
        SymmetricFilter trendFilter = TrendCycleFilterFactory.makeHendersonFilter(filterLength);// .defaultHendersonFilterForFrequency(freq);
        IFiltering strategy = new DefaultTrendFilteringStrategy(trendFilter,
                null, filterLength + " terms Henderson moving average");
        TsData sc = strategy.process(s, s.getDomain());
        TsData si = context.op(s, sc);
        int nf = context.getForecastHorizon();
        int nb = context.getBackcastHorizon();
        TsDomain gdom = (nf == 0 && nb == 0) ? null : sc.getDomain().drop(nb, nf);
        double gc = SeriesEvolution.calcAbsMeanVariations(sc, gdom, 1,
                context.isMultiplicative());
        double gi = SeriesEvolution.calcAbsMeanVariations(si, gdom, 1,
                context.isMultiplicative());
        double icr = gi / gc;
        if (freq == 4) {
            icr *= 3.0;
        }

        InformationSet dtables = info.subSet(X11Kernel.D);
        dtables.set(X11Kernel.D12_IC, icr);
    }
}
