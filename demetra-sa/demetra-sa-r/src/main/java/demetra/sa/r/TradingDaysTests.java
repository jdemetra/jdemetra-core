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

import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import jdplus.modelling.regular.tests.TradingDaysTest;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TradingDaysTests {

    public StatisticalTest fTest(TsData s, String model, int ny) {
        s = s.cleanExtremities();
        int freq = s.getAnnualFrequency();
        TsData slast = s;
        if (ny != 0) {
            slast = s.drop(Math.max(0, s.length() - freq * ny), 0);
        }
        if (model.equalsIgnoreCase("D1")) {
            return TradingDaysTest.olsTest(slast, 1);
        } else if (model.equalsIgnoreCase("DY")) {
            return TradingDaysTest.olsTest(slast, freq);
        } else if (model.equalsIgnoreCase("DYD1")) {
            return TradingDaysTest.olsTest(slast, freq, 1);
        } else if (model.equalsIgnoreCase("WN")) {
            return TradingDaysTest.olsTest(slast, 0);
        } else {
            return null;
        }
    }

}
