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
import demetra.revisions.timeseries.TsObsVintages;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.Matrix;
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
     * @param firstRow First row of the selection (included, 1-based)
     * @param lastRow Last row of the selection (included, 1-based). If lastRow
     * is negative,
     * firstRow is not taken into account and we take the -lastRow rows
     * @param firstVintage First vintage (included)
     * @param lastVintage Last vintage (included)
     * @return The figures corresponding to the selection. If a data for a given
     * vintage
     * is unavailable, we take the previous one
     */
    public TsMatrix vtable(int firstRow, int lastRow, String firstVintage, String lastVintage) {
        List<LocalDate> vintages = core.getVintages();
        int nv = vintages.size();
        int v0 = 0, v1 = nv - 1;

        LocalDate fdate = firstVintage == null ? vintages.get(0) : LocalDate.parse(firstVintage, DateTimeFormatter.ISO_DATE);
        LocalDate ldate = lastVintage == null ? vintages.get(v1) : LocalDate.parse(lastVintage, DateTimeFormatter.ISO_DATE);
        while (v0 < nv && vintages.get(v0).isBefore(fdate)) {
            ++v0;
        }
        while (v1 >= 0 && vintages.get(v1).isAfter(ldate)) {
            --v1;
        }

        if (v1 < v0) {
            return null;
        }
        ++v1;
        int r0 = firstRow - 1, r1 = lastRow;
        TsDomain domain = core.getDomain();
        if (lastRow < 0) {
            r0 = domain.length() + lastRow;
            r1 = domain.length();
        }
        if (r1 > domain.length()) {
            r1 = domain.length();
        }
        if (r0 < 0) {
            r0 = 0;
        }
        if (r1 <= r0) {
            return null;
        }
        Matrix data = Matrix.make(r1 - r0, v1 - v0);
        // fill the matrix
        DataBlockIterator cols = data.columnsIterator();
        final int rstart = r0;
        String[] ids = new String[data.getColumnsCount()];
        for (int v = v0; v < v1; ++v) {
            LocalDate cdate = vintages.get(v);
            ids[v - v0] = cdate.format(DateTimeFormatter.ISO_DATE);
            cols.next().set(i -> core.data(i + rstart, cdate));
        }
        return new TsMatrix(domain.get(r0), data, ids);
    }

    public TsMatrix vtable() {
        return vtable(1, core.length(), null, null);
    }

    /**
     *
     * @return
     */
    public RegressionBasedAnalysis diagonalAnalysis(int first, int last) {
        return Processor.diagonalAnalysis(core, first, last);
    }

    public RegressionBasedAnalysis verticalAnalysis(String first, String last) {
        LocalDate fdate = LocalDate.parse(first, DateTimeFormatter.ISO_DATE);
        LocalDate ldate = LocalDate.parse(last, DateTimeFormatter.ISO_DATE);

        return Processor.verticalAnalysis(core, fdate, ldate);
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
