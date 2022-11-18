/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.modelling.regression;

import demetra.math.matrices.Matrix;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.regression.GenericHolidaysVariable;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.timeseries.calendars.HolidaysUtility;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class GenericHolidaysFactory implements RegressionVariableFactory<GenericHolidaysVariable> {

    public static GenericHolidaysFactory FACTORY = new GenericHolidaysFactory();

    private GenericHolidaysFactory() {
    }

    @Override
    public boolean fill(GenericHolidaysVariable var, TsPeriod start, FastMatrix buffer) {
        int n = buffer.getRowsCount();
        TsDomain domain = TsDomain.of(start, n);
        FastMatrix days = FastMatrix.make(n, 7);
        GenericTradingDaysFactory.fillTdMatrix(start, days);
        Matrix corr = HolidaysCorrectionFactory.corrector(var.getCalendar(), var.getHoliday(), false).holidaysCorrection(domain);
        for (int i = 0; i < 7; ++i) {
            days.column(i).apply(corr.column(i), (a, b) -> a + b);
        }

        int freq = start.annualFrequency();
        DayClustering clustering = var.getClustering();
        FastMatrix D = HolidaysUtility.days(var.getCalendar(), freq, var.getHoliday().getValue() - 1);
        FastMatrix DC = HolidaysUtility.clustering(D, clustering);
        FastMatrix daysC = HolidaysUtility.clustering(days, clustering);
        int pos = start.annualPosition();
        int nc = buffer.getColumnsCount();
        
        DataBlock c0 = DC.column(0);
        for (int c=1; c<=nc; ++c){
            DC.column(c).div(c0);
        }

        // NOT OPTIMIZED
        c0 = daysC.column(0);
        for (int c = 1; c <= nc; ++c) {
            DataBlock ccur = daysC.column(c);
            for (int i = 0, j = pos; i < n; ++i) {
                double w=DC.get(j, c);
                buffer.set(i, c-1, ccur.get(i)-w*c0.get(i));
                if (++j == freq) {
                    j = 0;
                }
            }
        }

        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(GenericHolidaysVariable var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
