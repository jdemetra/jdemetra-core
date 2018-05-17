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

import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlCalendarSpec.NAME)
public class XmlCalendarSpec {
    static final String NAME = "calendarSpecType";


    @XmlElements(value = {
        @XmlElement(name = "tdVariables", type = XmlTradingDaysSpec.class)
    })
    public AbstractXmlTdVariables td;
    @XmlElementWrapper(name="movingHolidaySpec")
    @XmlElement (name = "movingHolidaySpecType")
    public XmlMovingHolidaySpec[] movingHolidaySpec;
    @XmlElement
    public Double aiccDiff = RegressionSpec.DEF_AICCDIFF;
    public boolean isAiccDiffSpecified() {
        return aiccDiff != null;
    }

    public XmlCalendarSpec() { }

    public static XmlCalendarSpec create(RegressionSpec spec) {
        if (spec == null)
            return null;
        XmlCalendarSpec x = new XmlCalendarSpec();
        x.aiccDiff = spec.getAICCDiff();
        if (spec.getTradingDays().isUsed()) {
            XmlTradingDaysSpec td = new XmlTradingDaysSpec();
            td.copy(spec.getTradingDays());
            x.td = td;
        }
        MovingHolidaySpec[] mh = spec.getMovingHolidays();
        if (mh != null) {
            XmlMovingHolidaySpec[] xmh = new XmlMovingHolidaySpec[mh.length];
            for (int i = 0; i < xmh.length; ++i) {
                xmh[i] = new XmlMovingHolidaySpec();
                xmh[i].copy(mh[i]);
            }
            x.movingHolidaySpec = xmh;
        }
        return x;
    }

    public void initSpec(RegressionSpec spec) {
        if (isAiccDiffSpecified())
            spec.setAICCDiff(aiccDiff);
        if (td != null)
            td.copyTo(spec);
        if (movingHolidaySpec != null) {
            MovingHolidaySpec[] mh = new MovingHolidaySpec[movingHolidaySpec.length];
            for (int i = 0; i < movingHolidaySpec.length; ++i)
                mh[i] = movingHolidaySpec[i].create();
            spec.setMovingHolidays(mh);
        }
    }
}
