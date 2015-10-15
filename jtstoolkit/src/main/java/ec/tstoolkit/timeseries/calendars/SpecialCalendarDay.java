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
    private final boolean julianeaster;

    public SpecialCalendarDay(DayEvent ev, int off) {
        this(ev, off, 1);
    }

    public SpecialCalendarDay(DayEvent ev, int off, double weight) {
        event = ev;
        offset = off;
        this.weight = weight;
        this.julianeaster=false;
    }
 
    public SpecialCalendarDay(DayEvent ev, int off, boolean julianeaster) {
        this(ev, off, 1, julianeaster);
    }

    public SpecialCalendarDay(DayEvent ev, int off, double weight, boolean julianeaster) {
        event = ev;
        offset = off;
        this.weight = weight;
        this.julianeaster=julianeaster;
    }
    
    public ISpecialDay toSpecialDay() {
        switch (event) {
            case ShroveMonday:
                return julianeaster ? EasterRelatedDay.JulianShroveMonday.reweight(weight).plus(offset)
                        : EasterRelatedDay.ShroveMonday.reweight(weight).plus(offset);
            case ShroveTuesday:
                return julianeaster ? EasterRelatedDay.JulianShroveTuesday.reweight(weight).plus(offset)
                        : EasterRelatedDay.ShroveTuesday.reweight(weight).plus(offset);
            case AshWednesday:
                return julianeaster ? EasterRelatedDay.JulianAshWednesday.reweight(weight).plus(offset)
                        : EasterRelatedDay.AshWednesday.reweight(weight).plus(offset);
            case Easter:
                return julianeaster ? EasterRelatedDay.JulianEaster.reweight(weight).plus(offset)
                        : EasterRelatedDay.Easter.reweight(weight).plus(offset);
            case MaundyThursday:
                return julianeaster ? EasterRelatedDay.JulianEasterThursday.reweight(weight).plus(offset)
                        : EasterRelatedDay.EasterThursday.reweight(weight).plus(offset);
            case GoodFriday:
                return julianeaster ? EasterRelatedDay.JulianEasterFriday.reweight(weight).plus(offset)
                        : EasterRelatedDay.EasterFriday.reweight(weight).plus(offset);
            case EasterMonday:
                return julianeaster ? EasterRelatedDay.JulianEasterMonday.reweight(weight).plus(offset)
                        : EasterRelatedDay.EasterMonday.reweight(weight).plus(offset);
            case Ascension:
                return julianeaster ? EasterRelatedDay.JulianAscension.reweight(weight).plus(offset)
                        : EasterRelatedDay.Ascension.reweight(weight).plus(offset);
            case Pentecost:
                return julianeaster ? EasterRelatedDay.JulianPentecost.reweight(weight).plus(offset)
                        : EasterRelatedDay.Pentecost.reweight(weight).plus(offset);
            case WhitMonday:
                return julianeaster ? EasterRelatedDay.JulianPentecostMonday.reweight(weight).plus(offset)
                        : EasterRelatedDay.PentecostMonday.reweight(weight).plus(offset);
            case CorpusChristi:
                return julianeaster ? EasterRelatedDay.JulianCorpusChristi.reweight(weight).plus(offset)
                        : EasterRelatedDay.CorpusChristi.reweight(weight).plus(offset);
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
    
    public boolean isJulianEaster(){
        return julianeaster;
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
