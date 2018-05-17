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

import demetra.xml.IXmlConverter;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlMovingHolidaySpec.NAME)
public class XmlMovingHolidaySpec implements IXmlConverter<MovingHolidaySpec> {
    static final String NAME = "movingHolidaySpecType";

     
    @XmlElement
    public MovingHolidaySpec.Type type;
    @XmlElement
    public int w;
    @XmlElement
    public XmlChangeOfRegimeSpec changeOfRegime;
    @XmlElement
    public RegressionTestSpec aicTest;

    @Override
    public MovingHolidaySpec create() {
        MovingHolidaySpec spec = new MovingHolidaySpec();
        spec.setType(type);
        spec.setW(w);
        spec.setTest(aicTest);
        if (changeOfRegime != null)
            spec.setChangeOfRegime(changeOfRegime.create());
        return spec;
    }

    @Override
    public void copy(MovingHolidaySpec t) {
        type = t.getType();
        w = t.getW();
        aicTest = t.getTest();
        if (t.getChangeOfRegime() != null) {
            changeOfRegime = new XmlChangeOfRegimeSpec();
            changeOfRegime.copy(t.getChangeOfRegime());
        }
    }
}
