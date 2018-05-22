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


@XmlSchema(namespace = "ec/tss.sa", 
        elementFormDefault = XmlNsForm.QUALIFIED, 
        attributeFormDefault = XmlNsForm.UNQUALIFIED, 
        location="",
        xmlns =
{ 
    @XmlNs(prefix = "sa", namespaceURI = "ec/tss.sa"),
    @XmlNs(prefix = "tss", namespaceURI = "ec/tss.core"),
    @XmlNs(prefix = "arima", namespaceURI = "ec/tss.arima"),
    @XmlNs(prefix = "uscb", namespaceURI = "ec/tss.uscb"),
    @XmlNs(prefix = "x12", namespaceURI = "ec/tss.x12"),
    @XmlNs(prefix = "x13", namespaceURI = "ec/tss.x13"),
    @XmlNs(prefix = "trs", namespaceURI = "ec/tss.tramoseats")
})
package demetra.xml.sa;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

