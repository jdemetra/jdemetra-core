/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.sa.tramoseats;

import ec.demetra.xml.modelling.XmlArimaSpec;
import ec.demetra.xml.modelling.XmlModellingSpecification;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TramoSpecificationType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TramoSpecificationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}ModellingSpecificationType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Series" type="{ec/eurostat/jdemetra/sa/tramoseats}XmlSeriesSpec" minOccurs="0"/&gt;
 *         &lt;element name="Transformation" type="{ec/eurostat/jdemetra/sa/tramoseats}TransformationSpecType" minOccurs="0"/&gt;
 *         &lt;element name="Estimation" type="{ec/eurostat/jdemetra/sa/tramoseats}EstimationSpecType" minOccurs="0"/&gt;
 *         &lt;element name="Regression" type="{ec/eurostat/jdemetra/sa/tramoseats}RegressionSpecType" minOccurs="0"/&gt;
 *         &lt;element name="Calendar" type="{ec/eurostat/jdemetra/sa/tramoseats}CalendarSpecType" minOccurs="0"/&gt;
 *         &lt;element name="Outliers" type="{ec/eurostat/jdemetra/sa/tramoseats}OutlierSpecType" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Arima" type="{ec/eurostat/jdemetra/modelling}XmlArimaSpec" minOccurs="0"/&gt;
 *           &lt;element name="AutoModelling" type="{ec/eurostat/jdemetra/sa/tramoseats}XmlAutoModellingSpec" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "TramoSpecification")
