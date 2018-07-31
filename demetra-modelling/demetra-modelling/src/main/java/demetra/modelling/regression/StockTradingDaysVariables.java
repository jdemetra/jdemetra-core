/*
* Copyright 2013 National Bank of Belgium
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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.CalendarUtility;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class StockTradingDaysVariables implements ITradingDaysVariable {

    // Wth day of the month. 0-based! See documentation of X12 Arima, for instance
    private final int w;
    private final String name;

    /**
     * Creates a new set of StockTradingDays variables
     *
     * @param w The wth day of the month is considered. When w is negative, the
     * (-w) day before the end of the month is considered
     *
     */
    public StockTradingDaysVariables(int w, String name) {
        this.w = w;
        this.name = name != null ? name : NAME;
    }

    @Override
    public String getDescription(TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append("TD Stock [").append(w).append(']');
        return builder.toString();
    }

    @Override
    public int getDim() {
        return 6;
    }

    @Override
    public String getItemDescription(int idx, TsDomain context) {
        return DayOfWeek.of(idx + 1).name();
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        // TODO
        int n = domain.getLength();
        for (int i = 0; i < n; ++i) {
            LocalDate end = domain.get(i).end().toLocalDate();
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
                    for (int j = 0; j < 6; ++j) {
                        data.get(j).set(i, -1);
                    }
                } else {
                    data.get(g - 1).set(i, 1);
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
                    for (int j = 0; j < 6; ++j) {
                        data.get(j).set(i, -1);
                    }
                } else {
                    data.get(g - 1).set(i, 1);
                }

            }
        }
//            // begin contains the first day of the last month of each period
//            begin[i] = Day.calc(month.getYear(), month.getPosition(), 0);
//            month.move(conv);
//        }
//        double[] z0 = new double[7];
//        for (int j = 0; j < n; j++) {
//            java.util.Arrays.fill(z0, 0.0d);
//            //
//            // z0[0] = Sunday
//            //
//            int dayofweek = (begin[j] - 3) % 7;
//            int monthlen = begin[j + 1] - begin[j];
//            if (dayofweek < 0) {
//                dayofweek += 7;
//            }
//            // 
//            // w_ (like in Tramo ??) could be negative. (if we want to stock on the -w_ day before month-end
//            // Example : 
//            // w_ = -2 
//            // 
//            // Jan 29
//            // Feb 26 or 27 (if LY)
//            // .............
//            // Apr 28
//            // .............
//            //
//            if (this.w >= 0) {
//                if (this.w < monthlen) {
//                    monthlen = this.w;
//                }
//            } else {
//                monthlen += this.w;
//                if (monthlen <= 0) {
//                    monthlen = 1;
//                }
//            }
//            int Lastdayofweek = (dayofweek + (monthlen - 1)) % 7;
//            z0[Lastdayofweek] = 1.0d;
//            for (int i = 0; i < 6; ++i) {
//                DataBlock x = data.get(i);
//                x.set(j, z0[i + 1] - z0[0]);
//            }
//        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StockTradingDaysVariables rename(String newname) {
        return new StockTradingDaysVariables(w, newname);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof StockTradingDaysVariables) {
            StockTradingDaysVariables x = (StockTradingDaysVariables) other;
            return x.w == w;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.w;
        return hash;
    }

}
