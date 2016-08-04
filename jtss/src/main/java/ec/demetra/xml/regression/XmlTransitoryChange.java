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

import ec.tstoolkit.timeseries.regression.TransitoryChange;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTransitoryChange.NAME)
@XmlType(name = XmlTransitoryChange.NAME)

public class XmlTransitoryChange extends XmlOutlier {

    static final String RNAME = "TransitoryChange", NAME = RNAME + "Type";

    @XmlAttribute
    public double factor = 0.7;

    @XmlAttribute
    public boolean monthlyFactor = false;

    @ServiceProvider(service = TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlTransitoryChange, TransitoryChange> {

        @Override
        public Class<TransitoryChange> getValueType() {
            return TransitoryChange.class;
        }

        @Override
        public Class<XmlTransitoryChange> getXmlType() {
            return XmlTransitoryChange.class;
        }

        @Override
        public TransitoryChange unmarshal(XmlTransitoryChange v) throws Exception {
            TransitoryChange o = new TransitoryChange(v.Position, v.factor, v.monthlyFactor);
            return o;
        }

        @Override
        public XmlTransitoryChange marshal(TransitoryChange v) throws Exception {
            XmlTransitoryChange xml = new XmlTransitoryChange();
            xml.Position = v.getPosition();
            xml.factor = v.getCoefficient();
            xml.monthlyFactor = v.isMonthlyCoefficient();
            return xml;
        }

    }
}
