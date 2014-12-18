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


package ec.tss.xml.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.xml.sa.AbstractXmlOutlierSpec;
import ec.tstoolkit.modelling.arima.tramo.OutlierSpec;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.utilities.Arrays2;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlOutlierSpec.NAME)
public class XmlOutlierSpec extends AbstractXmlOutlierSpec implements IXmlTramoSeatsSpec {
    static final String NAME = "outlierSpecType";

    @XmlElement(name="types")
    @XmlList
    public OutlierType[] types;
    @XmlElement
    public Double va;
    public boolean isVaSpecified() {
        return va != null;
    }
    @XmlElement
    public Integer imvx;
    public boolean isImvxSpecified() {
        return imvx != null;
    }
    @XmlElement
    public Double tcrate = OutlierSpec.DEF_DELTATC;
    public boolean isTcrateSpecified() {
        return tcrate != null;
    }

    public static XmlOutlierSpec create(OutlierSpec spec) {
        if (spec == null)
            return null;
        OutlierType[] types = spec.getTypes();
        if (Arrays2.isNullOrEmpty(types))
            return null;
        XmlOutlierSpec s = new XmlOutlierSpec();
        s.types = types;
        s.va = spec.getCriticalValue();
        s.tcrate = spec.getDeltaTC();
        s.initialize(spec.getSpan());
        s.imvx = spec.isEML() ? 1 : 0;
        return s;
    }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        OutlierSpec s = new OutlierSpec();
        if (span != null)
            s.setSpan(span.create());
        s.setTypes(types);
        if (isTcrateSpecified())
            s.setDeltaTC(tcrate);
        if (isVaSpecified())
            s.setCriticalValue(va);
        if (isImvxSpecified())
        s.setEML(imvx != 0);
        spec.getTramoSpecification().setOutliers(s);
    }
}
