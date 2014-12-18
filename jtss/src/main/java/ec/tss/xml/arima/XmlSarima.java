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
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlSarima.RNAME)
@XmlType(name = XmlSarima.NAME)
public class XmlSarima implements IXmlConverter<SarimaModel> {
    static final String NAME = "sarimaModelType";
    static final String RNAME = "sarimaModel";

    @XmlAttribute
    public String name;

    @XmlElement(name = "phi")
    @XmlList
    public double[] phi;
    @XmlElement
    public int d;
    public boolean getDSpecified() {
        return d > 0;
    }
    @XmlElement(name = "theta")
    @XmlList
    public double[] theta;
    public int s;
    public boolean getSSpecified() {
        return s > 1;
    }
    @XmlElement(name = "bphi")
    @XmlList
    public double[] bphi;
    @XmlElement
    public int bd;
    public boolean getBdSpecified() {
        return bd > 0;
    }
    @XmlElement(name = "btheta")
    @XmlList
    public double[] btheta;

    @Override
    public void copy(SarimaModel t) {
        SarimaSpecification spec = t.getSpecification();
        d = spec.getD();
        bd = spec.getBD();
        s = spec.getFrequency();
        if (spec.getP() > 0) {
            phi = new double[spec.getP() - 1];
            for (int i = 1; i <= spec.getP(); ++i)
                phi[i - 1] = t.phi(i);
        }
        if (spec.getQ() > 0) {
            theta = new double[spec.getQ() - 1];
            for (int i = 1; i <= spec.getQ(); ++i)
                theta[i - 1] = t.theta(i);
        }
        if (spec.getBP() > 0)
            bphi = new double[] {t.bphi(1)};
        if (spec.getBQ() > 0)
            btheta = new double[] {t.btheta(1)};
    }

    public SarimaModel create() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
