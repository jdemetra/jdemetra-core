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


package demetra.xml.sa.uscb;

import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;

/**
 *
 * @author Kristof Bayens
 */
public abstract class AbstractXmlTransform {
    public static AbstractXmlTransform create(TransformSpec spec) {
        if (spec == null)
            return null;
        if (spec.getFunction() == DefaultTransformationType.Auto)
            return XmlAuto.create(spec);
        else if (spec.getFunction() == DefaultTransformationType.Log)
            return XmlLog.create(spec);
        else // level
            return XmlLevel.create(spec);
    }

    public abstract void copyTo(TransformSpec spec);
}
