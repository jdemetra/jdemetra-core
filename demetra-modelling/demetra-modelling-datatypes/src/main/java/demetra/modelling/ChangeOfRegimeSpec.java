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

package demetra.modelling;

import demetra.design.Development;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ChangeOfRegimeSpec implements Cloneable {

    public ChangeOfRegimeSpec(LocalDate date) {
        this.date = date;
        this.type = Type.Full;
    }

    public ChangeOfRegimeSpec(LocalDate day, Type type) {
        this.date = day;
        this.type = type;
    }

    /**
     * @return the day
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.date);
        hash = 71 * hash + Objects.hashCode(this.type);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ChangeOfRegimeSpec && equals((ChangeOfRegimeSpec) obj));
    }
    
    private boolean equals(ChangeOfRegimeSpec other) {
        return other.date.equals(date) && other.type == type;
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
        if (type == Type.Partial_ZeroStart)
            builder.append('/');
        builder.append(date);
        if (type == Type.Partial_ZeroEnd)
            builder.append('/');
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
        LocalDate day=LocalDate.parse(s.substring(beg, end));
        if (day == null)
            return null;
        else
            return new ChangeOfRegimeSpec(day, type);
    }
    
    private final LocalDate date;
    private final Type type;
    
    
    
}
