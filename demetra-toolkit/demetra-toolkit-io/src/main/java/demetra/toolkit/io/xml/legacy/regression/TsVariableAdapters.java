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
package demetra.toolkit.io.xml.legacy.regression;

import demetra.timeseries.regression.ITsVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsVariableAdapters {

    private final AtomicReference<TsVariableAdapterLoader> ADAPTERS = new AtomicReference<>(new TsVariableAdapterLoader());

    private List<TsVariableAdapter> adapters() {
        return ADAPTERS.get().get();
    }

    public void reload() {
        ADAPTERS.set(new TsVariableAdapterLoader());
    }

    public List<Class> getXmlClasses() {
        List<Class> xmlClasses = new ArrayList<>();
        for (TsVariableAdapter adapter : adapters()) {
            adapter.xmlClasses(xmlClasses);
        }
        return xmlClasses;
    }

    public ITsVariable unmarshal(XmlRegressionVariable xvar) {
        for (TsVariableAdapter adapter : adapters()) {
            try {
                ITsVariable rslt = adapter.unmarshal(xvar);
                if (rslt != null) {
                    return rslt;
                }
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public XmlRegressionVariable marshal(ITsVariable ivar) {
        for (TsVariableAdapter adapter : adapters()) {
            try {
                XmlRegressionVariable rslt = adapter.marshal(ivar);
                if (rslt != null) {
                    return rslt;
                }
            } catch (Exception ex) {
            }
        }
        return null;
    }
}
