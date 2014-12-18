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
import ec.tss.xml.XmlPeriodSelection;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlOutlierSpec.NAME)
public class XmlOutlierSpec implements IXmlConverter<OutlierSpec> {

    static final String NAME = "outlierSpecType";
    @XmlElement
    public XmlPeriodSelection span;
    @XmlElement
    public Double defaultCv;
    @XmlElementWrapper
    @XmlElement( name="outlier")
    public XmlSingleOutlierSpec[] type;
    @XmlElement
    public Integer lsrun;
    @XmlElement
    public OutlierSpec.Method method = OutlierSpec.Method.AddOne;
    @XmlElement
    public Double tcrate = OutlierSpec.DEF_TCRATE;

    public XmlOutlierSpec() {
    }

    public static XmlOutlierSpec create(OutlierSpec spec) {
        if (spec == null) {
            return null;
        }
        XmlOutlierSpec x = new XmlOutlierSpec();
        x.copy(spec);
        x.defaultCv = spec.getDefaultCriticalValue();
        return x;
    }

    public void copyTo(OutlierSpec spec) {
        if (span != null) {
            spec.setSpan(span.create());
        }
        if (defaultCv != null) {
            spec.setDefaultCriticalValue(defaultCv);
        }
        if (type != null) {
            for (int i = 0; i < type.length; ++i) {
                spec.add(type[i].create());
            }
        }
        if (lsrun != null) {
            spec.setLSRun(lsrun);
        }
        if (method != null) {
            spec.setMethod(method);
        }
        if (tcrate != null) {
            spec.setMonthlyTCRate(tcrate);
        }
    }

    @Override
    public OutlierSpec create() {
        OutlierSpec spec = new OutlierSpec();
        copyTo(spec);
        return spec;
    }

    @Override
    public void copy(OutlierSpec t) {
        if (t.getSpan() != null) {
            span = new XmlPeriodSelection();
            span.copy(t.getSpan());
        }
        SingleOutlierSpec[] types = t.getTypes();
        if (types != null && types.length > 0) {
            type = new XmlSingleOutlierSpec[types.length];
            for (int i = 0; i < types.length; ++i) {
                type[i] = new XmlSingleOutlierSpec();
                type[i].copy(types[i]);
            }
        }
        if (t.getMethod() != OutlierSpec.Method.AddOne) {
            method = t.getMethod();
        }
//        lsrun = t.getLSRun(); NOT USED YET
        if (t.getMonthlyTCRate() != OutlierSpec.DEF_TCRATE) {
            tcrate = t.getMonthlyTCRate();
        }
    }
}
