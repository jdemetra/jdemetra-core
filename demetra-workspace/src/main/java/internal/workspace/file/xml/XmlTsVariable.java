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
package internal.workspace.file.xml;

import demetra.timeseries.StaticTsDataSupplier;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import demetra.toolkit.io.xml.legacy.core.XmlTsData;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Kristof Bayens
 */
public class XmlTsVariable extends XmlNamedObject implements IXmlConverter<StaticTsDataSupplier> {

    static final String NAME = "TsVariableType";
    @XmlElement
    public XmlTsData tsdata;

    public XmlTsVariable() {
    }

    @Override
    public StaticTsDataSupplier create() {
        if (tsdata == null) {
            return null;
        }
        return new StaticTsDataSupplier(XmlTsData.unmarshal(tsdata));
     }

    @Override
    public void copy(StaticTsDataSupplier t) {
        tsdata = new XmlTsData();
        XmlTsData.marshal(t.getData(), tsdata);
    }
}
