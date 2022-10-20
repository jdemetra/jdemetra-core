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
import demetra.timeseries.calendars.ChainedCalendar;
import demetra.timeseries.calendars.CompositeCalendar;
import demetra.timeseries.calendars.DayEvent;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.FixedWeekDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import demetra.timeseries.calendars.SingleDate;
import demetra.util.WeightedItem;
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

    public ToolkitProtos.SingleDate convert(@NonNull SingleDate sd) {
        return ToolkitProtos.SingleDate.newBuilder()
                .setDate(ToolkitProtosUtility.convert(sd.getDate()))
                .setWeight(sd.getWeight())
                .build();
    }

    public SingleDate convert(ToolkitProtos.SingleDate sd) {
        return new SingleDate(ToolkitProtosUtility.convert(sd.getDate()), sd.getWeight());
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
        return new FixedWeekDay(ed.getMonth(), ed.getPosition(), DayOfWeek.of(ed.getWeekday()), ed.getWeight(), convert(ed.getValidity()));
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
        return switch (hol) {
            case NewYear ->
                ToolkitProtos.CalendarEvent.HOLIDAY_NEWYEAR;
            case ShroveMonday ->
                ToolkitProtos.CalendarEvent.HOLIDAY_SHROVEMONDAY;
            case ShroveTuesday ->
                ToolkitProtos.CalendarEvent.HOLIDAY_SHROVETUESDAY;
            case AshWednesday ->
                ToolkitProtos.CalendarEvent.HOLIDAY_ASHWEDNESDAY;
            case Easter ->
                ToolkitProtos.CalendarEvent.HOLIDAY_EASTER;
            case MaundyThursday ->
                ToolkitProtos.CalendarEvent.HOLIDAY_MAUNDYTHURSDAY;
            case GoodFriday ->
                ToolkitProtos.CalendarEvent.HOLIDAY_GOODFRIDAY;
            case EasterMonday ->
                ToolkitProtos.CalendarEvent.HOLIDAY_EASTERMONDAY;
            case Ascension ->
                ToolkitProtos.CalendarEvent.HOLIDAY_ASCENSION;
            case Pentecost ->
                ToolkitProtos.CalendarEvent.HOLIDAY_PENTECOST;
            case CorpusChristi ->
                ToolkitProtos.CalendarEvent.HOLIDAY_CORPUSCHRISTI;
            case WhitMonday ->
                ToolkitProtos.CalendarEvent.HOLIDAY_WHITMONDAY;
            case MayDay ->
                ToolkitProtos.CalendarEvent.HOLIDAY_MAYDAY;
            case Assumption ->
                ToolkitProtos.CalendarEvent.HOLIDAY_ASSUMPTION;
            case LaborDay ->
                ToolkitProtos.CalendarEvent.HOLIDAY_LABORDAY;
            case Halloween ->
                ToolkitProtos.CalendarEvent.HOLIDAY_HALLOWEEN;
            case AllSaintsDay ->
                ToolkitProtos.CalendarEvent.HOLIDAY_ALLSAINTSDAY;
            case Armistice ->
                ToolkitProtos.CalendarEvent.HOLIDAY_ARMISTICE;
            case ThanksGiving ->
                ToolkitProtos.CalendarEvent.HOLIDAY_THANKSGIVING;
            case Christmas ->
                ToolkitProtos.CalendarEvent.HOLIDAY_CHRISTMAS;
            default ->
                null;
        };
    }

    public DayEvent convert(ToolkitProtos.CalendarEvent hol) {
        return switch (hol) {
            case HOLIDAY_NEWYEAR ->
                DayEvent.NewYear;
            case HOLIDAY_SHROVEMONDAY ->
                DayEvent.ShroveMonday;
            case HOLIDAY_SHROVETUESDAY ->
                DayEvent.ShroveTuesday;
            case HOLIDAY_ASHWEDNESDAY ->
                DayEvent.AshWednesday;
            case HOLIDAY_EASTER ->
                DayEvent.Easter;
            case HOLIDAY_MAUNDYTHURSDAY ->
                DayEvent.MaundyThursday;
            case HOLIDAY_GOODFRIDAY ->
                DayEvent.GoodFriday;
            case HOLIDAY_EASTERMONDAY ->
                DayEvent.EasterMonday;
            case HOLIDAY_ASCENSION ->
                DayEvent.Ascension;
            case HOLIDAY_PENTECOST ->
                DayEvent.Pentecost;
            case HOLIDAY_CORPUSCHRISTI ->
                DayEvent.CorpusChristi;
            case HOLIDAY_WHITMONDAY ->
                DayEvent.WhitMonday;
            case HOLIDAY_MAYDAY ->
                DayEvent.MayDay;
            case HOLIDAY_ASSUMPTION ->
                DayEvent.Assumption;
            case HOLIDAY_LABORDAY ->
                DayEvent.LaborDay;
            case HOLIDAY_HALLOWEEN ->
                DayEvent.Halloween;
            case HOLIDAY_ALLSAINTSDAY ->
                DayEvent.AllSaintsDay;
            case HOLIDAY_ARMISTICE ->
                DayEvent.Armistice;
            case HOLIDAY_THANKSGIVING ->
                DayEvent.ThanksGiving;
            case HOLIDAY_CHRISTMAS ->
                DayEvent.Christmas;
            default ->
                null;
        };
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
            if (holidays[i] instanceof FixedDay fixedDay) {
                builder.addFixedDays(convert(fixedDay));
            } else if (holidays[i] instanceof FixedWeekDay fixedWeekDay) {
                builder.addFixedWeekDays(convert(fixedWeekDay));
            } else if (holidays[i] instanceof EasterRelatedDay easterRelatedDay) {
                builder.addEasterRelatedDays(convert(easterRelatedDay));
            } else if (holidays[i] instanceof PrespecifiedHoliday prespecifiedHoliday) {
                builder.addPrespecifiedHolidays(convert(prespecifiedHoliday));
            } else if (holidays[i] instanceof SingleDate singleDate) {
                builder.addSingleDates(convert(singleDate));
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
        calendar.getSingleDatesList().forEach(sd -> {
            hol.add(convert(sd));
        });
        return new Calendar(hol.toArray(Holiday[]::new));
    }

    public ToolkitProtos.ChainedCalendar convert(ChainedCalendar cc) {
        ToolkitProtos.ChainedCalendar.Builder builder = ToolkitProtos.ChainedCalendar.newBuilder();
        return builder.setCalendar1(cc.getFirst())
                .setCalendar2(cc.getSecond())
                .setBreakDate(ToolkitProtosUtility.convert(cc.getBreakDate()))
                .build();
    }

    public ToolkitProtos.WeightedCalendar convert(CompositeCalendar cc) {
        ToolkitProtos.WeightedCalendar.Builder builder = ToolkitProtos.WeightedCalendar.newBuilder();
        for (WeightedItem<String> item : cc.getCalendars()) {
            builder.addItems(ToolkitProtos.WeightedCalendar.Item.newBuilder()
                    .setWeight(item.getWeight())
                    .setCalendar(item.getItem())
                    .build()
            );
        }
        return builder.build();
    }
    
    public ChainedCalendar convert(ToolkitProtos.ChainedCalendar cc) {
        return new ChainedCalendar(cc.getCalendar1(), cc.getCalendar2(), ToolkitProtosUtility.convert(cc.getBreakDate()));
    }
    
    public CompositeCalendar convert(ToolkitProtos.WeightedCalendar cc) {
        WeightedItem[] items = cc.getItemsList().stream()
                .map(item->new WeightedItem<String>(item.getCalendar(), item.getWeight()))
                .toArray(WeightedItem[]::new );
        return new CompositeCalendar(items);
    }
    
    public ToolkitProtos.CalendarDefinition convert(CalendarDefinition cd) {
        ToolkitProtos.CalendarDefinition.Builder builder = ToolkitProtos.CalendarDefinition.newBuilder();
        if (cd instanceof Calendar calendar) {
            ToolkitProtos.Calendar cal = convert(calendar);
            builder.setCalendar(cal);
        } else if (cd instanceof ChainedCalendar calendar) {
            ToolkitProtos.ChainedCalendar cal = convert(calendar);
            builder.setChainedCalendar(cal);
        } else if (cd instanceof CompositeCalendar calendar) {
            ToolkitProtos.WeightedCalendar cal = convert(calendar);
            builder.setWeightedCalendar(cal);
        } else {
            throw new UnsupportedOperationException();
        }
        return builder.build();
    }

    public CalendarDefinition convert(ToolkitProtos.CalendarDefinition cd) {
        if (cd.hasCalendar()) {
            return convert(cd.getCalendar());
        } else if (cd.hasChainedCalendar()){
            return convert(cd.getChainedCalendar());
        } else if (cd.hasWeightedCalendar()){
            return convert(cd.getWeightedCalendar());
        }else{   
            throw new UnsupportedOperationException();
        }
    }
}
