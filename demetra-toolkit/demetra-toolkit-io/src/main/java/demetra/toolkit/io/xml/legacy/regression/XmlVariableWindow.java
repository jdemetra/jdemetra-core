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

import demetra.timeseries.regression.ModifiedTsVariable;
import demetra.timeseries.regression.Window;
import demetra.toolkit.io.xml.legacy.XmlDateAdapter;
import java.time.LocalDate;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
//@XmlRootElement(name = XmlVariableWindow.NAME)
@XmlType(name = XmlVariableWindow.NAME)
public class XmlVariableWindow extends XmlRegressionVariableModifier {

    static final String RNAME = "VariableWindow", NAME = RNAME + "Type";

    @XmlElement
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    public LocalDate From;

    @XmlElement
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    public LocalDate To;

    @ServiceProvider(TsModifierAdapter.class)
    public static class Adapter extends TsModifierAdapter {

        @Override
        public Window unmarshal(XmlRegressionVariableModifier var) throws Exception {
            if (!(var instanceof XmlVariableWindow)) {
                return null;
            }
            XmlVariableWindow v = (XmlVariableWindow) var;
            Window o = new Window(v.From, v.To);
            return o;
        }

        @Override
        public XmlVariableWindow marshal(ModifiedTsVariable.Modifier var) throws Exception {
            if (!(var instanceof Window)) {
                return null;
            }
            Window v = (Window) var;
            XmlVariableWindow xml = new XmlVariableWindow();
            xml.From = v.getStart();
            xml.To = v.getEnd();
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlVariableWindow.class);
         }
    }
}
