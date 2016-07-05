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
package ec.demetra.xml.regression;

import ec.tss.xml.XmlDayAdapter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.TsVariableWindow;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlVariableWindow.NAME)
@XmlType(name = XmlVariableWindow.NAME)
public class XmlVariableWindow extends XmlTsModifier{
    
    static final String RNAME = "VariableWindow", NAME = RNAME + "Type";

    @XmlElement
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day Start;
    
    @XmlElement
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day End;

    @ServiceProvider(service = ITsVariableAdapter.class)
    public static class Adapter implements ITsVariableAdapter<XmlVariableWindow, TsVariableWindow> {

        @Override
        public Class<TsVariableWindow> getValueType() {
            return TsVariableWindow.class;
        }

        @Override
        public Class<XmlVariableWindow> getXmlType() {
            return XmlVariableWindow.class;
        }

        @Override
        public TsVariableWindow decode(XmlVariableWindow v) throws Exception {
            TsVariableWindow o = new TsVariableWindow(TsVariableAdapters.getDefault().decode(v.Core), v.Start, v.End);
            return o;
        }

        @Override
        public XmlVariableWindow encode(TsVariableWindow v) throws Exception {
            XmlVariableWindow xml = new XmlVariableWindow();
            xml.Start = v.getStart();
            xml.End=v.getEnd();
            xml.Core=TsVariableAdapters.getDefault().encode(v.getVariable());
            return xml;
        }
    }
}
