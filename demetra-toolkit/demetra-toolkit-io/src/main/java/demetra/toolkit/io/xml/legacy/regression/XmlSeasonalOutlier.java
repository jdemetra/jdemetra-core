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

import demetra.timeseries.regression.PeriodicOutlier;
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
@XmlType(name = "SeasonalOutlierType")
public class XmlSeasonalOutlier extends XmlOutlier {

    @XmlAttribute(name = "zeroEnded")
    protected Boolean zeroEnded;

    public boolean isZeroEnded() {
        if (zeroEnded == null) {
            return true;
        } else {
            return zeroEnded;
        }
    }

    public void setZeroEnded(Boolean value) {
        this.zeroEnded = value;
    }

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlSeasonalOutlier, PeriodicOutlier> {

        @Override
        public Class<PeriodicOutlier> getImplementationType() {
            return PeriodicOutlier.class;
        }

        @Override
        public Class<XmlSeasonalOutlier> getXmlType() {
            return XmlSeasonalOutlier.class;
        }

        @Override
        public PeriodicOutlier unmarshal(XmlSeasonalOutlier v) throws Exception {
            PeriodicOutlier o = new PeriodicOutlier(v.position.atStartOfDay(), 0, v.zeroEnded != null ? v.zeroEnded : true);
            return o;
        }

        @Override
        public XmlSeasonalOutlier marshal(PeriodicOutlier v) throws Exception {
            XmlSeasonalOutlier xml = new XmlSeasonalOutlier();
            xml.position = v.getPosition().toLocalDate();
            xml.zeroEnded = v.isZeroEnded();
            return xml;
        }

    }
}
