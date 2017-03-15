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

import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlNamedObject;
import ec.tss.xml.XmlTsData;
import ec.tstoolkit.timeseries.regression.TsVariable;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Kristof Bayens
 */
public class XmlTsVariable extends XmlNamedObject implements IXmlConverter<TsVariable> {

    static final String NAME = "TsVariableType";
    @XmlElement
    public XmlTsData tsdata;

    public XmlTsVariable() {
    }

    @Override
    public TsVariable create() {
        if (tsdata == null) {
            return null;
        }
        TsVariable result = new TsVariable(tsdata.name, tsdata.create());
        result.setName(name);
        return result;
    }

    @Override
    public void copy(TsVariable t) {
        tsdata = new XmlTsData();
        tsdata.copy(t.getTsData());
        tsdata.name = t.getDescription();
    }
}
