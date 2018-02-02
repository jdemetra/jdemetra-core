/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;

/**
 *
 * @author Jean Palate
 */
public class RegressionSpec extends BaseTramoSpec {

    private ec.tstoolkit.modelling.arima.tramo.RegressionSpec inner() {
        return core.getRegression();
    }

    public RegressionSpec(TramoSpecification spec) {
        super(spec);
    }

//    public OutlierDefinition[] getPreSpecifiedOutliers() {
//        return inner().getOutliers();
//    }
//
//    public void setPreSpecifiedOutliers(OutlierDefinition[] value) {
//        inner().setOutliers(value);
//    }
//
//    public InterventionVariable[] getInterventionVariables() {
//        return inner().getInterventionVariables();
//    }
//
//    public void setInterventionVariables(InterventionVariable[] value) {
//        inner().setInterventionVariables(value);
//    }
//
//    public Ramp[] getRamps() {
//        return inner().getRamps();
//    }
//
//    public void setRamps(Ramp[] value) {
//        inner().setRamps(value);
//    }
//
//    public TsVariableDescriptor[] getUserDefinedVariables() {
//        return inner().getUserDefinedVariables();
//    }
//
//    public void setUserDefinedVariables(TsVariableDescriptor[] value) {
//        inner().setUserDefinedVariables(value);
//    }
//    
//    public Coefficients getFixedCoefficients() {
//        Coefficients c = new Coefficients(inner().getAllFixedCoefficients());
//        c.setAllNames(inner().getRegressionVariableNames(TsFrequency.Undefined));
//        return c;
//    }
//
//    public void setFixedCoefficients(Coefficients coeffs) {
//        inner().setAllFixedCoefficients(coeffs.getFixedCoefficients());
//    }
    
    public CalendarSpec getCalendar() {
        return new CalendarSpec(core);
    }
}
