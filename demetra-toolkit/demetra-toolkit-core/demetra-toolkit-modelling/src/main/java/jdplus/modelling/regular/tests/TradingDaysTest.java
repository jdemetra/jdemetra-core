/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.modelling.regular.tests;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import jdplus.linearmodel.JointTest;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.Regression;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TradingDaysTest {

    /**
     * F test on generic trading days regressors (6 contrast variables)
     * 
     * The model is
     * y(t)-ymean ~ td + e
     * 
     * @param s Tested time series. Should be stationary
     * @return F test
     */
    public StatisticalTest olsTest(TsData s) {
        try {
            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
            Matrix matrix = Regression.matrix(s.getDomain(), td);
            LinearModel reg = LinearModel.builder()
                    .y(s.getValues().plus(-s.getValues().average()))
                    .addX(matrix)
                    .build();
            LeastSquaresResults lsr = Ols.compute(reg);
            return lsr.Ftest();
        } catch (Exception err) {
            return null;
        }
    }
    /**
     * F test on generic trading days regressors (6 contrast variables)
     * 
     * The model is
     * y(t) ~ c + y(t-1) + td + e
     * 
     * @param s Tested time series
     * @return F test
     */
    public StatisticalTest olsTest2(TsData s) {
        try {
            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
            TsDomain edomain = s.getDomain().drop(1, 0);
            DoubleSeq y = s.getValues();
            Matrix matrix = Regression.matrix(edomain, td);
            LinearModel reg = LinearModel.builder()
                    .y(y.drop(1, 0))
                    .meanCorrection(true)
                    .addX(y.drop(0, 1))
                    .addX(matrix)
                    .build();
            int nseas = td.dim();
            LeastSquaresResults lsr = Ols.compute(reg);
            return new JointTest(lsr.getLikelihood())
                    .variableSelection(2, nseas)
                    .blue()
                    .build();
        } catch (Exception err) {
            return null;
        }
    }

}
