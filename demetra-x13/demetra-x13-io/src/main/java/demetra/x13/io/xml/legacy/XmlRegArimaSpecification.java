/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved - * by the European Commission - subsequent versions of the EUPL (the "Licence");
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
package demetra.x13.io.xml.legacy;

import demetra.regarima.RegArimaSpec;
import demetra.regarima.RegressionSpec;
import demetra.toolkit.io.xml.legacy.modelling.XmlArimaSpec;
import demetra.toolkit.io.xml.legacy.modelling.XmlModellingSpecification;
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

    public static final RegArimaSpec unmarshal(XmlRegArimaSpecification xml) {
        RegressionSpec.Builder rbuilder = XmlRegressionSpec.unmarshal(xml.regression, RegressionSpec.builder());
        if (xml.calendar != null) {
            rbuilder = rbuilder.tradingDays(XmlTradingDaysSpec.unmarshal(xml.calendar.tradingDays))
                    .easter(XmlEasterSpec.unmarshal(xml.calendar.easter));
        }
        return RegArimaSpec.builder()
                .basic(XmlSeriesSpec.unmarshal(xml.series))
                .transform(XmlTransformationSpec.unmarshal(xml.transformation))
                .estimate(XmlEstimationSpec.unmarshal(xml.estimation))
                .regression(rbuilder.build())
                .arima(XmlArimaSpec.unmarshal(xml.arima))
                .autoModel(XmlAutoModellingSpec.unmarshal(xml.autoModelling))
                .outliers(XmlOutliersSpec.unmarshal(xml.outliers))
                .build();
    }

    public static final XmlRegArimaSpecification marshal(RegArimaSpec v) {
        XmlRegArimaSpecification xml=new XmlRegArimaSpecification();
        xml.series = XmlSeriesSpec.marshal(v.getBasic());
        xml.transformation = XmlTransformationSpec.marshal(v.getTransform());
        xml.estimation = XmlEstimationSpec.marshal(v.getEstimate());
        xml.calendar = XmlCalendarSpec.marshal(v.getRegression());
        xml.regression = XmlRegressionSpec.marshal(v.getRegression());
        xml.outliers = XmlOutliersSpec.marshal(v.getOutliers());
        if (v.isUsingAutoModel()) {
            xml.autoModelling = new XmlAutoModellingSpec();
            XmlAutoModellingSpec.marshal(v.getAutoModel(), xml.autoModelling);
        } else {
            xml.arima = new XmlArimaSpec();
            XmlArimaSpec.marshal(v.getArima(), xml.arima);
        }
        return xml;
    }
}
