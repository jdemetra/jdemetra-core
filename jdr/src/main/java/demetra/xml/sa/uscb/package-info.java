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


@XmlSchema(namespace = "ec/tss.uscb", elementFormDefault = XmlNsForm.QUALIFIED,
attributeFormDefault = XmlNsForm.UNQUALIFIED, 
        xmlns =
{ 
    @XmlNs(prefix = "tss", namespaceURI = "ec/tss.core"),
    @XmlNs(prefix = "arima", namespaceURI = "ec/tss.arima"),
    @XmlNs(prefix = "sa", namespaceURI = "ec/tss.sa"),
    @XmlNs(prefix = "uscb", namespaceURI = "ec/tss.uscb")
})
package demetra.xml.sa.uscb;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

