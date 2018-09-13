/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
class AutomaticTrendCycleComputer extends DefaultX11Algorithm implements
        ITrendCycleComputer {

    private double curIC;

    /**
     *
     * @param step
     * @param s
     * @param info
     *
     * @return
     */
    @Override
    public TsData doFinalFiltering(X11Step step, TsData s, InformationSet info) {
        int freq = context.getFrequency();
        if (step == X11Step.B) {
            curIC = freq == 4 ? .001 : 3.5;
        }
        int filterLength = freq + 1;
        SymmetricFilter trendFilter = TrendCycleFilterFactory.makeHendersonFilter(filterLength);// .defaultHendersonFilterForFrequency(freq);
        IFiltering strategy = new DefaultTrendFilteringStrategy(trendFilter,
                                                                null, filterLength + " terms Henderson moving average");
        TsData sc = strategy.process(s, s.getDomain());
        TsData si = op(s, sc);
        int nf = context.getForecastHorizon();
        int nb = context.getBackcastHorizon();
        TsDomain gdom = (nf == 0 && nb == 0) ? null : sc.getDomain().drop(nb, nf);
        double gc = SeriesEvolution.calcAbsMeanVariations(sc, gdom, 1,
                                                          isMultiplicative(), context.getValidDecomposition());
        double gi = SeriesEvolution.calcAbsMeanVariations(si, gdom, 1,
                                                          isMultiplicative(), context.getValidDecomposition());
        double icr = gi / gc;
        if (freq == 4) {
            icr *= 3.0;
        } else if (freq == 2) {
            icr *= 6.0;
        } //CH: Reason?
        filterLength = this.selectFilter(step, icr, freq);
        // double D = 4.0 / (Math.PI * curIC * curIC);
        if (filterLength == trendFilter.getLength()) {
            // just do end processing
            int len = filterLength / 2;
            sc = sc.extend(len, len);
            AsymmetricEndPoints iep = new AsymmetricEndPoints(
                    MusgraveFilterFactory.makeFilters(trendFilter, curIC/*
                     * D
                     */));
            iep.process(new DataBlock(s.internalStorage()),
                        new DataBlock(sc.internalStorage()));
            if (step == X11Step.D) {
                info.subSet(X11Kernel.D).set(X11Kernel.D12_FILTER, strategy.getDescription());
                info.subSet(X11Kernel.D).set(X11Kernel.D12_TLEN, filterLength);
            }
            return sc;
        } else {
            trendFilter = TrendCycleFilterFactory.makeHendersonFilter(filterLength);
            AsymmetricEndPoints iep = new AsymmetricEndPoints(
                    MusgraveFilterFactory.makeFilters(trendFilter, curIC/*
                     * D
                     */));
            strategy = new DefaultTrendFilteringStrategy(trendFilter, iep, filterLength + "-Henderson");
            if (step == X11Step.D) {
                info.subSet(X11Kernel.D).set(X11Kernel.D12_FILTER, strategy.getDescription());
                info.subSet(X11Kernel.D).set(X11Kernel.D12_TLEN, filterLength);
            }
            return strategy.process(s, s.getDomain());
        }
    }

    /**
     * 2 x freq filter without end processing
     *
     * @param step
     * @param s
     * @param info
     */
    @Override
    public TsData doInitialFiltering(X11Step step, TsData s, InformationSet info) {
        SymmetricFilter trendFilter = TrendCycleFilterFactory.makeTrendFilter(context.getFrequency());
        return new DefaultTrendFilteringStrategy(trendFilter, null).process(s,
                                                                            s.getDomain());
    }

    private int selectFilter(X11Step step, double icr, final int freq) {
        if ((step == X11Step.B && icr >= 1) || (icr >= 1 && icr < 3.5)) {
            return freq + 1;
        }
        if (icr < 1) {
            if (freq == 12) {
                curIC = 1;
                return 9;
            } else {
                return 5;
            }
        } else // icr >= 4.5
        {
            curIC = 4.5;
            return freq == 12 ? 23 : 7;
        }
    }
}
