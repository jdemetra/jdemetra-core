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

package ec.tss.xml;

import ec.tstoolkit.information.StatisticalTest;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlStatisticalTest.NAME)
public class XmlStatisticalTest implements IXmlConverter<StatisticalTest> {

    static final String NAME = "statisticalTestType";
    @XmlElement
    public String description;
    @XmlElement
    public double value;
    @XmlElement
    public double pvalue;

    public StatisticalTest create() {
        return new StatisticalTest(description, value, pvalue);
    }

    public void copy(StatisticalTest t) {
        description = t.description;
        value = t.value;
        pvalue = t.pvalue;
    }
}
