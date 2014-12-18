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
import ec.tstoolkit.modelling.arima.x13.EstimateSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlEstimateSpec.NAME)
public class XmlEstimateSpec implements IXmlConverter<EstimateSpec> {

    static final String NAME = "estimateSpecType";

    public static enum ExactLikelihood {

        Arma, Ma, None
    }
    @XmlElement
    public Double tol;
    @XmlElement
    public Integer maxiter = null;
    @XmlElement
    public ExactLikelihood likelihood;

    public static XmlEstimateSpec create(EstimateSpec spec) {
        if (spec == null) {
            return null;
        }
        XmlEstimateSpec x = new XmlEstimateSpec();
        x.copy(spec);
        return x;
    }

    public void copyTo(EstimateSpec spec) {
        if (tol != null) {
            spec.setTol(tol);
        }
    }

    @Override
    public EstimateSpec create() {
        EstimateSpec spec = new EstimateSpec();
        copyTo(spec);
        return spec;
    }

    @Override
    public void copy(EstimateSpec t) {
        if (t.getTol() != EstimateSpec.DEF_TOL) {
            tol = t.getTol();
        } else {
            tol = null;
        }
    }
}
