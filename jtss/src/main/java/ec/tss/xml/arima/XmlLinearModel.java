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
import ec.tstoolkit.arima.LinearModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.FiniteFilter;
import ec.tstoolkit.maths.linearfilters.ForeFilter;
import ec.tstoolkit.maths.linearfilters.IFiniteFilter;
import ec.tstoolkit.maths.linearfilters.RationalFilter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlLinearModel.RNAME)
@XmlType(name = XmlLinearModel.NAME)
public class XmlLinearModel implements IXmlConverter<LinearModel> {
    static final String NAME = "linearModelType";
    static final String RNAME = "linearModel";

    @XmlAttribute
    public String name;

    @XmlElement(name = "bar")
    @XmlList
    public double[] bar;
    @XmlElement(name = "far")
    @XmlList
    public double[] far;
    @XmlElement(name = "ma")
    @XmlList
    public double[] ma;
    @XmlElement
    public int lb;
    @XmlAttribute
    public double innovationVariance;

    @Override
    public LinearModel create() {
        ForeFilter pfar = null;
        BackFilter pbar = null;
        FiniteFilter pma = null;
        if (bar != null)
            pbar = BackFilter.of(bar);
        if (far != null)
            pfar = new ForeFilter(far);
        if (ma != null)
            pma = new FiniteFilter(ma, lb);
        RationalFilter rf = new RationalFilter(pma, pbar, pfar);
        return new LinearModel(rf, innovationVariance);
    }

    @Override
    public void copy(LinearModel t) {
        RationalFilter rf = t.getFilter();
        if (rf != null) {
            bar = rf.getRationalBackFilter().getDenominator().getPolynomial().getCoefficients();
            far = rf.getRationalForeFilter().getDenominator().getPolynomial().getCoefficients();
        }
        IFiniteFilter num = t.getFilter().getNumerator();
        ma = num.getWeights();
        lb = num.getLowerBound();
        innovationVariance = t.getInnovationVariance();
    }
}
