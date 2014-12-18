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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class SpecialCalendarDay implements ISpecialDay {

    public final DayEvent event;
    public final int offset;
    private final double weight;

    public SpecialCalendarDay(DayEvent ev, int off) {
        this(ev, off, 1);
    }

    public SpecialCalendarDay(DayEvent ev, int off, double weight) {
        event = ev;
        offset = off;
        this.weight = weight;
    }
 
    public ISpecialDay toSpecialDay() {
        switch (event) {
            case AshWednesday:
                return EasterRelatedDay.AshWednesday.reweight(weight).plus(offset);
            case Easter:
                return new EasterRelatedDay(offset, weight);
            case MaundyThursday:
                return EasterRelatedDay.EasterThursday.reweight(weight).plus(offset);
            case GoodFriday:
                return EasterRelatedDay.EasterFriday.reweight(weight).plus(offset);
            case EasterMonday:
                return EasterRelatedDay.EasterMonday.reweight(weight).plus(offset);
            case Ascension:
                return EasterRelatedDay.Ascension.reweight(weight).plus(offset);
            case Pentecost:
                return EasterRelatedDay.Pentecost.reweight(weight).plus(offset);
            case WhitMonday:
                return EasterRelatedDay.PentecostMonday.reweight(weight).plus(offset);
            case Assumption:
                return FixedDay.Assumption.reweight(weight).plus(offset);
            case Christmas:
                return FixedDay.Christmas.reweight(weight).plus(offset);
            case NewYear:
                return FixedDay.NewYear.reweight(weight).plus(offset);
            case MayDay:
                return FixedDay.MayDay.reweight(weight).plus(offset);
            case AllSaintsDay:
                return FixedDay.AllSaintsDay.reweight(weight).plus(offset);
            case Halloween:
                return FixedDay.Halloween.reweight(weight).plus(offset);

//            case LaborDay:
//                return FixedWeekDay.LaborDay.plus(offset);
//            case ThanksGiving:
//                return FixedWeekDay.ThanksGiving.plus(Offset);

            default:
                return null;
        }
    }

    @Override
    public Iterable<IDayInfo> getIterable(TsFrequency freq, Day start, Day end) {
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
    public TsDomain getSignificantDomain(TsFrequency freq, Day start, Day end) {
        ISpecialDay sd = toSpecialDay();
        if (sd == null) {
            return null;
        } else {
            return sd.getSignificantDomain(freq, start, end);
        }
    }

    @Override
    public double getWeight() {
        return weight;
    }
}
