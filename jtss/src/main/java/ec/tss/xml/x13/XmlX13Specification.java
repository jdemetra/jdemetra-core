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

package ec.tss.xml.x13;

import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.sa.AbstractXmlSaSpecification;
import ec.tss.xml.sa.XmlRegressionSpec;
import ec.tss.xml.uscb.AbstractXmlArimaSpec;
import ec.tss.xml.uscb.XmlBasicSpec;
import ec.tss.xml.uscb.XmlCalendarSpec;
import ec.tss.xml.uscb.XmlEstimateSpec;
import ec.tss.xml.uscb.XmlOutlierSpec;
import ec.tss.xml.uscb.XmlTransformSpec;
import ec.tss.xml.uscb.XmlX11Spec;
import ec.tstoolkit.modelling.arima.x13.EstimateSpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlX13Specification.RNAME)
@XmlType(name = XmlX13Specification.NAME)
public class XmlX13Specification extends AbstractXmlSaSpecification implements IXmlConverter<X13Specification> {

    static final String NAME = "x13SpecType";
    static final String RNAME = "x13Spec";
    @XmlElement
    public XmlBasicSpec basicSpec;
    @XmlElement
    public XmlTransformSpec transformSpec;
    @XmlElements({
        @XmlElement(name = "autoModelSpec", type = XmlAutoModelSpec.class),
        @XmlElement(name = "arimaSpec", type = XmlArimaSpec.class)
    })
    public AbstractXmlArimaSpec modelSpec;
    @XmlElement
    public XmlCalendarSpec calendarSpec;
    @XmlElement
    public XmlRegressionSpec regressionSpec;
    @XmlElement
    public XmlOutlierSpec outlierSpec;
    @XmlElement
    public XmlEstimateSpec estimateSpec;
    @XmlElement
    public XmlX11Spec x11Spec;

    public XmlX13Specification() {
    }

    public void copyTo(X13Specification spec) {
        if (basicSpec != null) {
            basicSpec.initSpec(spec.getRegArimaSpecification().getBasic());
        }
        if (transformSpec != null) {
            spec.getRegArimaSpecification().setTransform(new TransformSpec());
            transformSpec.initSpec(spec.getRegArimaSpecification().getTransform());
        }
        if (calendarSpec != null) {
            spec.getRegArimaSpecification().setRegression(new RegressionSpec());
            calendarSpec.initSpec(spec.getRegArimaSpecification().getRegression());
        }
        if (modelSpec != null) {
            modelSpec.copyTo(spec);
        }

        if (regressionSpec != null) {
            if (spec.getRegArimaSpecification().getRegression() == null) {
                spec.getRegArimaSpecification().setRegression(new RegressionSpec());
            }
            RegressionSpec regs = spec.getRegArimaSpecification().getRegression();
            regs.setOutliers(regressionSpec.createOutliers());
            regs.setInterventionVariables(regressionSpec.createInterventions());
            regs.setRamps(regressionSpec.createRamps());
            regs.setUserDefinedVariables(regressionSpec.createVariables());
        }
        if (outlierSpec != null) {
            spec.getRegArimaSpecification().setOutliers(new OutlierSpec());
            outlierSpec.copyTo(spec.getRegArimaSpecification().getOutliers());
        }
        if (estimateSpec != null) {
            spec.getRegArimaSpecification().setEstimate(new EstimateSpec());
            estimateSpec.copyTo(spec.getRegArimaSpecification().getEstimate());
        }
        if (x11Spec != null) {
            spec.setX11Specification(new X11Specification());
            x11Spec.copyTo(spec.getX11Specification());
        }
    }

     @Override
    public X13Specification convert(boolean useSystem) {
        X13Specification spec=create();
        if (! useSystem)
            return spec;
        X13Specification s=spec.matchSystem();
        if (s != null)
            return s;
        else
            return spec;
    }

    @Override
    public X13Specification create() {
        X13Specification spec = new X13Specification();
        copyTo(spec);
        return spec;
    }

    @Override
    public void copy(X13Specification t) {
        basicSpec = XmlBasicSpec.create(t.getRegArimaSpecification().getBasic());
        transformSpec = XmlTransformSpec.create(t.getRegArimaSpecification().getTransform());
        calendarSpec = XmlCalendarSpec.create(t.getRegArimaSpecification().getRegression());
        modelSpec = createArima(t);
        x11Spec = XmlX11Spec.create(t.getX11Specification());
        regressionSpec = new XmlRegressionSpec();
        if (t.getRegArimaSpecification().getRegression() != null) {
            regressionSpec.add(t.getRegArimaSpecification().getRegression().getOutliers());
            regressionSpec.add(t.getRegArimaSpecification().getRegression().getRamps());
            regressionSpec.add(t.getRegArimaSpecification().getRegression().getUserDefinedVariables());
            regressionSpec.add(t.getRegArimaSpecification().getRegression().getInterventionVariables());
        }
        if (regressionSpec.isEmpty()) {
            regressionSpec = null;
        }

        outlierSpec = XmlOutlierSpec.create(t.getRegArimaSpecification().getOutliers());
        estimateSpec = XmlEstimateSpec.create(t.getRegArimaSpecification().getEstimate());
    }

    static AbstractXmlArimaSpec createArima(X13Specification spec) {
        if (spec.getRegArimaSpecification().isUsingAutoModel()) {
            return XmlAutoModelSpec.create(spec.getRegArimaSpecification().getAutoModel());
        } else  {
            return XmlArimaSpec.create(spec.getRegArimaSpecification().getArima());
        } 
    }
}
