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

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.modelling.arima.tramo.EstimateSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlEstimateSpec.NAME)
public class XmlEstimateSpec implements IXmlConverter<EstimateSpec> {
    static final String NAME = "estimateSpecType";

    @XmlElement
    public Boolean eml = true;
    public boolean isEmlSpecified() {
        return eml != null;
    }
    @XmlElement
    public Double tol = EstimateSpec.DEF_TOL;
    public boolean isTolSpecified() {
        return tol != null;
    }
    @XmlElement
    public Double ubp = EstimateSpec.DEF_UBP;
    public boolean isUbpSpecified() {
        return ubp != null;
    }

    @Override
    public EstimateSpec create() {
        EstimateSpec spec = new EstimateSpec();
        if (isEmlSpecified())
            spec.setEML(eml);
        if (isTolSpecified())
            spec.setTol(tol);
        if (isUbpSpecified())
            spec.setUbp(ubp);
        return spec;
    }

    @Override
    public void copy(EstimateSpec t) {
        eml = t.isEML();
        tol = t.getTol();
        ubp = t.getUbp();
    }
}
