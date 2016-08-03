/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.xml.sa.tramoseats;

import ec.demetra.xml.core.XmlPeriodSelection;
import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.modelling.arima.tramo.EstimateSpec;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlEstimationSpec.RNAME)
@XmlType(name = XmlEstimationSpec.NAME)
public class XmlEstimationSpec implements IXmlConverter<EstimateSpec> {

    @XmlElement(name = "Span")
    public XmlPeriodSelection span;

    @XmlElement(name = "Precision")
    public Double tol;

    @XmlElement(name = "EML")
    public Boolean eml;

    @XmlElement(name = "UBP")
    public Double ubp;

    static final String RNAME = "EstimationSpec", NAME = RNAME + "Type";

    @Override
    public EstimateSpec create() {
        EstimateSpec spec = new EstimateSpec();
        if (span != null)
            spec.setSpan(span.create());
        if (tol != null)
            spec.setTol(tol);
        if (eml != null)
            spec.setEML(eml);
        if (ubp != null)
            spec.setUbp(ubp);
        return spec;
    }

    @Override
    public void copy(EstimateSpec v) {
        TsPeriodSelector vspan = v.getSpan();
        if (vspan != null && vspan.getType() != PeriodSelectorType.All) {
            span = new XmlPeriodSelection();
            span.copy(vspan);
        } else {
            span = null;
        }
        if (v.getTol() != EstimateSpec.DEF_TOL) {
            tol = v.getTol();
        } else {
            tol = null;
        }
        if (!v.isEML()) {
            eml = false;
        } else {
            eml = null;
        }
        if (v.getUbp() != EstimateSpec.DEF_UBP) {
            ubp = v.getUbp();
        } else {
            ubp = null;
        }
    }

}
