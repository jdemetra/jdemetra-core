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
package demetra.toolkit.io.xml.legacy.modelling;

import demetra.timeseries.regression.modelling.SarimaSpec;
import demetra.toolkit.io.xml.legacy.arima.XmlSarimaOrders;
import demetra.toolkit.io.xml.legacy.arima.XmlSarimaPolynomials;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ArimaSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="ArimaSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Mean" type="{ec/eurostat/jdemetra/modelling}MeanCorrectionType"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Orders" type="{ec/eurostat/jdemetra/core}SARIMA_OrderType"/&gt;
 *           &lt;element name="Model" type="{ec/eurostat/jdemetra/core}SARIMA_ModelType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "ArimaSpec")
@XmlType(name = "ArimaSpecType", propOrder = {
    "mean",
    "orders",
    "polynomials"
})
public class XmlArimaSpec {

    @XmlElement(name = "Mean")
    protected XmlMeanCorrection mean;
    @XmlElement(name = "Orders")
    protected XmlSarimaOrders orders;
    @XmlElement(name = "Polynomials")
    protected XmlSarimaPolynomials polynomials;

    /**
     * Gets the value of the mean property.
     *
     * @return possible object is {@link MeanCorrectionType }
     *
     */
    public XmlMeanCorrection getMean() {
        return mean;
    }

    /**
     * Sets the value of the mean property.
     *
     * @param value allowed object is {@link MeanCorrectionType }
     *
     */
    public void setMean(XmlMeanCorrection value) {
        this.mean = value;
    }

    /**
     * Gets the value of the orders property.
     *
     * @return possible object is {@link SARIMAOrderType }
     *
     */
    public XmlSarimaOrders getOrders() {
        return orders;
    }

    /**
     * Sets the value of the orders property.
     *
     * @param value allowed object is {@link SARIMAOrderType }
     *
     */
    public void setOrders(XmlSarimaOrders value) {
        this.orders = value;
    }

    /**
     * Gets the value of the model property.
     *
     * @return possible object is {@link SARIMAModelType }
     *
     */
    public XmlSarimaPolynomials getPolynomials() {
        return polynomials;
    }

    /**
     * Sets the value of the model property.
     *
     * @param value allowed object is {@link SARIMAModelType }
     *
     */
    public void setPolynomials(XmlSarimaPolynomials value) {
        this.polynomials = value;
    }

    public static boolean marshal(SarimaSpec v, XmlArimaSpec xml) {
        // unspecified model
        if (v.isUndefined()) {
            XmlSarimaOrders orders = new XmlSarimaOrders();
            orders.setP(v.getP());
            orders.setD(v.getD());
            orders.setQ(v.getQ());
            orders.setBP(v.getBp());
            orders.setBD(v.getBd());
            orders.setBQ(v.getBq());
            xml.setOrders(orders);
        } else {
            XmlSarimaPolynomials polynomials = new XmlSarimaPolynomials();
            polynomials.setD(v.getD());
            polynomials.setBD(v.getBd());
            polynomials.setAR(v.getPhi());
            polynomials.setMA(v.getTheta());
            polynomials.setBAR(v.getBphi());
            polynomials.setBMA(v.getBtheta());
            xml.setPolynomials(polynomials);
        }
        return true;
    }

    public static SarimaSpec unmarshal(XmlArimaSpec xml) {
        // unspecified model
        SarimaSpec.Builder builder = SarimaSpec.builder();
        if (xml.orders != null) {
            builder = builder.p(xml.orders.getP())
                    .d(xml.orders.getD())
                    .q(xml.orders.getQ())
                    .bp(xml.orders.getBP())
                    .bd(xml.orders.getBD())
                    .bq(xml.orders.getBQ());
        } else if (xml.polynomials != null) {
            if (xml.polynomials.getD() != null) {
                builder = builder.d(xml.polynomials.getD());
            }
            if (xml.polynomials.getBD() != null) {
                builder = builder.bd(xml.polynomials.getBD());
            }
            builder = builder.phi(xml.polynomials.getAR())
                    .theta(xml.polynomials.getMA())
                    .bphi(xml.polynomials.getBAR())
                    .btheta(xml.polynomials.getBMA());
        }
        return builder.build();
    }

}
