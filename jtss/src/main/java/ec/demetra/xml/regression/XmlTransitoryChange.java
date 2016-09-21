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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransitoryChangeType")
public class XmlTransitoryChange extends XmlOutlier {

    @XmlAttribute(name = "factor")
    protected Double factor;
    @XmlAttribute(name = "monthlyFactor")
    protected Boolean monthlyFactor;

    public double getFactor() {
        if (factor == null) {
            return  0.7D;
        } else {
            return factor;
        }
    }

    public void setFactor(Double value) {
        this.factor = value;
    }

    public boolean isMonthlyFactor() {
        if (monthlyFactor == null) {
            return false;
        } else {
            return monthlyFactor;
        }
    }

    public void setMonthlyFactor(Boolean value) {
        this.monthlyFactor = value;
    }
    
    @ServiceProvider(service = TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlTransitoryChange, TransitoryChange> {

        @Override
        public Class<TransitoryChange> getImplementationType() {
            return TransitoryChange.class;
        }

        @Override
        public Class<XmlTransitoryChange> getXmlType() {
            return XmlTransitoryChange.class;
        }

        @Override
        public TransitoryChange unmarshal(XmlTransitoryChange v) throws Exception {
            TransitoryChange o = new TransitoryChange(v.position, v.factor, v.monthlyFactor);
            return o;
        }

        @Override
        public XmlTransitoryChange marshal(TransitoryChange v) throws Exception {
            XmlTransitoryChange xml = new XmlTransitoryChange();
            xml.position = v.getPosition();
            xml.factor = v.getCoefficient();
            xml.monthlyFactor = v.isMonthlyCoefficient();
            return xml;
        }

    }
}
