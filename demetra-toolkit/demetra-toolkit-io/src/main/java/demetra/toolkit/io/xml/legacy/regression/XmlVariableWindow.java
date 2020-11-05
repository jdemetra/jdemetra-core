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

import demetra.timeseries.regression.Window;
import demetra.toolkit.io.xml.legacy.XmlDateAdapter;
import java.time.LocalDate;
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
public class XmlVariableWindow extends XmlRegressionVariableModifier{
    
    static final String RNAME = "VariableWindow", NAME = RNAME + "Type";

    @XmlElement
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    public LocalDate From;
    
    @XmlElement
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    public LocalDate To;

    @ServiceProvider(TsModifierAdapter.class)
    public static class Adapter extends TsModifierAdapter<XmlVariableWindow, Window> {

        @Override
        public Class<Window> getValueType() {
            return Window.class;
        }

        @Override
        public Class<XmlVariableWindow> getXmlType() {
            return XmlVariableWindow.class;
        }

        @Override
        public Window unmarshal(XmlVariableWindow v) throws Exception {
            Window o = new Window(v.From, v.To);
            return o;
        }

        @Override
        public XmlVariableWindow marshal(Window v) throws Exception {
            XmlVariableWindow xml = new XmlVariableWindow();
            xml.From = v.getStart();
            xml.To=v.getEnd();
            return xml;
        }
    }
}
