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
package ec.demetra.xml.sa.tramoseats;

import ec.demetra.xml.regression.XmlInterventionVariable;
import ec.demetra.xml.regression.XmlOutlier;
import ec.demetra.xml.regression.XmlRamp;
import ec.demetra.xml.regression.XmlRegression;
import ec.demetra.xml.regression.XmlRegressionItem;
import ec.demetra.xml.regression.XmlRegressionVariable;
import ec.demetra.xml.regression.XmlUserVariable;
import ec.tss.xml.IXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.Ramp;
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
        extends ec.demetra.xml.modelling.XmlRegressionSpec {

    public static final IXmlMarshaller<XmlRegressionSpec, RegressionSpec> MARSHALLER = (RegressionSpec v) -> {
        boolean used=v.getOutliersCount()>0 || v.getInterventionVariablesCount()>0 || v.getRampsCount()>0
                || v.getUserDefinedVariablesCount()>0;
        if (!used) {
            return null;
        }
        XmlRegressionSpec xml = new XmlRegressionSpec();
        xml.variables = new XmlRegression();
        if (v.getOutliersCount() > 0) {
            OutlierDefinition[] outliers = v.getOutliers();
            for (int i = 0; i < outliers.length; ++i) {
                XmlOutlier xvar=XmlOutlier.LEGACY_MARSHALLER.marshal(outliers[i]);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
//                IOutlierVariable var = TramoSpecification.getOutliersFactory().make(outliers[i]);
//                XmlRegressionVariable xvar = TsVariableAdapters.getDefault().marshal(var);
//                if (xvar != null) {
//                    XmlRegressionItem xcur = new XmlRegressionItem();
//                    xcur.setVariable(xvar);
//                    xml.variables.getItems().add(xcur);
//                }
            }
        }
        if (v.getRampsCount() > 0) {
            Ramp[] vars = v.getRamps();
            for (int i = 0; i < vars.length; ++i) {
                XmlRegressionVariable xvar = XmlRamp.getAdapter().marshal(vars[i]);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
            }
        }
        if (v.getInterventionVariablesCount() > 0) {
            InterventionVariable[] vars = v.getInterventionVariables();
            for (int i = 0; i < vars.length; ++i) {
                XmlRegressionVariable xvar = XmlInterventionVariable.getAdapter().marshal(vars[i]);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
            }
        }
        if (v.getUserDefinedVariablesCount() > 0) {
            TsVariableDescriptor[] vars = v.getUserDefinedVariables();
            for (int i = 0; i < vars.length; ++i) {
                XmlUserVariable xvar = XmlUserVariable.getLegacyAdapter().marshal(vars[i]);
                if (xvar != null) {
                    XmlRegressionItem xcur = new XmlRegressionItem();
                    xcur.setVariable(xvar);
                    xml.variables.getItems().add(xcur);
                }
            }

        }

        return xml;
    };

    public static final InPlaceXmlUnmarshaller<XmlRegressionSpec, RegressionSpec> UNMARSHALLER = (XmlRegressionSpec xml, RegressionSpec v) -> {
        if (xml.variables == null || xml.variables.isEmpty()) {
            return true;
        }
        for (XmlRegressionItem item : xml.variables.getItems()){
            XmlRegressionVariable cur = item.getVariable();
            if (cur instanceof XmlOutlier){
//                IOutlierVariable outlier=(IOutlierVariable) TsVariableAdapters.getDefault().unmarshal(cur);
//                v.add(outlier);
                OutlierDefinition od = XmlOutlier.LEGACY_UNMARSHALLER.unmarshal((XmlOutlier) cur);
                v.add(od);
            }else if (cur instanceof XmlRamp){
                Ramp ramp=XmlRamp.getAdapter().unmarshal((XmlRamp)cur);
                v.add(ramp);
            }else if (cur instanceof XmlInterventionVariable){
                InterventionVariable ivar=XmlInterventionVariable.getAdapter().unmarshal((XmlInterventionVariable)cur);
                v.add(ivar);
            }else if (cur instanceof XmlUserVariable){
                TsVariableDescriptor desc = XmlUserVariable.getLegacyAdapter().unmarshal((XmlUserVariable)cur);
                v.add(desc);
            }
        }
        
        return true;
    };
}
