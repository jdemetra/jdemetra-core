/*
* Copyright 2013 National Bank of Belgium
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

package ec.tss.xml.sa;

import ec.tstoolkit.utilities.StringFormatter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author BAYENSK
 */
public abstract class AbstractXmlOutlierDefinition {
    
    @XmlAttribute
    public String period;
    @XmlAttribute
    public Boolean prespecified;
    
    public boolean isPrespecifiedOutlier() {
        return prespecified == null ? true : prespecified;
    }
    
    public void setPrespecifiedOutlier(boolean p) {
        if (p) {
            prespecified = null;
        } else {
            prespecified = p;
        }
    }
    
    protected abstract OutlierType getType();
    
    public OutlierDefinition getDefinition() {
        Day p = StringFormatter.convertDay(period);
        if (p != null) {
            return new OutlierDefinition(p, getType());
        } else {
            // for compatibility issues
            TsPeriod pos = StringFormatter.readPeriod(period);
            if (pos != null) {
                return new OutlierDefinition(pos, getType());
            } else {
                return null;
            }
        }
    }
    
    public static AbstractXmlOutlierDefinition create(OutlierDefinition def) {
        AbstractXmlOutlierDefinition x = null;
        if (def.getType() == OutlierType.AO) {
            x = new XmlAoDefinition();
        } else if (def.getType() == OutlierType.LS) {
            x = new XmlLsDefinition();
        } else if (def.getType() == OutlierType.TC) {
            x = new XmlTcDefinition();
        } else if (def.getType() == OutlierType.SO) {
            x = new XmlSoDefinition();
        }
        if (x != null) {
            x.period = StringFormatter.convert(def.getPosition());
        }
        return x;
    }
}
