/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tempdisagg.univariate.internal;

import demetra.tempdisagg.spi.ITemporalDisaggregationResults;
import demetra.tempdisagg.spi.TemporalDisaggregationAlgorithm;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpecification;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(service = TemporalDisaggregationAlgorithm.class)
public class TemporalDisaggregationFactory implements TemporalDisaggregationAlgorithm {

    @Override
    public ITemporalDisaggregationResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpecification spec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ITemporalDisaggregationResults process(TsData aggregatedSeries, TsDomain domain, TemporalDisaggregationSpecification spec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
