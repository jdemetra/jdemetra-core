/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import demetra.information.InformationExtractor;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import jdr.spec.ts.Utility;

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

    public int getPrespecifiedOutliersCount() {
        return inner().getOutliersCount();
    }

    public Utility.Outlier getPrespecifiedOutlier(int i) {
        OutlierDefinition[] outliers = inner().getOutliers();
        if (outliers == null || i >= outliers.length) {
            return null;
        }
        OutlierDefinition cur = outliers[i];
        String pos = Utility.toString(cur.getPosition());
        double[] c = inner().getFixedCoefficients(Utility.outlierName(cur.getCode(), pos, 0));
        Utility.Outlier o = new Utility.Outlier(cur.getCode(), pos, c == null ? 0 : c[0]);
        return o;
    }

    public void clearPrespecifiedOutliers() {
        inner().clearOutliers();
    }

    public void addPrespecifiedOutlier(String code, String date, double coef) {
        Day pos = Utility.of(date);
        OutlierDefinition def = new OutlierDefinition(pos, code);
        inner().add(def);
        if (coef != 0 && Double.isFinite(coef)) {
            String on = Utility.outlierName(code, date, 0);
            inner().setFixedCoefficients(on, new double[]{coef});
        }
    }

    public int getUserDefinedVariablesCount() {
        return inner().getUserDefinedVariablesCount();
    }
    
    public Utility.UserDefinedVariable getUserDefinedVariable(int i){
        TsVariableDescriptor[] vars = inner().getUserDefinedVariables();
        if (vars == null || i >= vars.length) {
            return null;
        }
        TsVariableDescriptor desc=vars[i];
        double[] c = inner().getFixedCoefficients(desc.getName());
        return new Utility.UserDefinedVariable(desc.getName(), desc.getEffect().name(), c == null ? 0 : c[0]);
    }

    public void clearUserDefinedVariables() {
        inner().clearUserDefinedVariables();
    }

    public void addUserDefinedVariable(String name, String effect, double coef) {
        TsVariableDescriptor desc=new TsVariableDescriptor(InformationExtractor.concatenate(Utility.R, name));
        desc.setEffect(TsVariableDescriptor.UserComponentType.from(effect));
        inner().add(desc);
        if (coef != 0 && Double.isFinite(coef)) {
            inner().setFixedCoefficients(Utility.RPREFIX+name, new double[]{coef});
        }
    }
    
    public CalendarSpec getCalendar() {
        return new CalendarSpec(core);
    }
}
