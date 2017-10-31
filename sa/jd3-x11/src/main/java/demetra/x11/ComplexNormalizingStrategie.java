/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.design.Development;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDomain;

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
        IEndPointsProcessor iep = new MixedEndPoints(options, f.getLength() / 2, s.getStart().getPosition());
        IFiltering n = new DefaultTrendFilteringStrategy(f, iep);
        TsData tmp = n.process(s, s.getDomain());
    //    domain.getStart();

        
        return tmp;
    }

}
