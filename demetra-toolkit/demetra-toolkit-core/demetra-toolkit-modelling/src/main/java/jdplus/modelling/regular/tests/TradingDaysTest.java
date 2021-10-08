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
import jdplus.math.matrices.MatrixWindow;
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
     *
     * dy(t)-dybar ~ dtd + e
     *
     * dy is the series after differencing and dtd are the trading days after
     * differencing
     *
     * @param y Tested time series.
     * @param lags
     * @return F test
     */
    public StatisticalTest olsTest(TsData y, int... lags) {
        try {
            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
            Matrix m = Regression.matrix(y.getDomain(), td);
            DoubleSeq dy = y.getValues();
            Matrix dm = m;
            if (lags != null) {
                for (int j = 0; j < lags.length; ++j) {
                    int lag = lags[j];
                    if (lag > 0) {
                        Matrix mj = dm;
                        int nr = mj.getRowsCount(), nc = mj.getColumnsCount();
                        dm = mj.extract(lag, nr - lag, 0, nc).deepClone();
                        dm.sub(mj.extract(0, nr - lag, 0, nc));
                        dy = dy.delta(lag);
                    }
                }
            }
            dy = dy.plus(-dy.average());
            LinearModel reg = LinearModel.builder()
                    .y(dy)
                    .addX(dm)
                    .build();
            LeastSquaresResults lsr = Ols.compute(reg);
            return lsr.Ftest();
        } catch (Exception err) {
            return null;
        }
    }
}
