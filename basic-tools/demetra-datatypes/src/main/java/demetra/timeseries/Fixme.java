/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries;

import java.util.function.IntSupplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
@Deprecated
public class Fixme {

    public int getAsInt(TsUnit freq) {
        return OldFreq.of(freq).getAsInt();
    }

    public int getId(TsPeriod p) {
        return (int) p.getId();
    }

    public int getPosition(TsPeriod p) {
        if (p.getReference()!= TsPeriod.EPOCH) {
            throw new UnsupportedOperationException("Unsupported origin");
        }
        OldFreq freq = OldFreq.of(p.getUnit());
        return (p.start().getMonthValue() - 1) * freq.getAsInt() / 12;
    }

    private enum OldFreq implements IntSupplier {
        Undefined(0, TsUnit.UNDEFINED),
        Yearly(1, TsUnit.YEAR),
        HalfYearly(2, TsUnit.HALF_YEAR),
        Quarterly(4, TsUnit.QUARTER),
        Monthly(12, TsUnit.MONTH);

        final int val;
        final TsUnit freq;

        private OldFreq(int val, TsUnit freq) {
            this.val = val;
            this.freq = freq;
        }

        @Override
        public int getAsInt() {
            return val;
        }

        public static OldFreq of(TsUnit freq) {
            switch (freq.getChronoUnit()) {
                case YEARS:
                    if (freq.getAmount() == 1) {
                        return Yearly;
                    }
                    break;
                case MONTHS:
                    if (freq.getAmount() == 6) {
                        return HalfYearly;
                    }
                    if (freq.getAmount() == 3) {
                        return Quarterly;
                    }
                    if (freq.getAmount() == 1) {
                        return Monthly;
                    }
                    break;
            }
            throw new UnsupportedOperationException(freq.toString());
        }
    }

    /**
     * Returns the number of days for the month before or equal to the given
     * month. We consider that there are 28 days in February
     *
     * @param month 1-based index of the month
     * @return
     */
    public static int getCumulatedMonthDays(int month) {
        return CUMULATEDMONTHDAYS[month];
    }

    /**
     * Cumulative number of days (if no leap year). CumulatedMonthDays[2] =
     * number of days from 1/1 to 28/2.
     */
    private static final int[] CUMULATEDMONTHDAYS = {0, 31, 59, 90, 120, 151,
        181, 212, 243, 273, 304, 334, 365};
}
