/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
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
        int ny_all = domain.getLength() / domain.getFrequency().intValue();
        int nyr_all = domain.getLength() % domain.getFrequency().intValue() == 0 ? ny_all : ny_all + 1;

        for (int p = 0; p < tempOptions.length; p++) {
            int nf = 0;
            if (tempOptions[p] != null && tempOptions[p] != SeasonalFilterOption.Stable && tempOptions[p] != SeasonalFilterOption.Msr && tempOptions[p] != SeasonalFilterOption.X11Default) {
                DefaultSeasonalFilteringStrategy defaultFilteringStrategy = SeasonalFilterFactory.getDefaultFilteringStrategy(tempOptions[p]);
                SymmetricFilter sf = defaultFilteringStrategy.filter;
                nf = sf.getUpperBound();
            }
            if (tempOptions[p] != null && ny_all >= 5 && (nf < 8 || nyr_all >= 20)) {
            } else {
                tempOptions[p] = SeasonalFilterOption.Stable;
            }
        }

        IEndPointsProcessor iep = new MixedEndPoints(tempOptions, f.getLength() / 2, s.getStart().getPosition());
        IFiltering n = new DefaultTrendFilteringStrategy(f, iep);
        TsData tmp = n.process(s, s.getDomain());

        return tmp;
    }

}
