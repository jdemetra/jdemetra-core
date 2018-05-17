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


package demetra.xml.sa.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlEaster.NAME)
public class XmlEaster implements IXmlTramoSeatsSpec {
    static final String NAME = "easterSpecType";

    @XmlElement
    public Integer duration = EasterSpec.DEF_IDUR;
    public boolean isDurationSpecified() {
        return duration != null;
    }
    @XmlAttribute
    public Boolean pretest;
    public boolean isPretestSpecified() {
        return pretest != null;
    }

    public static XmlEaster create(CalendarSpec spec) {
        if (! spec.getEaster().isUsed())
            return null;
        XmlEaster easter = new XmlEaster();
        easter.duration = spec.getEaster().getDuration();
        if (spec.getEaster().isTest())
            easter.pretest = true;
        return easter;
    }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        RegressionSpec reg=spec.getTramoSpecification().getRegression();
        if (reg == null){
            reg=new RegressionSpec();
            spec.getTramoSpecification().setRegression(reg);
        }
        CalendarSpec cspec=reg.getCalendar();
        EasterSpec easter=cspec.getEaster();

         if (isPretestSpecified())
            easter.setTest(pretest);
        if (isDurationSpecified())
            easter.setDuration(duration);
    }
}
