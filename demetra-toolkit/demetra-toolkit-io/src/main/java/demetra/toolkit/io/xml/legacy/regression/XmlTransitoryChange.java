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
import demetra.timeseries.regression.TransitoryChange;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

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
            return 0.7D;
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

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter {

        @Override
        public TransitoryChange unmarshal(XmlRegressionVariable var) {
            if (!(var instanceof XmlTransitoryChange)) {
                return null;
            }
            XmlTransitoryChange v = (XmlTransitoryChange) var;
            TransitoryChange o = new TransitoryChange(v.position.atStartOfDay(), v.factor);
            return o;
        }

        @Override
        public XmlTransitoryChange marshal(ITsVariable var) {
            if (!(var instanceof TransitoryChange)) {
                return null;
            }
            TransitoryChange v = (TransitoryChange) var;
            XmlTransitoryChange xml = new XmlTransitoryChange();
            xml.position = v.getPosition().toLocalDate();
            xml.factor = v.getRate();
//            xml.monthlyFactor = v.isMonthlyCoefficient();
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlTransitoryChange.class);
        }
    }
    
    public static final Adapter ADAPTER=new Adapter();
}