@XmlType(name = "TramoSpecificationType", propOrder = {
    "series",
    "transformation",
    "estimation",
    "regression",
    "calendar",
    "outliers",
    "arima",
    "autoModelling"
})
public class XmlTramoSpecification
        extends XmlModellingSpecification {

    @XmlElement(name = "Series")
    protected XmlSeriesSpec series;
    @XmlElement(name = "Transformation")
    protected XmlTransformationSpec transformation;
    @XmlElement(name = "Estimation")
    protected XmlEstimationSpec estimation;
    @XmlElement(name = "Regression")
    protected XmlRegressionSpec regression;
    @XmlElement(name = "Calendar")
    protected XmlCalendarSpec calendar;
    @XmlElement(name = "Outliers")
    protected XmlOutliersSpec outliers;
    @XmlElement(name = "Arima")
    protected XmlArimaSpec arima;
    @XmlElement(name = "AutoModelling")
    protected XmlAutoModellingSpec autoModelling;

    /**
     * Gets the value of the series property.
     *
     * @return possible object is {@link XmlSeriesSpec }
     *
     */
    public XmlSeriesSpec getSeries() {
        return series;
    }

    /**
     * Sets the value of the series property.
     *
     * @param value allowed object is {@link XmlSeriesSpec }
     *
     */
    public void setSeries(XmlSeriesSpec value) {
        this.series = value;
    }

    /**
     * Gets the value of the transformation property.
     *
     * @return possible object is {@link XmlTransformationSpec }
     *
     */
    public XmlTransformationSpec getTransformation() {
        return transformation;
    }

    /**
     * Sets the value of the transformation property.
     *
     * @param value allowed object is {@link XmlTransformationSpec }
     *
     */
    public void setTransformation(XmlTransformationSpec value) {
        this.transformation = value;
    }

    /**
     * Gets the value of the estimation property.
     *
     * @return possible object is {@link XmlEstimationSpec }
     *
     */
    public XmlEstimationSpec getEstimation() {
        return estimation;
    }

    /**
     * Sets the value of the estimation property.
     *
     * @param value allowed object is {@link XmlEstimationSpec }
     *
     */
    public void setEstimation(XmlEstimationSpec value) {
        this.estimation = value;
    }

    /**
     * Gets the value of the regression property.
     *
     * @return possible object is {@link XmlRegressionSpec }
     *
     */
    public XmlRegressionSpec getRegression() {
        return regression;
    }

    /**
     * Sets the value of the regression property.
     *
     * @param value allowed object is {@link XmlRegressionSpec }
     *
     */
    public void setRegression(XmlRegressionSpec value) {
        this.regression = value;
    }

    /**
     * Gets the value of the calendar property.
     *
     * @return possible object is {@link XmlCalendarSpec }
     *
     */
    public XmlCalendarSpec getCalendar() {
        return calendar;
    }

    /**
     * Sets the value of the calendar property.
     *
     * @param value allowed object is {@link XmlCalendarSpec }
     *
     */
    public void setCalendar(XmlCalendarSpec value) {
        this.calendar = value;
    }

    /**
     * Gets the value of the outliers property.
     *
     * @return possible object is {@link XmlOutliersSpec }
     *
     */
    public XmlOutliersSpec getOutliers() {
        return outliers;
    }

    /**
     * Sets the value of the outliers property.
     *
     * @param value allowed object is {@link XmlOutliersSpec }
     *
     */
    public void setOutliers(XmlOutliersSpec value) {
        this.outliers = value;
    }

    /**
     * Gets the value of the arima property.
     *
     * @return possible object is {@link XmlArimaSpec }
     *
     */
    public XmlArimaSpec getArima() {
        return arima;
    }

    /**
     * Sets the value of the arima property.
     *
     * @param value allowed object is {@link XmlArimaSpec }
     *
     */
    public void setArima(XmlArimaSpec value) {
        this.arima = value;
    }

    /**
     * Gets the value of the autoModelling property.
     *
     * @return possible object is {@link XmlAutoModellingSpec }
     *
     */
    public XmlAutoModellingSpec getAutoModelling() {
        return autoModelling;
    }

    /**
     * Sets the value of the autoModelling property.
     *
     * @param value allowed object is {@link XmlAutoModellingSpec }
     *
     */
    public void setAutoModelling(XmlAutoModellingSpec value) {
        this.autoModelling = value;
    }

    public static final InPlaceXmlUnmarshaller<XmlTramoSpecification, TramoSpecification> UNMARSHALLER = (XmlTramoSpecification xml, TramoSpecification v) -> {
        if (xml.series != null) {
            XmlSeriesSpec.UNMARSHALLER.unmarshal(xml.series, v);
        }
        if (xml.transformation != null) {
            XmlTransformationSpec.UNMARSHALLER.unmarshal(xml.transformation, v.getTransform());
        }
        if (xml.estimation != null) {
            XmlEstimationSpec.UNMARSHALLER.unmarshal(xml.estimation, v.getEstimate());
        }
        if (xml.calendar != null) {
            XmlCalendarSpec.UNMARSHALLER.unmarshal(xml.calendar, v.getRegression().getCalendar());
        }
        if (xml.regression != null) {
            XmlRegressionSpec.UNMARSHALLER.unmarshal(xml.regression, v.getRegression());
        }
        if (xml.arima != null) {
            XmlArimaSpec.UNMARSHALLER.unmarshal(xml.arima, v.getArima());
        }
        if (xml.autoModelling != null) {
            XmlAutoModellingSpec.UNMARSHALLER.unmarshal(xml.autoModelling, v.getAutoModel());
        }
        if (xml.outliers != null) {
            XmlOutliersSpec.UNMARSHALLER.unmarshal(xml.outliers, v.getOutliers());
        }

        return true;
    };

    public static final InPlaceXmlMarshaller<XmlTramoSpecification, TramoSpecification> MARSHALLER = (TramoSpecification v, XmlTramoSpecification xml) -> {
        xml.series = XmlSeriesSpec.MARSHALLER.marshal(v);
        xml.transformation = XmlTransformationSpec.MARSHALLER.marshal(v.getTransform());
        xml.estimation = XmlEstimationSpec.MARSHALLER.marshal(v.getEstimate());
        xml.calendar = XmlCalendarSpec.MARSHALLER.marshal(v.getRegression().getCalendar());
        xml.regression = XmlRegressionSpec.MARSHALLER.marshal(v.getRegression());
        xml.outliers = XmlOutliersSpec.MARSHALLER.marshal(v.getOutliers());

        if (v.isUsingAutoModel()) {
            xml.autoModelling = new XmlAutoModellingSpec();
            XmlAutoModellingSpec.MARSHALLER.marshal(v.getAutoModel(), xml.autoModelling);
        } else {
            xml.arima = new XmlArimaSpec();
            XmlArimaSpec.MARSHALLER.marshal(v.getArima(), xml.arima);
        }
        return true;
    };

}
