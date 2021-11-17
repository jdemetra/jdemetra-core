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
package demetra.toolkit.io.protobuf;

import demetra.timeseries.ValidityPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.DayEvent;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.FixedWeekDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class CalendarProtosUtility {

    public ToolkitProtos.ValidityPeriod convert(ValidityPeriod vp) {
        return ToolkitProtos.ValidityPeriod.newBuilder()
                .setStart(ToolkitProtosUtility.convert(vp.getStart()))
                .setEnd(ToolkitProtosUtility.convert(vp.getEnd()))
                .build();
    }

    public ValidityPeriod convert(ToolkitProtos.ValidityPeriod vp) {
        return ValidityPeriod.between(
                ToolkitProtosUtility.convert(vp.getStart()),
                ToolkitProtosUtility.convert(vp.getEnd()));
    }

    public ToolkitProtos.FixedDay convert(@NonNull FixedDay fd) {
        return ToolkitProtos.FixedDay.newBuilder()
                .setMonth(fd.getMonth())
                .setDay(fd.getDay())
                .setWeight(fd.getWeight())
                .setValidity(convert(fd.getValidityPeriod()))
                .build();
    }

    public FixedDay convert(ToolkitProtos.FixedDay ed) {
        return new FixedDay(ed.getMonth(), ed.getDay(), ed.getWeight(), convert(ed.getValidity()));
    }

    public ToolkitProtos.FixedWeekDay convert(@NonNull FixedWeekDay fd) {
        return ToolkitProtos.FixedWeekDay.newBuilder()
                .setMonth(fd.getMonth())
                .setWeekday(fd.getDayOfWeek().getValue())
                .setPosition(fd.getPlace())                
                .setWeight(fd.getWeight())
                .setValidity(convert(fd.getValidityPeriod()))
                .build();
    }

    public FixedWeekDay convert(ToolkitProtos.FixedWeekDay ed) {
        return new FixedWeekDay(ed.getMonth(), ed.getPosition(), DayOfWeek.of(ed.getWeekday()) , ed.getWeight(), convert(ed.getValidity()));
    }

    public ToolkitProtos.EasterRelatedDay convert(@NonNull EasterRelatedDay ed) {
        return ToolkitProtos.EasterRelatedDay.newBuilder()
                .setOffset(ed.getOffset())
                .setJulian(ed.isJulian())
                .setWeight(ed.getWeight())
                .setValidity(convert(ed.getValidityPeriod()))
                .build();
    }

    public EasterRelatedDay convert(ToolkitProtos.EasterRelatedDay ed) {
        if (ed.getJulian()) {
            return EasterRelatedDay.julian(ed.getOffset(), ed.getWeight(), convert(ed.getValidity()));
        } else {
            return EasterRelatedDay.gregorian(ed.getOffset(), ed.getWeight(), convert(ed.getValidity()));
        }
    }

    public ToolkitProtos.CalendarEvent convert(DayEvent hol) {
        switch (hol) {
            case NewYear:
                return ToolkitProtos.CalendarEvent.HOLIDAY_NEWYEAR;
            case ShroveMonday:
                return ToolkitProtos.CalendarEvent.HOLIDAY_SHROVEMONDAY;
            case ShroveTuesday:
                return ToolkitProtos.CalendarEvent.HOLIDAY_SHROVETUESDAY;
            case AshWednesday:
                return ToolkitProtos.CalendarEvent.HOLIDAY_ASHWEDNESDAY;
            case Easter:
                return ToolkitProtos.CalendarEvent.HOLIDAY_EASTER;
            case MaundyThursday:
                return ToolkitProtos.CalendarEvent.HOLIDAY_MAUNDYTHURSDAY;
            case GoodFriday:
                return ToolkitProtos.CalendarEvent.HOLIDAY_GOODFRIDAY;
            case EasterMonday:
                return ToolkitProtos.CalendarEvent.HOLIDAY_EASTERMONDAY;
            case Ascension:
                return ToolkitProtos.CalendarEvent.HOLIDAY_ASCENSION;
            case Pentecost:
                return ToolkitProtos.CalendarEvent.HOLIDAY_PENTECOST;
            case CorpusChristi:
                return ToolkitProtos.CalendarEvent.HOLIDAY_CORPUSCHRISTI;
            case WhitMonday:
                return ToolkitProtos.CalendarEvent.HOLIDAY_WHITMONDAY;
            case MayDay:
                return ToolkitProtos.CalendarEvent.HOLIDAY_MAYDAY;
            case Assumption:
                return ToolkitProtos.CalendarEvent.HOLIDAY_ASSUMPTION;
            case LaborDay:
                return ToolkitProtos.CalendarEvent.HOLIDAY_LABORDAY;
            case Halloween:
                return ToolkitProtos.CalendarEvent.HOLIDAY_HALLOWEEN;
            case AllSaintsDay:
                return ToolkitProtos.CalendarEvent.HOLIDAY_ALLSAINTDAY;
            case Armistice:
                return ToolkitProtos.CalendarEvent.HOLIDAY_ARMISTICE;
            case ThanksGiving:
                return ToolkitProtos.CalendarEvent.HOLIDAY_THANKSGIVING;
            case Christmas:
                return ToolkitProtos.CalendarEvent.HOLIDAY_CHRISTMAS;
            default:
                return null;
        }
    }

    public DayEvent convert(ToolkitProtos.CalendarEvent hol) {
        switch (hol) {
            case HOLIDAY_NEWYEAR:
                return DayEvent.NewYear;
            case HOLIDAY_SHROVEMONDAY:
                return DayEvent.ShroveMonday;
            case HOLIDAY_SHROVETUESDAY:
                return DayEvent.ShroveTuesday;
            case HOLIDAY_ASHWEDNESDAY:
                return DayEvent.AshWednesday;
            case HOLIDAY_EASTER:
                return DayEvent.Easter;
            case HOLIDAY_MAUNDYTHURSDAY:
                return DayEvent.MaundyThursday;
            case HOLIDAY_GOODFRIDAY:
                return DayEvent.GoodFriday;
            case HOLIDAY_EASTERMONDAY:
                return DayEvent.EasterMonday;
            case HOLIDAY_ASCENSION:
                return DayEvent.Ascension;
            case HOLIDAY_PENTECOST:
                return DayEvent.Pentecost;
            case HOLIDAY_CORPUSCHRISTI:
                return DayEvent.CorpusChristi;
            case HOLIDAY_WHITMONDAY:
                return DayEvent.WhitMonday;
            case HOLIDAY_MAYDAY:
                return DayEvent.MayDay;
            case HOLIDAY_ASSUMPTION:
                return DayEvent.Assumption;
            case HOLIDAY_LABORDAY:
                return DayEvent.LaborDay;
            case HOLIDAY_HALLOWEEN:
                return DayEvent.Halloween;
            case HOLIDAY_ALLSAINTDAY:
                return DayEvent.AllSaintsDay;
            case HOLIDAY_ARMISTICE:
                return DayEvent.Armistice;
            case HOLIDAY_THANKSGIVING:
                return DayEvent.ThanksGiving;
            case HOLIDAY_CHRISTMAS:
                return DayEvent.Christmas;
            default:
                return null;
        }
    }

    public ToolkitProtos.PrespecifiedHoliday convert(PrespecifiedHoliday ph) {

        ToolkitProtos.CalendarEvent ce;
        if (ph.isJulian()) {
            if (ph.getEvent() == DayEvent.Easter) {
                ce = ToolkitProtos.CalendarEvent.HOLIDAY_JULIANEASTER;
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            ce = convert(ph.getEvent());
        }

        return ToolkitProtos.PrespecifiedHoliday.newBuilder()
                .setWeight(ph.getWeight())
                .setValidity(convert(ph.getValidityPeriod()))
                .setEvent(ce)
                .build();
    }

    public PrespecifiedHoliday convert(ToolkitProtos.PrespecifiedHoliday ph) {
        DayEvent ce;
        boolean julian = false;
        if (ph.getEvent() == ToolkitProtos.CalendarEvent.HOLIDAY_JULIANEASTER) {
            ce = DayEvent.Easter;
            julian = true;
        } else {
            ce = convert(ph.getEvent());
        }
        return PrespecifiedHoliday.builder()
                .event(ce)
                .offset(ph.getOffset())
                .julian(julian)
                .weight(ph.getWeight())
                .validityPeriod(convert(ph.getValidity()))
                .build();
    }

    public ToolkitProtos.Calendar convert(Calendar calendar) {
        ToolkitProtos.Calendar.Builder builder = ToolkitProtos.Calendar.newBuilder();
        Holiday[] holidays = calendar.getHolidays();
        for (int i = 0; i < holidays.length; ++i) {
            if (holidays[i] instanceof FixedDay) {
                builder.addFixedDays(convert((FixedDay) holidays[i]));
            } else if (holidays[i] instanceof FixedWeekDay) {
                builder.addFixedWeekDays(convert((FixedWeekDay) holidays[i]));
            } else if (holidays[i] instanceof EasterRelatedDay) {
                builder.addEasterRelatedDays(convert((EasterRelatedDay) holidays[i]));
            } else if (holidays[i] instanceof PrespecifiedHoliday) {
                builder.addPrespecifiedHolidays(convert((PrespecifiedHoliday) holidays[i]));
            }
        }
        return builder.build();
    }

    public Calendar convert(ToolkitProtos.Calendar calendar) {
        List<Holiday> hol = new ArrayList<>();
        calendar.getFixedDaysList().forEach(fd -> {
            hol.add(convert(fd));
        });
        calendar.getFixedWeekDaysList().forEach(fd -> {
            hol.add(convert(fd));
        });
        calendar.getEasterRelatedDaysList().forEach(ed -> {
            hol.add(convert(ed));
        });
        calendar.getPrespecifiedHolidaysList().forEach(pd -> {
            hol.add(convert(pd));
        });
        return new Calendar(hol.toArray(new Holiday[hol.size()]));
    }

    public ToolkitProtos.CalendarDefinition convert(CalendarDefinition cd) {
        ToolkitProtos.CalendarDefinition.Builder builder = ToolkitProtos.CalendarDefinition.newBuilder();
        if (cd instanceof Calendar) {
            ToolkitProtos.Calendar cal = convert((Calendar) cd);
            builder.setCalendar(cal);
        } else {
            throw new UnsupportedOperationException();
        }
        return builder.build();
    }

    public CalendarDefinition convert(ToolkitProtos.CalendarDefinition cd) {
        ToolkitProtos.CalendarDefinition.Builder builder = ToolkitProtos.CalendarDefinition.newBuilder();
        if (builder.hasCalendar()) {
            return convert(builder.getCalendar());
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
