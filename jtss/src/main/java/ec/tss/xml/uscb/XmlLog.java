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


package ec.tss.xml.uscb;

import ec.tss.xml.sa.IXmlLog;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlLog.NAME)
public class XmlLog extends AbstractXmlTransform implements IXmlLog {
    static final String NAME = "logTransformSpecType";

    @XmlElement
    public String permanentPrior;
    @XmlElement
    public String temporaryPrior;
    @XmlElement
    public LengthOfPeriodType adjust = LengthOfPeriodType.None;
    public boolean isAdjustSpecified() {
        return adjust != LengthOfPeriodType.None;
    }

    public static XmlLog create(TransformSpec spec) {
        if (spec.getFunction() != DefaultTransformationType.Log)
            return null;
        XmlLog x = new XmlLog();
        x.adjust = spec.getAdjust();
        return x;
    }

    @Override
    public void copyTo(TransformSpec spec) {
        spec.setFunction(DefaultTransformationType.Log);
        spec.setAdjust(adjust);
    }
}
