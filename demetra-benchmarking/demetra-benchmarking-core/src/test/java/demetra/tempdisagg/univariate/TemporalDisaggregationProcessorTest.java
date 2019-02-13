/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tempdisagg.univariate;

import demetra.data.AggregationType;
import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.data.ParameterSpec;
import demetra.ssf.SsfAlgorithm;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TemporalDisaggregationProcessorTest {

    public TemporalDisaggregationProcessorTest() {
    }

    @Test
    public void testChowLin() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
//                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec);
//        System.out.println(rslt.getDisaggregatedSeries());
//        System.out.println(rslt.getStdevDisaggregatedSeries());
    }

}
