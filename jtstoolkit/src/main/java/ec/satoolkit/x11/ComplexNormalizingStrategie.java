/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Exploratory)
public class ComplexNormalizingStrategie implements INormalizing {

    /*  private IEndPointsProcessor[] endPointsProcessors;*/
    private SeasonalFilterOption[] options;

    public ComplexNormalizingStrategie(SeasonalFilterOption[] options) {
        this.options = options;
    }

    @Override
    public String getDescription() {
        return "Mixed Normalizer";
    }

    @Override
    public TsData process(TsData s, TsDomain domain, int freq) {
        //  TsDomain rdomain = domain == null ? s.getDomain() : domain;
        SymmetricFilter f = TrendCycleFilterFactory.makeTrendFilter(freq);
        //check if it is nessecarry to replace a filter with a stable because the time series is to short
        SeasonalFilterOption[] tempOptions = options.clone();
        PeriodIterator pin = new PeriodIterator(s, domain);
        int p = 0;
        while (pin.hasMoreElements()) {
            DataBlock bin = pin.nextElement().data;
            int nf = 0, len = bin.getLength();
            if (tempOptions[p] != null && tempOptions[p] != SeasonalFilterOption.Stable && tempOptions[p] != SeasonalFilterOption.Msr && tempOptions[p] != SeasonalFilterOption.X11Default) {
                DefaultSeasonalFilteringStrategy defaultFilteringStrategy = SeasonalFilterFactory.getDefaultFilteringStrategy(tempOptions[p]);
                SymmetricFilter sf = defaultFilteringStrategy.filter;
                nf = sf.getUpperBound();
            }
            if (tempOptions[p] != null && 2 * nf < len && (nf < 8 || len >= 20)) {
            } else {
                tempOptions[p] = SeasonalFilterOption.Stable;
            }
            ++p;
        }

        IEndPointsProcessor iep = new MixedEndPoints(tempOptions, f.getLength() / 2, s.getStart().getPosition());
        IFiltering n = new DefaultTrendFilteringStrategy(f, iep);
        TsData tmp = n.process(s, s.getDomain());
        //    domain.getStart();

        return tmp;
    }

}
