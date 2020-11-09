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
import demetra.timeseries.regression.TsLags;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
//@XmlRootElement(name = XmlLaggedVariable.RNAME)
@XmlType(name = XmlLags.NAME)
public class XmlLags extends XmlRegressionVariableModifier {

    static final String RNAME = "LaggedVariable", NAME = RNAME + "Type";

    @XmlElement
    public int FirstLag;

    @XmlElement
    public int LastLag;

    @ServiceProvider(TsModifierAdapter.class)
    public static class Adapter extends TsModifierAdapter {

        @Override
        public TsLags unmarshal(XmlRegressionVariableModifier var) throws Exception {
            if (!(var instanceof XmlLags)) {
                return null;
            }
            XmlLags v = (XmlLags) var;
            return new TsLags(v.FirstLag, v.LastLag);
        }

        @Override
        public XmlLags marshal(ModifiedTsVariable.Modifier m) throws Exception {
            if (!(m instanceof TsLags)) {
                return null;
            }
            TsLags v = (TsLags) m;
            XmlLags xml = new XmlLags();
            xml.FirstLag = v.getFirstLag();
            xml.LastLag = v.getLastLag();
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add((XmlLags.class));
        }
    }
}
