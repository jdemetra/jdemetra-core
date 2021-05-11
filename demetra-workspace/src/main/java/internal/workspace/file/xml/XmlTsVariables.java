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

import demetra.timeseries.DynamicTsDataSupplier;
import demetra.timeseries.StaticTsDataSupplier;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.regression.TsDataSuppliers;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlTsVariables.RNAME)
@XmlType(name = XmlTsVariables.NAME)
public class XmlTsVariables implements IXmlConverter<TsDataSuppliers> {

    static final String NAME = "tsVariablesType";
    static final String RNAME = "tsVariables";
    @XmlElements({
        @XmlElement(name = "tsVariable", type = XmlTsVariable.class),
        @XmlElement(name = "dynamicTsVariable", type = XmlDynamicTsVariable.class)
    })
    public XmlNamedObject[] vars;

    @XmlAttribute
    public String name;

    public boolean isEmpty() {
        return (vars == null || vars.length == 0);
    }

    @Override
    public TsDataSuppliers create() {
        TsDataSuppliers nvars = new TsDataSuppliers();
        if (!isEmpty()) {
            for (int i = 0; i < vars.length; ++i) {
                if (vars[i] != null) {
                    if (vars[i] instanceof XmlTsVariable) {
                        XmlTsVariable v = (XmlTsVariable) vars[i];
                        nvars.set(v.name, v.create());
                    } else if (vars[i] instanceof XmlDynamicTsVariable) {
                        XmlDynamicTsVariable v = (XmlDynamicTsVariable) vars[i];
                        nvars.set(v.name, v.create());
                    }
                }
            }
        }

        return nvars;
    }

    @Override
    public void copy(TsDataSuppliers t) {
        String[] n = t.getNames();
        if (n == null || n.length == 0) {
            return;
        }
        vars = new XmlNamedObject[n.length];
        for (int i = 0; i < n.length; ++i) {
            TsDataSupplier v = t.get(n[i]);
            if (v instanceof StaticTsDataSupplier) {
                XmlTsVariable var = new XmlTsVariable();
                var.name = n[i];
                var.copy((StaticTsDataSupplier) v);
                vars[i] = var;
            } else if (v instanceof DynamicTsDataSupplier) {
                XmlDynamicTsVariable var = new XmlDynamicTsVariable();
                var.name = n[i];
                var.copy((DynamicTsDataSupplier) v);
                vars[i] = var;
            }
        }
    }
}
