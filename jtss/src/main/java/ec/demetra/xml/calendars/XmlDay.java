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


package ec.demetra.xml.calendars;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DayType")
@XmlSeeAlso({XmlFixedDay.class, XmlFixedWeekDay.class, XmlEasterRelatedDay.class, XmlSpecialCalendarDay.class}) 
public abstract class XmlDay {
    
    @XmlElement(name = "weight")
    protected Double weight;
    
    public double getWeight(){
        return weight == null ? 1 : weight;
    }
    
    public void setWeight(double w){
        if (w == 1)
            weight=null;
        else
            weight=w;
    }

}
