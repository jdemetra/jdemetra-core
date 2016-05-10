/*
 * Copyright 2016 National Bank of Belgium
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

import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Jean Palate
 */
public abstract class XmlVariable {

    private static final List<TsVariableAdapter> adapters = new ArrayList<>();

    public static void register(final TsVariableAdapter... nadapters) {
        synchronized (adapters) {
            for (int i = 0; i < nadapters.length; ++i) {
                final TsVariableAdapter cur = nadapters[i];
                Optional<TsVariableAdapter> first = adapters.stream().filter(
                        (TsVariableAdapter z) -> z.getValueType().equals(cur.getValueType())).findFirst();
                if (first.isPresent()) {
                    adapters.remove(first.get());
                }
                adapters.add(cur);
            }
        }
    }

    public static ITsVariable unmarshal(XmlVariable var) throws Exception {
        synchronized (adapters) {
            Optional<TsVariableAdapter> first = adapters.stream().
                    filter((TsVariableAdapter z) -> z.getXmlType().isInstance(var)).findFirst();
            if (first.isPresent()) {
                return (ITsVariable) first.get().unmarshal(var);
            } else {
                return null;
            }
        }
    }

    public static Class[] getValueTypes() {
        synchronized (adapters) {
            return adapters.stream().map(x->x.getValueType()).toArray(n->new Class[n]);
        }
    }

    public static XmlVariable marshal(ITsVariable var) throws Exception {
        synchronized (adapters) {
            Optional<TsVariableAdapter> first = adapters.stream().
                    filter((TsVariableAdapter z) -> z.getValueType().isInstance(var)).findFirst();
            if (first.isPresent()) {
                return (XmlVariable) first.get().marshal(var);
            } else {
                return null;
            }
        }
    }

    @XmlAttribute
    public String name;

    @XmlAttribute
    public ComponentType effect = ComponentType.Undefined;

}
