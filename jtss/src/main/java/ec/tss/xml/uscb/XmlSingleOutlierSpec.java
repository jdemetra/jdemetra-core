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

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.timeseries.regression.OutlierType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlSingleOutlierSpec.NAME)
public class XmlSingleOutlierSpec implements IXmlConverter<SingleOutlierSpec> {
    static final String NAME = "singleOutlierSpecType";

    @XmlElement
    public OutlierType type = OutlierType.Undefined;
    @XmlElement
    public Double cv;
    public boolean isCvSpecified() {
        return cv != null;
    }

    @Override
    public SingleOutlierSpec create() {
        SingleOutlierSpec spec = new SingleOutlierSpec();
        spec.setType(type);
        if (isCvSpecified())
            spec.setCriticalValue(cv);
        return spec;
    }

    @Override
    public void copy(SingleOutlierSpec t) {
        if (t == null)
            return;
        type = t.getType();
        cv = t.getCriticalValue();
    }
}
