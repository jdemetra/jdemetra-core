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

package ec.tss.tsproviders.common.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ec.tss.TsCollection;

@XmlRootElement(name = "tscollection")
@XmlType(name = "tscollection")
class wsTsCollection {

    @XmlAttribute
    String name;
    @XmlElement(name = "ts")
    @XmlElementWrapper(name = "data")
    wsTs[] tslist;

    void copy(TsCollection c) {
	name = c.getName();
	tslist = new wsTs[c.getCount()];
	for (int i = 0; i < tslist.length; ++i) {
	    tslist[i] = new wsTs();
	    tslist[i].copy(c.get(i));
	}
    }
}
