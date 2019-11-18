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

import jdplus.data.DataBlock;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.matrices.Matrix;
import demetra.stats.TestResult;
import jdplus.stats.tests.StatisticalTest;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import jdplus.modelling.regression.Regression;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.GenericTradingDays;
import static jdplus.timeseries.simplets.TsDataToolkit.drop;
import static jdplus.timeseries.simplets.TsDataToolkit.delta;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TradingDaysTests {


    public TestResult ftest(TsData s, boolean ar, int ny) {
        int freq = s.getTsUnit().ratioOf(TsUnit.YEAR);

        if (ar) {
            if (ny != 0) {
                s = drop(s, Math.max(0, s.length() - freq * ny - 1), 0);
            }
            return processAr(s);
        } else {
            s = delta(s, 1);
            if (ny != 0) {
                s = drop(s, Math.max(0, s.length() - freq * ny), 0);
            }
            return process(s);
        }

    }

    private TestResult process(TsData s) {
        try {
            DataBlock y=DataBlock.of(s.getValues());
            y.sub(y.average());
            GenericTradingDaysVariable var=new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7));
            Matrix td = Regression.matrix(s.getDomain(), var);
            LinearModel reg=new LinearModel(y.getStorage(), false, td);
            Ols ols = new Ols();
            LeastSquaresResults rslt = ols.compute(reg);
            StatisticalTest ftest = rslt.Ftest();
            return ftest.toSummary();
          
        } catch (Exception err) {
            return null;
        }
    }

    private TestResult processAr(TsData s) {
        try {
            DataBlock y=DataBlock.of(s.getValues());
            TsDomain domain = s.getDomain();
            GenericTradingDaysVariable var=new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7));
            Matrix td = Regression.matrix(domain.range(1, domain.length()), var);
            LinearModel reg=LinearModel.builder()
                    .y(y.drop(1, 0))
                    .addX(y.drop(0, 1))
                    .addX(td)
                    .build();
            
            Ols ols = new Ols();
            LeastSquaresResults rslt = ols.compute(reg);
            StatisticalTest ftest = rslt.Ftest(1, td.getColumnsCount());
            return ftest.toSummary();
         } catch (Exception err) {
            return null;
        }
    }
}
