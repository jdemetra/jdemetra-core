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
package ec.demetra.xml.modelling;

import ec.demetra.xml.arima.XmlSarimaModel;
import ec.demetra.xml.arima.XmlSarimaOrders;
import ec.demetra.xml.arima.XmlSarimaPolynomials;
import ec.demetra.xml.core.XmlParameters;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.modelling.arima.DefaultArimaSpec;
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
@XmlRootElement(name = "ArimaSpec")
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

    public static final InPlaceXmlMarshaller<XmlArimaSpec, DefaultArimaSpec> MARSHALLER = (DefaultArimaSpec v, XmlArimaSpec xml) -> {
        // unspecified model
        if (!v.hasParameters()) {
            XmlSarimaOrders orders = new XmlSarimaOrders();
            orders.setP(v.getP());
            orders.setD(v.getD());
            orders.setQ(v.getQ());
            orders.setBP(v.getBP());
            orders.setBD(v.getBD());
            orders.setBQ(v.getBQ());
            xml.setOrders(orders);
        } else {
            XmlSarimaPolynomials polynomials = new XmlSarimaPolynomials();
            polynomials.setD(v.getD());
            polynomials.setBD(v.getBD());
            polynomials.setAR(v.getPhi());
            polynomials.setMA(v.getTheta());
            polynomials.setBAR(v.getBPhi());
            polynomials.setBMA(v.getBTheta());
            xml.setPolynomials(polynomials);
        }
        if (v.isMean()) {
            XmlMeanCorrection xmean = new XmlMeanCorrection();
            xml.setMean(xmean);
        }
        return true;
    };

    public static final InPlaceXmlUnmarshaller<XmlArimaSpec, DefaultArimaSpec> UNMARSHALLER = (XmlArimaSpec xml, DefaultArimaSpec v) -> {
        // unspecified model
        if (xml.orders != null) {
            v.setP(xml.orders.getP());
            v.setD(xml.orders.getD());
            v.setQ(xml.orders.getQ());
            v.setBP(xml.orders.getBP());
            v.setBD(xml.orders.getBD());
            v.setBQ(xml.orders.getBQ());
        } else if (xml.polynomials != null) {
            if (xml.polynomials.getD() != null) {
                v.setD(xml.polynomials.getD());
            }
            if (xml.polynomials.getBD() != null) {
                v.setBD(xml.polynomials.getBD());
            }
            v.setPhi(xml.polynomials.getAR());
            v.setTheta(xml.polynomials.getMA());
            v.setBPhi(xml.polynomials.getBAR());
            v.setBTheta(xml.polynomials.getBMA());
        }
        if (xml.mean != null) {
            v.setMean(true);
        }else
            v.setMean(false);
        return true;
    };

}
