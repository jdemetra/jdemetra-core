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

import demetra.revisions.timeseries.TsDataVintages;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class will simplify the use of the Java library. It could be avoided,
 * but the R code would be significantly more complex
 *
 * @author PALATEJ
 */
public class VintagesFactory {

    private final TsUnit unit;
    private final TsDataVintages.Builder<LocalDate> builder = TsDataVintages.<LocalDate>builder();

     public VintagesFactory(int period) {
        this.unit = TsUnit.ofAnnualFrequency(period);
    }

    public void add(String periodDate, String registrationDate, double value) {
        synchronized (this) {
            LocalDate pdate = LocalDate.parse(periodDate, DateTimeFormatter.ISO_DATE);
            LocalDate rdate = LocalDate.parse(registrationDate, DateTimeFormatter.ISO_DATE);
            TsPeriod p = TsPeriod.of(unit, pdate);
            builder.add(p, rdate, value);
         }
    }

    public TsDataVintages<LocalDate> build() {
        return builder.build();
    }

}
