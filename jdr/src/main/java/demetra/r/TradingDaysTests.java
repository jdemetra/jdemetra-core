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

import demetra.data.DataBlock;
import demetra.information.InformationMapping;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.maths.matrices.Matrix;
import demetra.processing.IProcResults;
import demetra.stats.TestResult;
import demetra.stats.mapping.TestInfo;
import demetra.stats.tests.StatisticalTest;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.calendar.DayClustering;
import demetra.timeseries.calendar.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariables;
import demetra.timeseries.regression.RegressionUtility;
import demetra.timeseries.simplets.TsData;
import static demetra.timeseries.simplets.TsDataToolkit.delta;
import static demetra.timeseries.simplets.TsDataToolkit.drop;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TradingDaysTests {


    public TestResult ftest(TsData s, boolean ar, int ny) {
        int ifreq = TsUtility.periodFromTsUnit(s.getTsUnit());

        if (ar) {
            if (ny != 0) {
                s = drop(s, Math.max(0, s.length() - ifreq * ny - 1), 0);
            }
            return processAr(s);
        } else {
            s = delta(s, 1);
            if (ny != 0) {
                s = drop(s, Math.max(0, s.length() - ifreq * ny), 0);
            }
            return process(s);
        }

    }

    private TestResult process(TsData s) {
        try {
            DataBlock y=DataBlock.of(s.values());
            y.sub(y.average());
            GenericTradingDaysVariables var=new GenericTradingDaysVariables(GenericTradingDays.contrasts(DayClustering.TD7));
            Matrix td = RegressionUtility.data(Collections.singletonList(var), s.domain());
            LinearModel reg=new LinearModel(y.getStorage(), false, td);
            Ols ols = new Ols();
            LeastSquaresResults rslt = ols.compute(reg);
            StatisticalTest ftest = rslt.Ftest();
            return TestResult.builder()
                    .value(ftest.getValue())
                    .pvalue(ftest.getPValue())
                    .description(ftest.getDistribution().toString())
                    .build();
          
        } catch (Exception err) {
            return null;
        }
    }

    private TestResult processAr(TsData s) {
        try {
            DataBlock y=DataBlock.of(s.values());
            RegularDomain domain = s.domain();
            GenericTradingDaysVariables var=new GenericTradingDaysVariables(GenericTradingDays.contrasts(DayClustering.TD7));
            Matrix td = RegressionUtility.data(Collections.singletonList(var), domain.range(1, domain.length()));
            LinearModel reg=LinearModel.of(y.drop(1, 0))
                    .addX(y.drop(0, 1))
                    .addX(td)
                    .build();
            
            Ols ols = new Ols();
            LeastSquaresResults rslt = ols.compute(reg);
            StatisticalTest ftest = rslt.Ftest(1, td.getColumnsCount());
            return TestResult.builder()
                    .value(ftest.getValue())
                    .pvalue(ftest.getPValue())
                    .description(ftest.getDistribution().toString())
                    .build();
         } catch (Exception err) {
            return null;
        }
    }
}
