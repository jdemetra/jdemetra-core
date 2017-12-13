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
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.maths.matrices.Matrix;
import demetra.stats.TestResult;
import demetra.stats.tests.LjungBoxTest;
import demetra.stats.tests.StatisticalTest;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.regression.PeriodicContrasts;
import demetra.timeseries.regression.RegressionUtility;
import demetra.timeseries.simplets.TsData;
import static demetra.timeseries.simplets.TsDataToolkit.delta;
import static demetra.timeseries.simplets.TsDataToolkit.drop;
import java.util.Collections;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeasonalityTests {

    public TestResult ftest(TsData s, boolean ar, int ny) {
        int freq = s.getTsUnit().ratioOf(TsUnit.YEAR);

        if (ar) {
            if (ny != 0) {
                s = drop(s, Math.max(0, s.length() - freq * ny - 1), 0);
            }
            return processAr(s, freq);
        } else {
            s = delta(s, 1);
            if (ny != 0) {
                s = drop(s, Math.max(0, s.length() - freq * ny), 0);
            }
            return process(s, freq);
        }
    }

    public TestResult qstest(TsData s, int ny) {
        int freq = s.getTsUnit().ratioOf(TsUnit.YEAR);

        s = delta(s, 1);
        if (ny != 0) {
            s = drop(s, Math.max(0, s.length() - freq * ny), 0);
        }
        StatisticalTest test = new LjungBoxTest(s.values())
                .lag(freq)
                .autoCorrelationsCount(2)
                .usePositiveAutoCorrelations()
                .build();
        return TestResult.builder()
                .value(test.getValue())
                .pvalue(test.getPValue())
                .description(test.getDistribution().toString())
                .build();
    }

    private TestResult process(TsData s, int freq) {
        try {
            DataBlock y = DataBlock.of(s.values());
            y.sub(y.average());
            RegularDomain domain = s.domain();
            PeriodicContrasts var = new PeriodicContrasts(freq);
            Matrix sd = RegressionUtility.data(Collections.singletonList(var), domain);
            LinearModel reg = new LinearModel(y.getStorage(), false, sd);
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

    private TestResult processAr(TsData s, int freq) {
        try {
            DataBlock y = DataBlock.of(s.values());
            RegularDomain domain = s.domain();
            PeriodicContrasts var = new PeriodicContrasts(freq);

            Matrix sd = RegressionUtility.data(Collections.singletonList(var), domain.range(1, domain.length()));
            LinearModel reg = LinearModel.of(y.drop(1, 0))
                    .addX(y.drop(0, 1))
                    .addX(sd)
                    .meanCorrection(true)
                    .build();

            Ols ols = new Ols();
            LeastSquaresResults rslt = ols.compute(reg);
            StatisticalTest ftest = rslt.Ftest(2, sd.getColumnsCount());
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
