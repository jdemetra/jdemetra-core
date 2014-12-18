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
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.sa.AbstractXmlSaSpecification;
import ec.tss.xml.sa.XmlRegressionSpec;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlTramoSeatsSpecification.RNAME)
@XmlType(name = XmlTramoSeatsSpecification.NAME)
public class XmlTramoSeatsSpecification extends AbstractXmlSaSpecification implements IXmlConverter<TramoSeatsSpecification> {

    static final String NAME = "tramoseatsSpecType";
    static final String RNAME = "tramoseatsSpec";
    @XmlElement
    public XmlTransformSpec transformSpec;
    @XmlElement
    public XmlOutlierSpec outlierSpec;
    @XmlElements(value = {
        @XmlElement(name = "autoModelSpec", type = XmlAutoModelSpec.class),
        @XmlElement(name = "arimaSpec", type = XmlArimaSpec.class)
    })
    public AbstractXmlArimaSpec modelSpec;
    @XmlElement
    public XmlCalendarSpec calendarSpec;
    @XmlElement
    public XmlRegressionSpec regressionSpec;
    @XmlElement
    public XmlEstimateSpec estimateSpec;
    @XmlElement
    public XmlSeatsSpec decompositionSpec;

    public XmlTramoSeatsSpecification() {
    }

    public void copyTo(TramoSeatsSpecification spec) {
        if (transformSpec != null) {
            transformSpec.copyTo(spec);
        }

        if (outlierSpec != null) {
            outlierSpec.copyTo(spec);
        }

        if (modelSpec != null) {
            modelSpec.copyTo(spec);
        }

        if (regressionSpec != null) {
            RegressionSpec regs = spec.getTramoSpecification().getRegression();
            regs.setOutliers(regressionSpec.createOutliers());
            regs.setInterventionVariables(regressionSpec.createInterventions());
            regs.setRamps(regressionSpec.createRamps());
            regs.setUserDefinedVariables(regressionSpec.createVariables());
        }

        if (calendarSpec != null) {
            calendarSpec.copyTo(spec);
        }

        if (estimateSpec != null) {
            spec.getTramoSpecification().setEstimate(estimateSpec.create());
        }

        if (decompositionSpec != null) {
            spec.setSeatsSpecification(decompositionSpec.create());
        }
    }

    @Override
    public TramoSeatsSpecification convert(boolean useSystem) {
        TramoSeatsSpecification spec = create();
        if (!useSystem) {
            return spec;
        }
        TramoSeatsSpecification s = spec.matchSystem();
        if (s != null) {
            return s;
        }
        else {
            return spec;
        }
    }

    @Override
    public TramoSeatsSpecification create() {
        TramoSeatsSpecification spec = new TramoSeatsSpecification();
        copyTo(spec);
        return spec;
    }

    @Override
    public void copy(TramoSeatsSpecification t) {
        transformSpec = XmlTransformSpec.create(t.getTramoSpecification().getTransform());
        modelSpec = AbstractXmlArimaSpec.create(t.getTramoSpecification());
        outlierSpec = XmlOutlierSpec.create(t.getTramoSpecification().getOutliers());
        calendarSpec = XmlCalendarSpec.create(t.getTramoSpecification().getRegression());
        regressionSpec = new XmlRegressionSpec();
        regressionSpec.add(t.getTramoSpecification().getRegression().getOutliers());
        regressionSpec.add(t.getTramoSpecification().getRegression().getRamps());
        regressionSpec.add(t.getTramoSpecification().getRegression().getUserDefinedVariables());
        regressionSpec.add(t.getTramoSpecification().getRegression().getInterventionVariables());
        if (regressionSpec.isEmpty()) {
            regressionSpec = null;
        }
        if (!t.getTramoSpecification().getEstimate().isDefault()) {
            estimateSpec = new XmlEstimateSpec();
            estimateSpec.copy(t.getTramoSpecification().getEstimate());
        }
        decompositionSpec = new XmlSeatsSpec();
        decompositionSpec.copy(t.getSeatsSpecification());
    }
}
