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


package demetra.toolkit.io.xml.legacy.core;


import demetra.toolkit.io.xml.legacy.XmlDateAdapter;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Kristof Bayens
 */
@XmlType
public class XmlTimeSpan {
    @XmlElement
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    public LocalDate Start;
    
    @XmlElement
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    public LocalDate End;
    
}
