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
package ec.demetra.xml.sa.x13;

import ec.demetra.xml.modelling.XmlArimaSpec;
import ec.demetra.xml.modelling.XmlModellingSpecification;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Mats Maggi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RegArimaSpecification")
@XmlType(name = "RegArimaSpecificationType", propOrder = {
    "series",
    "transformation",
    "estimation",
    "regression",
    "calendar",
    "outliers",
    "arima",
    "autoModelling"
})
public class XmlRegArimaSpecification extends XmlModellingSpecification {

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

    public XmlSeriesSpec getSeries() {
        return series;
    }

    public void setSeries(XmlSeriesSpec series) {
        this.series = series;
    }

    public XmlTransformationSpec getTransformation() {
        return transformation;
    }

    public void setTransformation(XmlTransformationSpec transformation) {
        this.transformation = transformation;
    }

    public XmlEstimationSpec getEstimation() {
        return estimation;
    }

    public void setEstimation(XmlEstimationSpec estimation) {
        this.estimation = estimation;
    }

    public XmlRegressionSpec getRegression() {
        return regression;
    }

    public void setRegression(XmlRegressionSpec regression) {
        this.regression = regression;
    }

    public XmlCalendarSpec getCalendar() {
        return calendar;
    }

    public void setCalendar(XmlCalendarSpec calendar) {
        this.calendar = calendar;
    }

    public XmlOutliersSpec getOutliers() {
        return outliers;
    }

    public void setOutliers(XmlOutliersSpec outliers) {
        this.outliers = outliers;
    }

    public XmlArimaSpec getArima() {
        return arima;
    }

    public void setArima(XmlArimaSpec arima) {
        this.arima = arima;
    }

    public XmlAutoModellingSpec getAutoModelling() {
        return autoModelling;
    }

    public void setAutoModelling(XmlAutoModellingSpec autoModelling) {
        this.autoModelling = autoModelling;
    }
    
    public static final InPlaceXmlUnmarshaller<XmlRegArimaSpecification, RegArimaSpecification> UNMARSHALLER = (XmlRegArimaSpecification xml, RegArimaSpecification v) -> {
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

    public static final InPlaceXmlMarshaller<XmlRegArimaSpecification, RegArimaSpecification> MARSHALLER = (RegArimaSpecification v, XmlRegArimaSpecification xml) -> {
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
