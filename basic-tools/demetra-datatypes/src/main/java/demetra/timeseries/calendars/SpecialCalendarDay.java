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

package demetra.timeseries.calendars;

import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsFrequency;
import java.time.LocalDate;


/**
 *
 * @author Jean Palate
 */
public class SpecialCalendarDay implements ISpecialDay {

    public final DayEvent event;
    public final int offset;
    private final double weight;
    private final boolean julian;

    public SpecialCalendarDay(DayEvent ev, int off) {
        this(ev, off, 1);
    }

    public SpecialCalendarDay(DayEvent ev, int off, double weight) {
        this(ev, off, weight, false);
    }
 
    public SpecialCalendarDay(DayEvent ev, int off, boolean julianeaster) {
        this(ev, off, 1, julianeaster);
    }

    public SpecialCalendarDay(DayEvent ev, int off, double weight, boolean julianeaster) {
        event = ev;
        offset = off;
        this.weight = weight;
        this.julian=julianeaster;
    }
    
    public ISpecialDay toSpecialDay() {
        switch (event) {
            case ShroveMonday:
                return julian ? EasterRelatedDay.JULIAN_SHROVEMONDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.SHROVEMONDAY.reweight(weight).plus(offset);
            case ShroveTuesday:
                return julian ? EasterRelatedDay.JULIAN_SHROVETUESDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.SHROVETUESDAY.reweight(weight).plus(offset);
            case AshWednesday:
                return julian ? EasterRelatedDay.JULIAN_ASHWEDNESDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.ASHWEDNESDAY.reweight(weight).plus(offset);
            case Easter:
                return julian ? EasterRelatedDay.JULIAN_EASTER.reweight(weight).plus(offset)
                        : EasterRelatedDay.EASTER.reweight(weight).plus(offset);
            case MaundyThursday:
                return julian ? EasterRelatedDay.JULIAN_EASTERTHURSDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.EASTERTHURSDAY.reweight(weight).plus(offset);
            case GoodFriday:
                return julian ? EasterRelatedDay.JULIAN_EASTERFRIDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.EASTERFRIDAY.reweight(weight).plus(offset);
            case EasterMonday:
                return julian ? EasterRelatedDay.JULIAN_EASTERMONDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.EASTERMONDAY.reweight(weight).plus(offset);
            case Ascension:
                return julian ? EasterRelatedDay.JULIAN_ASCENSION.reweight(weight).plus(offset)
                        : EasterRelatedDay.ASCENSION.reweight(weight).plus(offset);
            case Pentecost:
                return julian ? EasterRelatedDay.JULIAN_PENTECOST.reweight(weight).plus(offset)
                        : EasterRelatedDay.PENTECOST.reweight(weight).plus(offset);
            case WhitMonday:
                return julian ? EasterRelatedDay.JULIAN_PENTECOSTMONDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.PENTECOSTMONDAY.reweight(weight).plus(offset);
            case CorpusChristi:
                return julian ? EasterRelatedDay.JULIAN_CORPUSCHRISTI.reweight(weight).plus(offset)
                        : EasterRelatedDay.CORPUSCHRISTI.reweight(weight).plus(offset);
            case Assumption:
                return FixedDay.ASSUMPTION.reweight(weight).plus(offset);
            case Christmas:
                return FixedDay.CHRISTMAS.reweight(weight).plus(offset);
            case NewYear:
                return FixedDay.NEWYEAR.reweight(weight).plus(offset);
            case MayDay:
                return FixedDay.MAYDAY.reweight(weight).plus(offset);
            case AllSaintsDay:
                return FixedDay.ALLSAINTSDAY.reweight(weight).plus(offset);
            case Halloween:
                return FixedDay.HALLOWEEN.reweight(weight).plus(offset);

//            case LaborDay:
//                return FixedWeekDay.LaborDay.plus(offset);
//            case ThanksGiving:
//                return FixedWeekDay.ThanksGiving.plus(Offset);

            default:
                return null;
        }
    }
    
    public boolean isJulianEaster(){
        return julian;
    }
    
    @Override
    public Iterable<IDayInfo> getIterable(TsFrequency freq, LocalDate start, LocalDate end) {
        ISpecialDay sd = toSpecialDay();
        if (sd == null) {
            return null;
        } else {
            return sd.getIterable(freq, start, end);
        }
    }

    @Override
    public double[][] getLongTermMeanEffect(int freq) {
        ISpecialDay sd = toSpecialDay();
        if (sd == null) {
            return null;
        } else {
            return sd.getLongTermMeanEffect(freq);
        }
    }

    @Override
    public RegularDomain getSignificantDomain(RegularDomain domain) {
        ISpecialDay sd = toSpecialDay();
        if (sd == null) {
            return null;
        } else {
            return sd.getSignificantDomain(domain);
        }
    }

    @Override
    public double getWeight() {
        return weight;
    }
    
        @Override
    public boolean match(Context context){
        return context.isJulianEaster() == julian;
    }
    

}
