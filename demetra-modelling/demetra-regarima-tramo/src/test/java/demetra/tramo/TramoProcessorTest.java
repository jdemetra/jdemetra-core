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
package demetra.tramo;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.regarima.regular.RegArimaModelling;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TramoProcessorTest {
    
    private final double[] data, datamissing;
    
    public TramoProcessorTest() {
        data=Data.PROD.clone();
        datamissing=Data.PROD.clone();
        datamissing[2]=Double.NaN;
        datamissing[100]=Double.NaN;
        datamissing[101]=Double.NaN;
        datamissing[102]=Double.NaN;
    }

    @Test
    public void testProdMissing() {
        TramoProcessor processor=TramoProcessor.of(TramoSpec.TR5, null);
        TsPeriod start=TsPeriod.monthly(1967,1);
        TsData s=TsData.of(start, DoubleSequence.ofInternal(datamissing));
        demetra.regarima.regular.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("JD3 with missing");
        System.out.println(rslt.getEstimation().getStatistics().getLogLikelihood());
    }
    
    @Test
    public void testProdLegacyMissing() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR5.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, datamissing, true);
        PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy with missing");
        System.out.println(rslt.estimation.getStatistics().logLikelihood);
    }
    
    @Test
    public void testProd() {
        TramoProcessor processor=TramoProcessor.of(TramoSpec.TRfull, null);
        TsPeriod start=TsPeriod.monthly(1967,1);
        TsData s=TsData.of(start, DoubleSequence.ofInternal(data));
        demetra.regarima.regular.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("JD3");
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
    }
    
    @Test
    public void testProdLegacy() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy");
        System.out.println(rslt.estimation.getStatistics().adjustedLogLikelihood);
    }
    
//    @Test
    public void testProdWald() {
        TramoSpec nspec=new TramoSpec(TramoSpec.TRfull);
        nspec.getRegression().getCalendar().getTradingDays().setAutomaticMethod(TradingDaysSpec.AutoMethod.WaldTest);
        TramoProcessor processor=TramoProcessor.of(nspec, null);
        TsPeriod start=TsPeriod.monthly(1967,1);
        TsData s=TsData.of(start, DoubleSequence.ofInternal(data));
        processor.process(s, null);
    }
    
//    @Test
    public void testProdWaldLegacy() {
        TramoSpecification nspec = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.clone();
        nspec.getRegression().getCalendar().getTradingDays().setAutomaticMethod(ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec.AutoMethod.WaldTest);
        IPreprocessor processor = nspec.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        processor.process(s, null);
    }
}
