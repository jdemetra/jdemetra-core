/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.tempdisagg.spi;

import demetra.design.Algorithm;
import demetra.design.ServiceDefinition;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpecification;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.processing.ProcResults;

/**
 *
 * @author Jean Palate
 */
@Algorithm
@ServiceDefinition(isSingleton=true)
public interface TemporalDisaggregationAlgorithm {
    
    // Output dictionary
    /**
     * Disaggregated series
     */
    public static final String DISAGGREGATEDSERIES="disaggregatedseries";
    /**
     * Standard error of the disaggregated series
     */
    public static final String STDERRDISAGGREGATEDSERIES="stderrdisaggregatedseries";
    /**
     * Part of the disaggregated series linked to the regression variables (outside constant/trend) 
     */
    public static final String REGEFFECT="regressioneffect";
    /**
     * Part of the disaggregated series linked to the disaggregation method (including constant/trend) 
     */
    public static final String SMOOTHINGEFFECT="smoothingeffect";
    /**
     * Discrepancies between the aggregated series and the aggregated regression variables 
     * (including the constnat an the trend)
     */
    public static final String RESIDUALS="residuals";
    /**
     * Residuals of the model; should be white noises. 
     */
    public static final String WNRESIDUALS="wnresiduals";
    /**
     * Likelihood statistics
     */
    public static final String LIKELIHOOD="likelihood";
    /**
     * Maximum likelihood estimates
     */
    public static final String ML="mlestimates";

    ITemporalDisaggregationResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpecification spec);
   
    ITemporalDisaggregationResults process(TsData aggregatedSeries, TsDomain domain, TemporalDisaggregationSpecification spec);
}
