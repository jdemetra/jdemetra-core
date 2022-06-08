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
package demetra.revisions.r;

import demetra.revisions.parametric.RegressionBasedAnalysis;
import demetra.revisions.timeseries.TsDataVintages;
import demetra.revisions.timeseries.TsMatrix;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class UtilityTest {
    
    public UtilityTest() {
    }

    @Test
    public void testVertical() {
        TsDataVintages<LocalDate> v = random(480, 20);
        LocalDate t0=LocalDate.of(2015, 1, 1);
        LocalDate t1=LocalDate.of(2020, 1, 1);
//        VintageSelector sel = VintageSelector.<LocalDate>custom(t0, t1);
//        TsDataVintages <LocalDate>vc = v.select(sel);
        Vintages V=new Vintages(v);
        RegressionBasedAnalysis<LocalDate> analysis = V.verticalAnalysis(t0.format(DateTimeFormatter.ISO_DATE), t1.format(DateTimeFormatter.ISO_DATE));
        double theil = Utility.theil(analysis, 10);
        double[] biasInformation = Utility.biasInformation(analysis, 10);
    }

    @Test
    public void testvtable() {
        TsDataVintages<LocalDate> v = random2(480, 20);
        LocalDate t0=LocalDate.of(2005, 1, 1);
        LocalDate t1=LocalDate.of(2020, 1, 1);
        Vintages V=new Vintages(v);
        TsMatrix vtable = V.vtable(3, 136, t0.format(DateTimeFormatter.ISO_DATE), t1.format(DateTimeFormatter.ISO_DATE));
        assertTrue(vtable.getMatrix().getRowsCount() == 134);
//        System.out.println(vtable.getMatrix());
    }

    @Test
    public void testDiagonal() {
        TsDataVintages<LocalDate> v = random(360, 20);
        LocalDate t0=LocalDate.of(2015, 1, 1);
        LocalDate t1=LocalDate.of(2020, 1, 1);
        Vintages V=new Vintages(v);
        RegressionBasedAnalysis<LocalDate> analysis = V.diagonalAnalysis(0, 15);
        double theil = Utility.theil(analysis, 10);
        double[] biasInformation = Utility.biasInformation(analysis, 10);
    }

    private static TsDataVintages<LocalDate> random(int N, int K) {
        Random rnd = new Random();
        TsDataVintages.Builder<LocalDate> builder = TsDataVintages.<LocalDate>builder();
        TsPeriod start = TsPeriod.monthly(2000, 1);
        for (int i = 0; i < N; ++i) {
            for (int k = 0; k < K; ++k) {
                builder.add(start, start.end().toLocalDate().plusDays(k * 20), rnd.nextDouble());
            }
            start = start.next();
        }
        return builder.build();
    }
    
    private static TsDataVintages<LocalDate> random2(int N, int K) {
        Random rnd = new Random();
        TsDataVintages.Builder<LocalDate> builder = TsDataVintages.<LocalDate>builder();
        TsPeriod start = TsPeriod.monthly(2000, 1);
        for (int i = 0; i < N; ++i) {
            for (int k = 0; k < K; ++k) {
                TsPeriod v = start.plus(k);
                builder.add(start, v.end().toLocalDate(), rnd.nextDouble());
                builder.add(start, v.end().toLocalDate().plusDays(15), rnd.nextDouble());
            }
            start = start.next();
        }
        return builder.build();
    }
}
