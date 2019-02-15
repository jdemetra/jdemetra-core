/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.calendar.r;

import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.calendars.DayEvent;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.HolidaysUtility;
import demetra.timeseries.calendars.IHoliday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class Holidays {

    private final List<Holiday> holidays = new ArrayList<>();

    private boolean add(IHoliday fday) {
        Holiday ev = new Holiday(fday);
        if (!holidays.contains(ev)) {
            holidays.add(ev);
            return true;
        } else {
            return false;
        }
    }

    private Holiday[] elements() {
        return holidays.toArray(new Holiday[holidays.size()]);
    }

    public boolean add(String holiday, int offset, double weight, boolean julian) {
        try {
            PrespecifiedHoliday cur = new PrespecifiedHoliday(DayEvent.valueOf(holiday), offset, weight, julian);
            return add(cur);
        } catch (Exception err) {
            return false;
        }
    }

    public boolean addFixedDay(int month, int day, double weight, boolean julian) {
        FixedDay cur = new FixedDay(month, day, weight);
        return add(cur);
    }

    public MatrixType holidays(String date, int length, String type) {
        LocalDate start = LocalDate.parse(date);
        Holiday[] elements = elements();
        Matrix m = Matrix.make(length, elements.length);
        switch (type) {
            case "SkipSundays":
                HolidaysUtility.fillDays(elements, m, start, true);
                break;
            case "NextWorkingDay":
                HolidaysUtility.fillNextWorkingDays(elements, m, start, 0);
                break;
            case "PreviousWorkingDay":
                HolidaysUtility.fillPreviousWorkingDays(elements, m, start, 0);
                break;
            default:
                HolidaysUtility.fillDays(elements, m, start, false);
        }
        return m.unmodifiable();
    }

}
