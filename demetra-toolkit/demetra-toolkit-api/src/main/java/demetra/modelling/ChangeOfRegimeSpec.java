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

import nbbrd.design.Development;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class ChangeOfRegimeSpec{

    public static enum Type {
        Full,
        Partial_ZeroStart,
        Partial_ZeroEnd
    }
    
    public ChangeOfRegimeSpec(LocalDate date) {
        this.date = date;
        this.type = Type.Full;
    }

    public ChangeOfRegimeSpec(LocalDate day, Type type) {
        this.date = day;
        this.type = type;
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
    
    private LocalDate date;
    private Type type;
    
    
    
}
