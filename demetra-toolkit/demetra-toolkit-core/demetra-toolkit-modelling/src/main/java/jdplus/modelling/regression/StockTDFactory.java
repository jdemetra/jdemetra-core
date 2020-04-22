/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.data.Range;
import demetra.timeseries.regression.StockTradingDays;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.CalendarUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
class StockTDFactory implements RegressionVariableFactory<StockTradingDays> {

     static StockTDFactory FACTORY=new StockTDFactory();

    private StockTDFactory(){}

    @Override
    public boolean fill(StockTradingDays var, TsPeriod start, Matrix buffer) {
        int n = buffer.getRowsCount();
        int w = var.getW();
        TsPeriod cur = start;
        for (int i = 0; i < n; ++i) {
            LocalDate end = cur.end().toLocalDate();
            cur = cur.next();
            // first day after the current period
            if (w <= 0) {
                // 1 for monday, 7 for sunday
                int dw = end.getDayOfWeek().getValue();
                int g = (dw + w - 1) % 7;
                if (g < 0) {
                    g += 7;
                }
                if (g == 0) // Sunday
                {
                    buffer.row(i).set(-1);
                } else {
                    buffer.set(i, g - 1, 1);
                }
            } else {
                int month = end.getMonthValue() - 1;
                int year = end.getYear();
                if (month == 0) {
                    month = 12;
                    --year;
                }
                int day = w;
                if (day > 28) {
                    int wmax = CalendarUtility.getNumberOfDaysByMonth(year, month);
                    if (day > wmax) {
                        day = wmax;
                    }
                }
                LocalDate d = LocalDate.of(year, month, day);

                int g = d.getDayOfWeek().getValue();
                if (g == 7) // Sunday
                {
                    buffer.row(i).set(-1);
                } else {
                    buffer.set(i, g - 1, 1);
                }
            }
        }
        return true;
    }

    @Override
    public <P extends Range<LocalDateTime>, D extends TimeSeriesDomain<P>>  boolean fill(StockTradingDays var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

}
