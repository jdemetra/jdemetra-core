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
import demetra.revisions.timeseries.TsObsVintages;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.revisions.parametric.Processor;

/**
 * This class will simplify the use of the Java library. It could be avoided,
 * but the R code would be significantly more complex
 *
 * @author PALATEJ
 */
public class VintagesFactory {

    private final TsUnit unit;
    private final TsDataVintages.Builder<LocalDate> builder = new TsDataVintages.Builder<>();

    private volatile TsDataVintages vintages;

    public VintagesFactory(int period) {
        this.unit = TsUnit.ofAnnualFrequency(period);
    }

    public void add(String periodDate, String registrationDate, double value) {
        synchronized (this) {
            LocalDate pdate = LocalDate.parse(periodDate, DateTimeFormatter.ISO_DATE);
            LocalDate rdate = LocalDate.parse(registrationDate, DateTimeFormatter.ISO_DATE);
            TsPeriod p = TsPeriod.of(unit, pdate);
            builder.add(p, rdate, value);
            vintages = null;
        }
    }

    public TsDataVintages<LocalDate> build() {
        TsDataVintages v = vintages;
        if (v == null) {
            synchronized (this) {
                v = builder.build();
                vintages = v;
            }
        }
        return v;
    }

    /**
     *
     * @param nrevs
      * @return
     */
    public RegressionBasedAnalysis regressionBasedAnalysis(int nrevs) {
        return Processor.regressionBasedAnalysis(build(), nrevs);
    }

    public TsData preliminary() {
        return build().preliminary();
    }

    public TsData current() {
        return build().current();
    }

    public double[] values(String referenceDate) {
        LocalDate pdate = LocalDate.parse(referenceDate, DateTimeFormatter.ISO_DATE);
        TsPeriod p = TsPeriod.of(unit, pdate);

        TsDataVintages<LocalDate> v = build();
        int pos = v.getStart().until(p);
        TsObsVintages vpos = v.get(pos);
        return vpos == null ? null : vpos.values();
    }
}
