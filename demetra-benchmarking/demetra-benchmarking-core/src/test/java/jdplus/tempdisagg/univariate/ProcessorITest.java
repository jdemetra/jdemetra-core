/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tempdisagg.univariate;

import demetra.data.AggregationType;
import demetra.data.Data;
import demetra.data.Doubles;
import demetra.data.ParameterSpec;
import demetra.tempdisagg.univariate.TemporalDisaggregationIResults;
import demetra.tempdisagg.univariate.TemporalDisaggregationISpec;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class ProcessorITest {
    
    public ProcessorITest() {
    }

    @Test
    public void testQ() {
        TemporalDisaggregationISpec speci = TemporalDisaggregationISpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .truncatedRho(-1)
                .constant(true)
                .build();
        TsData y = TsData.of(TsPeriod.yearly(1978), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        
        TemporalDisaggregationIResults rslti = new ProcessorI().process(y, q, speci);
//        System.out.println(rslti.getDisaggregatedSeries());
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .constant(true)
                .build();
        
        TemporalDisaggregationResults rslt = new TemporalDisaggregationProcessor().process(y, new TsData[]{q}, spec);
//        System.out.println(rslt.getDisaggregatedSeries());
    }
    
    @Test
    public void testQ2() {
        TemporalDisaggregationISpec speci = TemporalDisaggregationISpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .constant(true)
                .truncatedRho(.5)
                .parameter(ParameterSpec.initial(.6))
                .build();
        TsData y = TsData.of(TsPeriod.yearly(1978), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        
        TemporalDisaggregationIResults rslti = new ProcessorI().process(y, q, speci);
//        System.out.println(rslti.getDisaggregatedSeries());
    }
    
    @Test
    public void testQ3() {
        TemporalDisaggregationISpec speci = TemporalDisaggregationISpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .constant(true)
                .parameter(ParameterSpec.fixed(.6))
                .build();
        TsData y = TsData.of(TsPeriod.yearly(1978), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        
        TemporalDisaggregationIResults rslti = new ProcessorI().process(y, q, speci);
//        System.out.println(rslti.getDisaggregatedSeries());
    }
}
