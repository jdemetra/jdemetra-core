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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequenciesMonitorTest {

    EstimateSpec.Method method = EstimateSpec.Method.Cholesky;

    public MixedFrequenciesMonitorTest() {
    }

    //@Test
    public void demoFlows() {
        MixedFrequenciesSpecification spec = new MixedFrequenciesSpecification();
        //spec.getArima().setP(3);
        //spec.getBasic().setLog(true);
        //spec.getRegression().getEaster().setOption(EasterSpec.Type.IncludeEaster);
        //spec.getRegression().getTradingDays().setTradingDaysType(TradingDaysType.TradingDays);
        //spec.getRegression().getTradingDays().setLeapYear(true);
        MixedFrequenciesMonitor monitor = new MixedFrequenciesMonitor();
        spec.getEstimate().setMethod(method);
        System.out.println("FLOWS");
        for (int ny = 4; ny < 5; ++ny) {
            TsData s = data.Data.X;
            TsData Q = s.drop(0, s.getLength() - ny * 12).changeFrequency(TsFrequency.Quarterly, TsAggregationType.Sum, true);
            TsData M = s.drop(ny * 12, 0);

            boolean rslt = monitor.process(Q, M, spec);
            System.out.println(s);
            System.out.println(monitor.getInterpolatedSeries());
            System.out.println(monitor.getInterpolationErrors());
            System.out.println(monitor.getArima());
            System.out.println(monitor.getLikelihood().getLogLikelihood());
        }
    }

    //@Test
    public void demoStocks() {
        MixedFrequenciesSpecification spec = new MixedFrequenciesSpecification();
        //spec.getArima().setP(3);
        spec.getBasic().setDataType(DataType.Stock);
        //spec.getBasic().setLog(true);
        //spec.getRegression().getEaster().setOption(EasterSpec.Type.IncludeEaster);
        //spec.getRegression().getTradingDays().setTradingDaysType(TradingDaysType.TradingDays);
        //spec.getRegression().getTradingDays().setLeapYear(true);
        MixedFrequenciesMonitor monitor = new MixedFrequenciesMonitor();
        spec.getEstimate().setMethod(method);
        System.out.println("STOCKS");
        for (int ny = 4; ny < 5; ++ny) {
            TsData s = data.Data.X;
            TsData Q = s.drop(0, s.getLength() - ny * 12).changeFrequency(TsFrequency.Quarterly, TsAggregationType.Last, true);
            TsData M = s.drop(ny * 12, 0);

            boolean rslt = monitor.process(Q, M, spec);
            System.out.println(monitor.getInterpolatedSeries());
            System.out.println(monitor.getInterpolationErrors());
            System.out.println(monitor.getArima());
            System.out.println(monitor.getLikelihood().getLogLikelihood());
        }
    }

    //@Test
    public void demoStressTest() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        //spec.setP(3);
        //spec.setBP(1);
        //spec.setD(1);
        //spec.setBD(1);
        SarimaModel model = new SarimaModel(spec);
        MixedFrequenciesSpecification mspec = new MixedFrequenciesSpecification();
        mspec.getArima().setArima(model);
        mspec.getArima().setParameterType(ParameterType.Fixed);
        //spec.getBasic().setLog(true);
        mspec.getRegression().getEaster().setOption(EasterSpec.Type.IncludeEaster);
        mspec.getRegression().getTradingDays().setTradingDaysType(TradingDaysType.TradingDays);
        mspec.getRegression().getTradingDays().setLeapYear(true);
        MixedFrequenciesMonitor monitor = new MixedFrequenciesMonitor();
        mspec.getEstimate().setMethod(method);
        TsData P = new TsData(TsFrequency.Monthly, 1980, 0, 360);
        P.randomAirline();

        int ny = 15, K = 1000;
        for (int l = 0; l <= 12; ++l) {
            long l0 = System.currentTimeMillis();
            TsData s = P.drop(l * 24, 0);
            ny = s.getLength() / 24;
            TsData sq = s.changeFrequency(TsFrequency.Quarterly, TsAggregationType.Sum, true);
            TsData Q = s.drop(0, s.getLength() - ny * 12).changeFrequency(TsFrequency.Quarterly, TsAggregationType.Sum, true);
            TsData M = s.drop(ny * 12, 0);

            boolean rslt = monitor.process(Q, M, mspec);
            for (int i = 0; i < K; ++i) {
                monitor.getFunction().ssqEvaluate(model.getParameters());
            }
            long l1 = System.currentTimeMillis();
            System.out.println(l1 - l0);
        }
    }

}
