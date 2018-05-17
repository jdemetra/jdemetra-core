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

package demetra.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlStrings.NAME)
public class XmlStrings implements IXmlConverter<String[]>{
    
    private static final String[] EMPTY=new String[0];
    
    static final String NAME = "stringsType";
    
    @XmlElement
    public String items;

    @Override
    public String[] create() {
        return items == null ? EMPTY : items.split(" ");
    }

    @Override
    public void copy(String[] t) {
        if (t == null || t.length == 0)
            items=null;
        else{
            StringBuilder builder=new StringBuilder();
            builder.append(t[0]);
            for (int i=1; i<t.length; ++i){
                builder.append(" ").append(t[i]);
            }
            items=builder.toString();
        }
    }
}
