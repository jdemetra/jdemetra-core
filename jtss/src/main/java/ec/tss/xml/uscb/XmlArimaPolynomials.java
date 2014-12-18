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

import ec.tstoolkit.utilities.StringFormatter;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlArimaPolynomials.NAME)
public class XmlArimaPolynomials extends AbstractXmlArimaModel {
    static final String NAME = "arimaPolynomialsType";
    @XmlElement
    public Boolean mean;
    @XmlElement(name = "phi")
    public String phi;
    @XmlElement
    public int d;
    @XmlElement(name = "th")
    public String th;
    @XmlElement(name = "bphi")
    public String bphi;
    @XmlElement
    public int bd;
    @XmlElement(name = "bth")
    public String bth;

    public static XmlArimaPolynomials create(ArimaSpec spec) {
        XmlArimaPolynomials t = new XmlArimaPolynomials();
        if (spec.isMean())
            t.mean = Boolean.TRUE;
        t.d = spec.getD();
        t.bd = spec.getBD();
        t.phi = StringFormatter.write(spec.getPhi());
        t.th = StringFormatter.write(spec.getTheta());
        t.bphi = StringFormatter.write(spec.getBPhi());
        t.bth = StringFormatter.write(spec.getBTheta());
        return t;
    }

    @Override
    public void initSpec(ArimaSpec arima) {
        if (mean != null) {
            arima.setMean(mean);
        }
        arima.setD(d);
        arima.setBD(bd);
        arima.setPhi(StringFormatter.readParameters(phi));
        arima.setTheta(StringFormatter.readParameters(th));
        arima.setBPhi(StringFormatter.readParameters(bphi));
        arima.setBTheta(StringFormatter.readParameters(bth));
     }
}
