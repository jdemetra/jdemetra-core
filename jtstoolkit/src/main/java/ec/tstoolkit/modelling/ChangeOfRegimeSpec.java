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

package ec.tstoolkit.modelling;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.utilities.StringFormatter;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ChangeOfRegimeSpec implements Cloneable {

    public ChangeOfRegimeSpec(Day day) {
        this.day_ = day;
        this.type_ = Type.Full;
    }

    public ChangeOfRegimeSpec(Day day, Type type) {
        this.day_ = day;
        this.type_ = type;
    }

    @Override
    public ChangeOfRegimeSpec clone() {
        try {
            ChangeOfRegimeSpec spec = (ChangeOfRegimeSpec) super.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     * @return the day
     */
    public Day getDate() {
        return day_;
    }

    /**
     * @param day the day to set
     */
    public void setDate(Day date) {
        this.day_ = date;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type_;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type_ = type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.day_);
        hash = 71 * hash + Objects.hashCode(this.type_);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ChangeOfRegimeSpec && equals((ChangeOfRegimeSpec) obj));
    }
    
    private boolean equals(ChangeOfRegimeSpec other) {
        return other.day_.equals(day_) && other.type_ == type_;
    }

    public static enum Type {
        Full,
        Partial_ZeroStart,
        Partial_ZeroEnd
    }
    
    /**
     * Convention of the US-Census Bureau
     * @return 
     */
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append('/');
        if (type_ == Type.Partial_ZeroStart)
            builder.append('/');
        builder.append(StringFormatter.convert(day_));
        if (type_ == Type.Partial_ZeroEnd)
            builder.append('/');
        return builder.toString();        
    }
    
    public static ChangeOfRegimeSpec fromString(String s){
        int beg=0, end=s.length();
        if (end < 3 || s.charAt(beg) != '/' || s.charAt(end-1) != '/')
            return null;
        ++beg;
        --end;
        Type type;
        if (s.charAt(beg) == '/'){
            ++beg;
            type=Type.Partial_ZeroStart;
        }
        else if (s.charAt(end-1) == '/'){
            --end;
            type=Type.Partial_ZeroEnd;
        }
        else
            type= Type.Full;
        Day day=StringFormatter.convertDay(s.substring(beg, end));
        if (day == null)
            return null;
        else
            return new ChangeOfRegimeSpec(day, type);
    }
    
    private Day day_;
    private Type type_;
    
    
    
}
