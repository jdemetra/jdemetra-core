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

import ec.tstoolkit.timeseries.regression.LaggedTsVariable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
//@XmlRootElement(name = XmlLaggedVariable.RNAME)
@XmlType(name = XmlLaggedVariable.NAME)
public class XmlLaggedVariable extends XmlRegressionVariableModifier{
    
    static final String RNAME = "LaggedVariable", NAME = RNAME + "Type";

    @XmlElement
    public int FirstLag;
    
    @XmlElement
    public int LastLag;

    @ServiceProvider(service = TsModifierAdapter.class)
    public static class Adapter extends TsModifierAdapter<XmlLaggedVariable, LaggedTsVariable> {

        @Override
        public Class<LaggedTsVariable> getValueType() {
            return LaggedTsVariable.class;
        }

        @Override
        public Class<XmlLaggedVariable> getXmlType() {
            return XmlLaggedVariable.class;
        }

        @Override
        public LaggedTsVariable unmarshal(XmlLaggedVariable v) throws Exception {
            LaggedTsVariable o = new LaggedTsVariable(null, v.FirstLag, v.LastLag);
            return o;
        }

        @Override
        public XmlLaggedVariable marshal(LaggedTsVariable v) throws Exception {
            XmlLaggedVariable xml = new XmlLaggedVariable();
            xml.FirstLag = v.getFirstLag();
            xml.LastLag=v.getLastLag();
            return xml;
        }
    }
}
