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

import ec.tstoolkit.timeseries.regression.LevelShift;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlLevelShift.RNAME)
@XmlType(name = XmlLevelShift.NAME)
public class XmlLevelShift extends XmlOutlier {

    static final String RNAME = "LevelShift", NAME = RNAME + "Type";

    @XmlAttribute
    public boolean zeroEnded=true;
    
    @ServiceProvider(service = TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlLevelShift, LevelShift> {

        @Override
        public Class<LevelShift> getValueType() {
            return LevelShift.class;
        }

        @Override
        public Class<XmlLevelShift> getXmlType() {
            return XmlLevelShift.class;
        }

        @Override
        public LevelShift unmarshal(XmlLevelShift v) throws Exception {
            LevelShift o = new LevelShift(v.Position);
            o.setZeroEnded(v.zeroEnded);
            return o;
        }

        @Override
        public XmlLevelShift marshal(LevelShift v) throws Exception {
            XmlLevelShift xml = new XmlLevelShift();
            xml.Position = v.getPosition();
            xml.zeroEnded=v.isZeroEnded();
            return xml;
        }

    }
}
