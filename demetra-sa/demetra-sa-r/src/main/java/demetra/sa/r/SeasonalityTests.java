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
package demetra.sa.r;

import jdplus.data.DataBlock;
import demetra.stats.StatisticalTest;
import jdplus.stats.tests.LjungBox;
import jdplus.sa.tests.CanovaHansen;
import jdplus.sa.tests.CanovaHansen2;
import jdplus.sa.tests.PeriodicLjungBox;
import demetra.data.DoubleSeq;
import demetra.sa.diagnostics.CombinedSeasonalityTest;
import demetra.sa.io.protobuf.SaProtosUtility;
import jdplus.sa.tests.CombinedSeasonality;
import jdplus.sa.tests.FTest;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeasonalityTests {

    public StatisticalTest qsTest(double[] x, int period, int ny) {
        double[] s=x.clone();
        for (int i = s.length - 1; i > 0; --i) {
            s[i] -= s[i - 1];
        }
        DoubleSeq y = DoubleSeq.of(s, 1, s.length - 1);
        if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        return new LjungBox(y)
                .lag(period)
                .autoCorrelationsCount(2)
                .usePositiveAutoCorrelations()
                .build();
     }

    public StatisticalTest periodicQsTest(double[] x, double[] periods) {
        DoubleSeq y;
        double[] s=x.clone();
        if (periods.length == 1) {
            for (int j = s.length - 1; j > 0; --j) {
                s[j] -= s[j - 1];
            }
            y = DoubleSeq.of(s, 1, s.length - 1);
        } else {
            int del = 0;
            for (int i = 1; i < periods.length; ++i) {
                int p = (int) periods[i];
                del += p;
                for (int j = s.length - 1; j >= del; --j) {
                    s[j] -= s[j - p];
                }
            }
            y = DoubleSeq.of(s, del, s.length - del);
        }
        return new PeriodicLjungBox(y, 0)
                .lags(periods[0], 2)
                .usePositiveAutocorrelations()
                .build();
    }

    public double[] canovaHansenTest(double[] s, int start, int end, boolean original) {
        double[] rslt = new double[end - start];
        DoubleSeq x = DoubleSeq.of(s);
        for (int i = start; i < end; ++i) {
            if (original) {
                rslt[i - start] = CanovaHansen.test(x)
                        .specific(i, 1)
                        .build()
                        .testAll();
            } else {
                rslt[i - start] = CanovaHansen2.of(x)
                        .periodicity(i)
                        .compute();
            }
        }
        return rslt;
    }

    public StatisticalTest fTest(double[] s, int freq, int ny, String model) {
        FTest.Model M = FTest.Model.valueOf(model);
        try {
            DataBlock y = DataBlock.of(s);
            return new FTest(y, freq)
                    .model(M)
                    .ncycles(ny)
                    .build();
        } catch (Exception err) {
            return null;
        }
    }

    public CombinedSeasonality combinedTest(double[] data, int period, int startperiod, boolean mul){
         return new CombinedSeasonality(DoubleSeq.of(data), period, startperiod, mul);
    }
    
    public byte[] toBuffer(CombinedSeasonality cs){
        return SaProtosUtility.convert(cs).toByteArray();
    }
}
