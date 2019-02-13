/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.descriptors;

import demetra.descriptors.stats.DiffuseConcentratedLikelihoodDescriptor;
import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TemporalDisaggregationDescriptor {
    
    public final String LIKELIHOOD="likelihood", DISAGG="disagg", EDISAGG="edisagg",
            RES="residuals", FRES="fullresiduals", WNRES="wnresiduals";
    
    final InformationMapping<TemporalDisaggregationResults> MAPPING = new InformationMapping<>(TemporalDisaggregationResults.class);

    static {
        MAPPING.delegate(LIKELIHOOD, DiffuseConcentratedLikelihoodDescriptor.getMapping(), source->source.getConcentratedLikelihood());
        MAPPING.set(DISAGG, TsData.class, source->source.getDisaggregatedSeries());
        MAPPING.set(EDISAGG, TsData.class, source->source.getStdevDisaggregatedSeries());
    }

    public InformationMapping<TemporalDisaggregationResults> getMapping() {
        return MAPPING;
    }
    
}
