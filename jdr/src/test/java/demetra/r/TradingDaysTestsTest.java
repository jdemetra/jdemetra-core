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
package demetra.r;

import demetra.data.Data;
import jdplus.data.DataBlockStorage;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.ssf.univariate.SsfData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsData;
import jdplus.timeseries.simplets.TsDataToolkit;
import static jdplus.timeseries.simplets.TsDataToolkit.log;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.ssf.SsfUcarima;
import org.junit.Test;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysTestsTest {

    public TradingDaysTestsTest() {
    }

    @Test
    public void testTD() {
        TsData s = log(TsData.of(TsPeriod.monthly(1992, 1), Doubles.of(Data.ABS_RETAIL)));
        TimeVaryingRegression.Results regarima = TimeVaryingRegression.regarima(s, "TD7", "Default", 1e-7);
        TsData rtd = regarima.getData("tdeffect", TsData.class);

        UcarimaModel ucm = ucmAirline(regarima.getArima());
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s.getValues());
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        int[] pos = ssf.componentsPosition();
        TsData i1 = TsData.of(s.getStart(), Doubles.of(ds.item(pos[2])));
        
        data = new SsfData(TsDataToolkit.subtract(s, rtd).getValues());
        ds = DkToolkit.fastSmooth(ssf, data);
        TsData i2 = TsData.of(s.getStart(), Doubles.of(ds.item(pos[2])));
       
//        System.out.println(TradingDaysTests.ftest(i1, true, 0));
//        System.out.println(TradingDaysTests.ftest(i1, false, 0));
//        System.out.println(TradingDaysTests.ftest(i1, true, 8));
//        System.out.println(TradingDaysTests.ftest(i1, false, 8));
//        System.out.println(TradingDaysTests.ftest(i2, true, 0));
//        System.out.println(TradingDaysTests.ftest(i2, false, 0));
//        System.out.println(TradingDaysTests.ftest(i2, true, 8));
//        System.out.println(TradingDaysTests.ftest(i2, false, 8));
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        SarimaSpecification spec = SarimaSpecification.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, th)
                .btheta(1, bth)
                .build();

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

    public static UcarimaModel ucmAirline(SarimaModel sarima) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

}
