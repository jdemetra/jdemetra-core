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


package ec.tss.xml.arima;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlArimaModel.RNAME)
@XmlType(name = XmlArimaModel.NAME)
public class XmlArimaModel implements IXmlConverter<ArimaModel> {
    static final String NAME = "arimaModelType";
    static final String RNAME = "arimaModel";

    @XmlAttribute
    public String name;

    @XmlElement(name = "ar")
    @XmlList
    public double[] ar;
    @XmlElement(name = "differencing")
    @XmlList
    public double[] differencing;
    @XmlElement(name = "ma")
    @XmlList
    public double[] ma;
    @XmlAttribute
    public double innovationVariance;

    @Override
    public ArimaModel create() {
        BackFilter far = null, fd = null, fma = null;
        if (ar != null)
            far = BackFilter.of(ar);
        if (differencing != null)
            fd = BackFilter.of(differencing);
        if (ma != null)
            fma = BackFilter.of(ma);
        return new ArimaModel(far, fd, fma, innovationVariance);
    }

    @Override
    public void copy(ArimaModel t) {
        ar = t.getStationaryAR().getPolynomial().getCoefficients();
        differencing = t.getNonStationaryAR().getPolynomial().getCoefficients();
        ma = t.getMA().getPolynomial().getCoefficients();
        innovationVariance = t.getInnovationVariance();
    }
}
