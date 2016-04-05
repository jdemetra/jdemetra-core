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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.StringFormatter;
import java.util.Objects;

/**
 *
 * @author Palate Jean
 * @author Demortier Jeremy
 */
public class Sequence implements Cloneable {

    @Override
    public String toString() {
        if (start.equals(end))
            return start.toString();
        StringBuilder builder = new StringBuilder();
        builder.append(StringFormatter.convert(start)).append(InformationSet.SEP).append(StringFormatter.convert(end));
        return builder.toString();
    }
    
    public String toString(TsFrequency freq){
        TsPeriod pstart=new TsPeriod(freq, start);
        TsPeriod pend=new TsPeriod(freq, end);
        if (pend.equals(pstart))
            return pstart.toString();
        StringBuilder builder = new StringBuilder();
        builder.append(pstart).append(InformationSet.SEP).append(pend);
        return builder.toString();
    }
    
    public Day start;
    public Day end;

    public Sequence() {
        end = start = Day.toDay();
    }

    public Sequence(Day s, Day e) {
        start = s;
        end = e;
    }

    public static Sequence fromString(String s){
        String[] ss=InformationSet.split(s);
        if (ss.length != 2)
            return null;
        Day start=StringFormatter.convertDay(ss[0]);
        Day end=StringFormatter.convertDay(ss[1]);
        if (start != null && end != null)
            return new Sequence(start, end);
        else
            return null;
    }
    
    @Override
    public Sequence clone() {
        try {
            return (Sequence) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public Day getEnd() {
        return end;
    }

    public Day getStart() {
        return start;
    }

    public void setEnd(Day end) {
        this.end = end;
    }

    public void setStart(Day start) {
        this.start = start;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Sequence && equals((Sequence) obj));
    }
    
    private boolean equals(Sequence other) {
        return Objects.equals(start, other.start) && Objects.equals(end, other.end);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.start);
        hash = 53 * hash + Objects.hashCode(this.end);
        return hash;
    }
}
