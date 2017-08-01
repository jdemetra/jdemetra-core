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
package demetra.timeseries.calendar;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.simplets.TsDomain;
import demetra.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.calendars.DefaultGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDaysTest {

    public GenericTradingDaysTest() {
    }

    @Test
    public void testTD() {
        TsDomain md = TsDomain.of(TsFrequency.Monthly, 1980, 0, 360);
        Matrix M1 = Matrix.make(md.length(), 6);
        GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
        gtd.data(md, M1.columnList());
        ec.tstoolkit.timeseries.simplets.TsDomain omd = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, 360);
        ec.tstoolkit.maths.matrices.Matrix oM1 = new ec.tstoolkit.maths.matrices.Matrix(omd.getLength(), 6);
        DefaultGregorianCalendarProvider.instance.calendarData(TradingDaysType.TradingDays, omd, oM1.columnList());

        for (int i = 0; i < 6; ++i) {
            assertTrue(distance(M1.column(i), oM1.column(i)) < 1e-9);
        }
    }

    private double distance(DataBlock column, ec.tstoolkit.data.DataBlock column0) {
        return column.distance(
                DataBlock.ofInternal(column0.getData(), column0.getStartPosition(), column0.getEndPosition(), column0.getIncrement()));
    }

}
