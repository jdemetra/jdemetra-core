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
package demetra.revisions.timeseries;

import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TsDataVintagesTest {

    public TsDataVintagesTest() {
    }

    @Test
    public void testDateSelection() {
        TsDataVintages<LocalDateTime> v = random(360, 15);
        LocalDateTime t=LocalDateTime.of(2020, 1, 1, 0, 0);
        TsData s1 = v.vintage(t);
        TsData s2 = TsDataVintages.<LocalDateTime>seriesAt(v, t, (p, q)->p.compareTo(q));
        assertEquals(s1, s2);
    }

//    @Test
//    public void testVintageSelection() {
//        TsDataVintages<LocalDateTime> v = random(360, 15);
//        LocalDateTime t0=LocalDateTime.of(2015, 1, 1, 0, 0);
//        LocalDateTime t1=LocalDateTime.of(2020, 1, 1, 0, 0);
//        VintageSelector sel = VintageSelector.<LocalDateTime>custom(t0, t1);
//        TsDataVintages <LocalDateTime>vc = v.select(sel);
//        List<LocalDateTime> vintages = vc.getVintages();
//        assertTrue(!vintages.get(0).isBefore(t0));
//        assertTrue(!vintages.get(vintages.size()-1).isAfter(t1));
//    }
//
    private static TsDataVintages<LocalDateTime> random(int N, int K) {
        Random rnd = new Random();
        TsDataVintages.Builder<LocalDateTime> builder = TsDataVintages.<LocalDateTime>builder();
        TsPeriod start = TsPeriod.monthly(2000, 1);
        for (int i = 0; i < N; ++i) {
            for (int k = 0; k < K; ++k) {
                builder.add(start, start.end().plusDays(k * 20), rnd.nextDouble());
            }
            start = start.next();
        }
        return builder.build();
    }
}
