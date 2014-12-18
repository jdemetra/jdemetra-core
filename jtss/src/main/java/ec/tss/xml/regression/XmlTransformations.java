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


package ec.tss.xml.regression;

import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Kristof Bayens
 */
public class XmlTransformations {
    public enum Interpolation
    {
        none,
        byreplacement,
        linear
    };

    @XmlAttribute
    public boolean logTransformed;

    @XmlAttribute
    public Interpolation interpolation = Interpolation.none;
    public boolean isInterpolationSpecified() {
        return interpolation != Interpolation.none;
    }

    @XmlAttribute
    public Double constant;
    public boolean isConstantSpecified() {
        return constant != null;
    }

    @XmlAttribute
    public Boolean permanentPrior;
    public boolean isPermanentPriorSpecified() {
        return permanentPrior != null;
    }

    @XmlAttribute
    public Boolean temporaryPrior;
    public boolean isTemporaryPriorSpecified() {
        return temporaryPrior != null;
    }

    @XmlAttribute
    public LengthOfPeriodType adjust= LengthOfPeriodType.None;
    public boolean isAdjustSpecified() {
        return adjust != null;
    }

    @XmlAttribute
    public Double units = 1.0;
    @XmlAttribute
    public boolean isUnitsSpecified() {
        return units != null;
    }

    public boolean isDefault() {
        return logTransformed = false && !isConstantSpecified() && !isInterpolationSpecified()
            && !isTemporaryPriorSpecified() && !isPermanentPriorSpecified() && !isAdjustSpecified()
            && !isUnitsSpecified();
    }
}
