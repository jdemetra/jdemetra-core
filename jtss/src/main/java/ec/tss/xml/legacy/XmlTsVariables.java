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

package ec.tss.xml.legacy;

import ec.tss.DynamicTsVariable;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.IXmlMapper;
import ec.tss.xml.XmlConverterMapper;
import ec.tss.xml.XmlNamedObject;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.Arrays2;
import java.util.HashMap;
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
public class XmlTsVariables implements IXmlConverter<TsVariables> {

    static final String NAME = "tsVariablesType";
    static final String RNAME = "tsVariables";
    static final HashMap<Class<? extends XmlNamedObject>, IXmlMapper<? extends ITsVariable, ? extends XmlNamedObject>> fromXmlMap = new HashMap<>();
    static final HashMap<Class<? extends ITsVariable>, IXmlMapper<? extends ITsVariable, ? extends XmlNamedObject>> toXmlMap = new HashMap<>();

    static <S extends ITsVariable, X extends XmlNamedObject & IXmlConverter<S>> void register(Class<S> sclass, Class<X> xclass) {
        XmlConverterMapper<S, X> mapper = new XmlConverterMapper<>(xclass);
        fromXmlMap.put(xclass, mapper);
        toXmlMap.put(sclass, mapper);
    }

    static {
        register(TsVariable.class, XmlTsVariable.class);
        register(DynamicTsVariable.class, XmlDynamicTsVariable.class);
    }
    @XmlElements({
        @XmlElement(name = "staticTSVariable", type = XmlTsVariable.class),
        @XmlElement(name = "dynamicTSVariable", type = XmlDynamicTsVariable.class)
    })
    public XmlNamedObject[] vars;

    public boolean isEmpty() {
        return (vars == null || vars.length == 0);
    }

    @Override
    public TsVariables create() {
        TsVariables nvars = new TsVariables();
        if (!isEmpty()) {
            for (int i = 0; i < vars.length; ++i) {
                if (vars[i] != null) {
                    IXmlMapper mapper = fromXmlMap.get(vars[i].getClass());
                    if (mapper != null) {
                        ITsVariable v = (ITsVariable) mapper.fromXml(vars[i]);
                        nvars.set(vars[i].name, v);
                    }
                }
            }
        }

        return nvars;
    }

    @Override
    public void copy(TsVariables t) {
        String[] n = t.getNames();
        if (Arrays2.isNullOrEmpty(n)) {
            return;
        }
        vars = new XmlNamedObject[n.length];
        for (int i = 0; i < n.length; ++i) {
            ITsVariable v = t.get(n[i]);
            IXmlMapper mapper = toXmlMap.get(v.getClass());
            if (mapper != null) {
                vars[i] = (XmlNamedObject) mapper.toXml(v);
                vars[i].name = n[i];
            }
        }
    }
}
