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
package jdplus.timeseries.calendars;

import demetra.timeseries.ValidityPeriod;
import nbbrd.design.Development;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.FixedWeekDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import demetra.timeseries.calendars.SingleDate;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface HolidayInfo {

    /**
     * Day corresponding to the holiday
     *
     * @return
     */
    LocalDate getDay();

    /**
     * Day of week of the holiday
     *
     * @return
     */
    default DayOfWeek getDayOfWeek() {
        return getDay().getDayOfWeek();
    }

    /**
     * Returns the working date equal or before the given date
     *
     * @param date
     * @param nonworking List of non working days (ordered values of DayOfWeek)
     * @return
     */
    static LocalDate getPreviousWorkingDate(LocalDate date, int[] nonworking) {
        int dw = date.getDayOfWeek().getValue();
        for (int i = 0; i < nonworking.length; ++i) {
            if (dw == nonworking[i]) {
                int del = 1;
                while (del < nonworking.length) {
                    int pdw = dw - del;
                    if (pdw <= 0) {
                        pdw += 7;
                    }
                    boolean wd = true;
                    for (int j = i - 1; j >= 0; --j) {
                        if (nonworking[j] == pdw) {
                            wd = false;
                            break;
                        }
                    }
                    if (wd) {
                        for (int j = nonworking.length - 1; j > i; --j) {
                            if (nonworking[j] == pdw) {
                                wd = false;
                                break;
                            }
                        }
                    }
                    if (wd) {
                        break;
                    }
                    ++del;
                }
                return date.minusDays(del);
            }
        }
        return date;
    }

    /**
     * Returns the date equal or after the given date
     *
     * @param date
     * @param nonworking List of non working days (ordered values of DayOfWeek)
     * @return
     */
    static LocalDate getNextWorkingDate(LocalDate date, int[] nonworking) {
        int dw = date.getDayOfWeek().getValue();
        for (int i = 0; i < nonworking.length; ++i) {
            if (dw == nonworking[i]) {
                int del = 1;
                while (del < nonworking.length) {
                    int pdw = dw + del;
                    if (pdw > 7) {
                        pdw -= 7;
                    }
                    boolean wd = true;
                    for (int j = i + 1; j < nonworking.length; ++j) {
                        if (nonworking[j] == pdw) {
                            wd = false;
                            break;
                        }
                    }
                    if (wd) {
                        for (int j = 0; j < i; ++j) {
                            if (nonworking[j] == pdw) {
                                wd = false;
                                break;
                            }
                        }
                    }
                    if (wd) {
                        break;
                    }
                    ++del;

                }
                return date.plusDays(del);
            }
        }
        return date;
    }

    static FixedDayInfo of(FixedDay fday, int year) {
        return new FixedDayInfo(year, fday);
    }

    static EasterDayInfo of(EasterRelatedDay fday, int year) {
        return new EasterDayInfo(year, fday.getOffset(), fday.isJulian());
    }

    static FixedWeekDayInfo of(FixedWeekDay fday, int year) {
        return new FixedWeekDayInfo(year, fday);
    }

    static HolidayInfo of(Holiday holiday, int year) {
        if (holiday instanceof FixedDay) {
            return new FixedDayInfo(year, (FixedDay) holiday);
        } else if (holiday instanceof EasterRelatedDay) {
            return of((EasterRelatedDay) holiday, year);
         } else if (holiday instanceof FixedWeekDay) {
            return of((FixedWeekDay) holiday, year);
        } else if (holiday instanceof PrespecifiedHoliday) {
            PrespecifiedHoliday ph = (PrespecifiedHoliday) holiday;
            return of(ph.rawHoliday(), year);
        }
        throw new IllegalArgumentException();
    }

    static Iterable<HolidayInfo> iterable(Holiday holiday, LocalDate fstart, LocalDate fend) {
        if (holiday instanceof FixedDay) {
            return new FixedDayInfo.FixedDayIterable((FixedDay) holiday, fstart, fend);
        } else if (holiday instanceof EasterRelatedDay eday) {
             ValidityPeriod vp = eday.getValidityPeriod();
            if (vp.getStart().isAfter(fstart))
                fstart=vp.getStart();
            if (vp.getEnd().isBefore(fend))
                fend=vp.getEnd();
           return new EasterDayInfo.EasterDayList(eday.getOffset(), eday.isJulian(), fstart, fend);
        } else if (holiday instanceof FixedWeekDay fwd) {
            return new FixedWeekDayInfo.FixedWeekDayIterable(fwd, fstart, fend);
        } else if (holiday instanceof PrespecifiedHoliday ph) {
            return iterable(ph.rawHoliday(), fstart, fend);
        } else if (holiday instanceof SingleDate fd) {
            return new SingleDateInfo.SingleDateIterable(fd, fstart, fend);
       }
        throw new IllegalArgumentException();
    }

}
