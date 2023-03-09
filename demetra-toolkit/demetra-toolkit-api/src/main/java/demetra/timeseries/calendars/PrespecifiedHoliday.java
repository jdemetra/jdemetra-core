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

import nbbrd.design.Development;
import demetra.timeseries.ValidityPeriod;
import java.time.LocalDate;


/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Beta)
public class PrespecifiedHoliday implements Holiday {

    @lombok.NonNull
    private DayEvent event;
    private int offset;
    private double weight;
    private ValidityPeriod validityPeriod;
    private boolean julian;
    
    public static Builder builder(){
        return new Builder()
                .validityPeriod(ValidityPeriod.ALWAYS)
                .weight(1);
    }
    
    public Holiday rawHoliday() {
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
                return julian ? EasterRelatedDay.JULIAN_WHITMONDAY.reweight(weight).plus(offset)
                        : EasterRelatedDay.WHITMONDAY.reweight(weight).plus(offset);
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
            case Armistice:
                return FixedDay.ARMISTICE.reweight(weight).plus(offset);
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

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public PrespecifiedHoliday reweight(double newWeight) {
        return new PrespecifiedHoliday(event, offset, newWeight, validityPeriod, julian);
    }
    
    @Override
    public PrespecifiedHoliday forPeriod(LocalDate start, LocalDate end) {
        if (validityPeriod.getStart().equals(start) && validityPeriod.getEnd().equals(end)) {
            return this;
        } else {
            return new PrespecifiedHoliday(event, offset, weight, ValidityPeriod.between(start, end), julian);
        }
    }

     @Override
    public String display() {
        StringBuilder builder = new StringBuilder();
        builder.append(event.name());
        if (offset<0)
            builder.append(offset);
        else if (offset>0)
            builder.append('+').append(offset);
        return builder.toString();
    }
   
}
