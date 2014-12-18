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
import ec.tstoolkit.modelling.arima.tramo.ArimaSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlArimaOrders.NAME)
public class XmlArimaOrders extends AbstractXmlArimaModel {
    static final String NAME = "arimaOrdersType";

    @XmlElement
    public Boolean mean;
    public boolean isMeanSpecified() {
        return mean != null;
    }
    @XmlElement
    public int p;
    @XmlElement
    public int d;
    @XmlElement
    public int q;
    @XmlElement
    public int bp;
    @XmlElement
    public int bd;
    @XmlElement
    public int bq;

    public static XmlArimaOrders create(ArimaSpec spec) {
        XmlArimaOrders t = new XmlArimaOrders();
        t.mean = spec.isMean();
        t.p = spec.getP();
        t.d = spec.getD();
        t.q = spec.getQ();
        t.bp = spec.getBP();
        t.bd = spec.getBD();
        t.bq = spec.getBQ();
        return t;
    }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        ArimaSpec arima = new ArimaSpec();
        if (isMeanSpecified())
            arima.setMean(mean);
        arima.setP(p);
        arima.setD(d);
        arima.setQ(q);
        arima.setBP(bp);
        arima.setBD(bd);
        arima.setBQ(bq);
        spec.getTramoSpecification().setArima(arima);
    }
}
