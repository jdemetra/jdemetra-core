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
package internal.workspace.file.xml.util;

import demetra.timeseries.DynamicTsDataSupplier;
import demetra.timeseries.TsData;
import demetra.timeseries.TsMoniker;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import demetra.toolkit.io.xml.legacy.core.XmlTsData;
import demetra.toolkit.io.xml.legacy.core.XmlTsMoniker;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlDynamicTsVariable.NAME)
public class XmlDynamicTsVariable extends XmlNamedObject implements IXmlConverter<DynamicTsDataSupplier> {

    static final String NAME = "dynamicTsVariableType";

    /**
     *
     */
    @XmlElement
    public XmlTsData tsdata;
    @XmlElement
    public XmlTsMoniker moniker;

    @Override
    public DynamicTsDataSupplier create() {
        TsMoniker m = XmlTsMoniker.getAdapter().unmarshal(moniker);
        DynamicTsDataSupplier result = (tsdata != null && tsdata.getValues() != null)
                ? new DynamicTsDataSupplier(m, XmlTsData.unmarshal(tsdata))
                : new DynamicTsDataSupplier(m, null);
        return result;
    }

    @Override
    public void copy(DynamicTsDataSupplier t) {
        moniker = XmlTsMoniker.getAdapter().marshal(t.getMoniker());
        TsData d = t.get();
        if (d != null) {
            tsdata=new XmlTsData();
            XmlTsData.marshal(d, tsdata);
        }
    }
}
