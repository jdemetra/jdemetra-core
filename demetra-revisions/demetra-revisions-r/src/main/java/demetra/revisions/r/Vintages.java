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
import demetra.revisions.timeseries.VintageSelector;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jdplus.revisions.parametric.Processor;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class Vintages {

    TsDataVintages<LocalDate> core;

    private TsUnit unit() {
        return core.getStart().getUnit();
    }

    /**
     *
     * @return
     */
    public RegressionBasedAnalysis diagonalAnalysis() {
        return Processor.regressionBasedAnalysis(core, RegressionBasedAnalysis.Type.Diagonal);
    }

    public Vintages selectFirstRevisions(int nrevs) {
        return new Vintages(core.select(VintageSelector.first(nrevs)));
    }

    public Vintages selectBetween(String start, String end) {
        LocalDate d0 = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate d1 = LocalDate.parse(end, DateTimeFormatter.ISO_DATE);
        return new Vintages(core.select(VintageSelector.<LocalDate>custom(d0, d1)));
    }

    public RegressionBasedAnalysis verticalAnalysis() {
        return Processor.regressionBasedAnalysis(core, RegressionBasedAnalysis.Type.Vertical);
    }

    public TsData preliminary() {
        return core.preliminary();
    }

    public TsData current() {
        return core.current();
    }

    public TsData verticalVintage(String key) {
        LocalDate pdate = LocalDate.parse(key, DateTimeFormatter.ISO_DATE);
        return core.vintage(pdate);
    }

    public TsData diagonalVintage(int pos) {
        return core.vintage(pos);
    }

    public double[] values(String referenceDate) {
        LocalDate pdate = LocalDate.parse(referenceDate, DateTimeFormatter.ISO_DATE);
        TsPeriod p = TsPeriod.of(unit(), pdate);

        TsDataVintages<LocalDate> v = core;
        int pos = v.getStart().until(p);
        TsObsVintages vpos = v.get(pos);
        return vpos == null ? null : vpos.values();
    }

    public String[] vintages() {
        List<LocalDate> vintages = core.getVintages();
        String[] v = new String[vintages.size()];
        int pos = 0;
        for (LocalDate d : vintages) {
            v[pos++] = d.format(DateTimeFormatter.ISO_DATE);
        }
        return v;
    }

}
