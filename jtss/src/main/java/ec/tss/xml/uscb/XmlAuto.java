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

import ec.tss.xml.sa.IXmlAuto;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlAuto.NAME)
public class XmlAuto extends AbstractXmlTransform implements IXmlAuto {

    static final String NAME = "autoTransformSpecType";
    @XmlElement
    public Double aicDiff;
    @XmlElement
    public LengthOfPeriodType adjust;

    public XmlAuto() {
    }

    public static XmlAuto create(TransformSpec spec) {
        if (spec.getFunction() != DefaultTransformationType.Auto) {
            return null;
        }
        XmlAuto x = new XmlAuto();
        if (spec.getAICDiff() != TransformSpec.DEF_AICDIFF) {
            x.aicDiff = spec.getAICDiff();
        }
        if (spec.getAdjust() != LengthOfPeriodType.None) {
            x.adjust = spec.getAdjust();
        }
        return x;
    }

    @Override
    public void copyTo(TransformSpec spec) {
        spec.setFunction(DefaultTransformationType.Auto);
        if (aicDiff != null) {
            spec.setAICDiff(aicDiff);
        }
        if (adjust != null) {
            spec.setAdjust(adjust);
        }
    }
}
