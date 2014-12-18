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

/**
 *
 * @author PCuser
 */
import ec.tss.TsWorkspace;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "tsworkspace")
@XmlType(name = "tsworkspace")
public class wsTsWorkspace {

    static wsTsWorkspace from(TsWorkspace t) {
        wsTsWorkspace result = new wsTsWorkspace();
        int n = t.getCount();
        if (n == 0) {
            result.tsclist = null;
        } else {
            result.tsclist = new wsTsCollection[n];
            for (int i = 0; i < n; ++i) {
                wsTsCollection c = new wsTsCollection();
                c.copy(t.get(i));
                result.tsclist[i] = c;
            }
        }
        return result;
    }
    @XmlElement(name = "tscollection")
    @XmlElementWrapper(name = "timeseries")
    wsTsCollection[] tsclist;
}
