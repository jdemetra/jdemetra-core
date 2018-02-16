/*
 * Copyright 2017 National Bank of Belgium
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
package jdr.spec.x13;

import ec.tstoolkit.descriptors.EnhancedPropertyDescriptor;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class RegressionSpec extends BaseRegArimaSpec {

    private ec.tstoolkit.modelling.arima.x13.RegressionSpec inner() {
        return core.getRegression();
    }

    public RegressionSpec(RegArimaSpecification spec) {
        super(spec);
    }


    public OutlierDefinition[] getPreSpecifiedOutliers() {
        return inner().getOutliers();
    }

    public void setPreSpecifiedOutliers(OutlierDefinition[] value) {
        inner().setOutliers(value);
    }

    public InterventionVariable[] getInterventionVariables() {
        return inner().getInterventionVariables();
    }

    public void setInterventionVariables(InterventionVariable[] value) {
        inner().setInterventionVariables(value);
    }

    public Ramp[] getRamps() {
        return inner().getRamps();
    }

    public void setRamps(Ramp[] value) {
        inner().setRamps(value);
    }

    public TsVariableDescriptor[] getUserDefinedVariables() {
        return inner().getUserDefinedVariables();
    }

    public void setUserDefinedVariables(TsVariableDescriptor[] value) {
        inner().setUserDefinedVariables(value);
    }

    public CalendarSpec getCalendar() {
        return new CalendarSpec(core);
    }

//    public Coefficients getFixedCoefficients() {
//        Coefficients c = new Coefficients(inner().getAllFixedCoefficients());
//        c.setAllNames(inner().getRegressionVariableNames(TsFrequency.Undefined));
//        return c;
//    }
//
//    public void setFixedCoefficients(Coefficients coeffs) {
//        inner().setAllFixedCoefficients(coeffs.getFixedCoefficients());
//    }
    
}
