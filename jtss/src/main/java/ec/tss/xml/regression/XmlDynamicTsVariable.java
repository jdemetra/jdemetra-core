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
package ec.tss.xml.regression;

import ec.tss.DynamicTsVariable;
import ec.tss.TsMoniker;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlNamedObject;
import ec.tss.xml.XmlTsData;
import ec.tss.xml.XmlTsMoniker;
import ec.tstoolkit.timeseries.simplets.TsData;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlDynamicTsVariable.NAME)
public class XmlDynamicTsVariable extends XmlNamedObject implements IXmlConverter<DynamicTsVariable> {

    static final String NAME = "dynamicTsVariableType";
    @XmlElement
    public XmlTsData tsdata;
    @XmlElement
    public XmlTsMoniker moniker;

    @Override
    public DynamicTsVariable create() {
        TsMoniker m = moniker.create();
        DynamicTsVariable result = tsdata != null
                ? new DynamicTsVariable(tsdata.name, m, tsdata.create())
                : new DynamicTsVariable(name, m, null);
        result.setName(name);
        return result;
    }

    @Override
    public void copy(DynamicTsVariable t) {
        moniker = new XmlTsMoniker();
        moniker.copy(t.getMoniker());
        TsData d = t.getTsData();
        if (d != null) {
            tsdata = new XmlTsData();
            tsdata.copy(d);
            tsdata.name = t.getDescription();
        }
    }
}
