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
package demetra.x13.io.xml.legacy;

import demetra.regarima.RegressionSpec;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.toolkit.io.xml.legacy.regression.XmlInterventionVariable;
import demetra.toolkit.io.xml.legacy.regression.XmlOutlier;
import demetra.toolkit.io.xml.legacy.regression.XmlRamp;
import demetra.toolkit.io.xml.legacy.regression.XmlRegression;
import demetra.toolkit.io.xml.legacy.regression.XmlRegressionItem;
import demetra.toolkit.io.xml.legacy.regression.XmlRegressionVariable;
import demetra.toolkit.io.xml.legacy.regression.XmlUserVariable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for RegressionSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="RegressionSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}RegressionSpecType"&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegressionSpecType")
public class XmlRegressionSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlRegressionSpec {

    public static XmlRegressionSpec marshal(RegressionSpec v) {
        if (!v.isUsed()) {
            return null;
        }
        XmlRegressionSpec xml = new XmlRegressionSpec();
        xml.variables = new XmlRegression();
        if (v.getOutliersCount() > 0) {
            List<IOutlier> outliers = v.getOutliers();
            for (IOutlier o : outliers) {
                XmlOutlier xvar = XmlOutlier.marshal(o);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
            }
        }
        if (v.getRampsCount() > 0) {
            List<Ramp> ramps = v.getRamps();
            for (Ramp ramp : ramps) {
                XmlRegressionVariable xvar = XmlRamp.getAdapter().marshal(ramp);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
            }
        }
        if (v.getInterventionVariablesCount() > 0) {
            List<InterventionVariable> interventionVariables = v.getInterventionVariables();
            for (InterventionVariable iv : interventionVariables) {
                XmlRegressionVariable xvar = XmlInterventionVariable.getAdapter().marshal(iv);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
            }
        }
        if (v.getUserDefinedVariablesCount() > 0) {
            List<TsContextVariable> userDefinedVariables = v.getUserDefinedVariables();
            for (TsContextVariable uv : userDefinedVariables) {
                XmlUserVariable xvar = XmlUserVariable.getLegacyAdapter().marshal(uv);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
            }

        }
        return xml;
    }

    public static final RegressionSpec.Builder unmarshal(XmlRegressionSpec xml, RegressionSpec.Builder builder) {
        if (xml.variables == null || xml.variables.isEmpty()) {
            return builder;
        }
        for (XmlRegressionItem item : xml.variables.getItems()) {
            XmlRegressionVariable cur = item.getVariable();
            if (cur instanceof XmlOutlier) {
                IOutlier outlier = XmlOutlier.unmarshal((XmlOutlier) cur);
                builder = builder.outlier(outlier);
            } else if (cur instanceof XmlRamp) {
                Ramp ramp = XmlRamp.getAdapter().unmarshal((XmlRamp) cur);
                builder = builder.ramp(ramp);
            } else if (cur instanceof XmlInterventionVariable) {
                InterventionVariable ivar = XmlInterventionVariable.getAdapter().unmarshal((XmlInterventionVariable) cur);
                builder = builder.interventionVariable(ivar);
            } else if (cur instanceof XmlUserVariable) {
                TsContextVariable tcv = XmlUserVariable.getLegacyAdapter().unmarshal((XmlUserVariable) cur);
                builder = builder.userDefinedVariable(tcv);
            }
        }
        return builder;
    }
}
