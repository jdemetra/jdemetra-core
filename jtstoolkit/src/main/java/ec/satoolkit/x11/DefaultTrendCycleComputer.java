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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
class DefaultTrendCycleComputer extends DefaultX11Algorithm implements
        ITrendCycleComputer {

    private SymmetricFilter hendersonFilter;

    /**
     *
     * @param x11
     * @param hLength
     */
    DefaultTrendCycleComputer(final int hLength) {
        hendersonFilter = TrendCycleFilterFactory.makeHendersonFilter(hLength);
    }

    /**
     *
     * @param step
     * @param s
     * @param info
     * @return
     */
    @Override
    public TsData doFinalFiltering(X11Step step, TsData s, InformationSet info) {
        int flen = hendersonFilter.getLength();
        IFiltering strategy = new DefaultTrendFilteringStrategy(
                hendersonFilter, new AsymmetricEndPoints(MusgraveFilterFactory.makeFiltersForHenderson(flen,
                context.getFrequency())), flen + "-Henderson");
        if (step == X11Step.D) {
            info.subSet(X11Kernel.D).set(X11Kernel.D12_FILTER, strategy.getDescription());
            info.subSet(X11Kernel.D).set(X11Kernel.D12_TLEN, flen);
        }
        return strategy.process(s, s.getDomain());
    }

    @Override
    public TsData doInitialFiltering(X11Step step, TsData s, InformationSet info) {
        SymmetricFilter trendFilter = TrendCycleFilterFactory.makeTrendFilter(context.getFrequency());
        return new DefaultTrendFilteringStrategy(trendFilter, null).process(s,
                s.getDomain());
    }

    /**
     *
     * @return
     */
    public int getFilterLength() {
        return this.hendersonFilter.getLength();
    }
}
