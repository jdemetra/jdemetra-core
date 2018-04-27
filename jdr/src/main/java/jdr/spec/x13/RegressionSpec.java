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

import demetra.information.InformationExtractor;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.simplets.TsData;
import jdr.spec.ts.Utility;
import jdr.spec.ts.Utility.Outlier;
import jdr.spec.ts.Utility.UserDefinedVariable;

/**
 *
 */
public class RegressionSpec extends BaseRegArimaSpec {

    private ec.tstoolkit.modelling.arima.x13.RegressionSpec inner() {
        return core.getRegression();
    }

    public RegressionSpec(RegArimaSpecification spec) {
        super(spec);
    }

    public int getPrespecifiedOutliersCount() {
        return inner().getOutliersCount();
    }

    public Outlier getPrespecifiedOutlier(int i) {
        OutlierDefinition[] outliers = inner().getOutliers();
        if (outliers == null || i >= outliers.length) {
            return null;
        }
        OutlierDefinition cur = outliers[i];
        String pos = Utility.toString(cur.getPosition());
        double[] c = inner().getFixedCoefficients(Utility.outlierName(cur.getCode(), pos, 0));
        Outlier o = new Outlier(cur.getCode(), pos, c == null ? 0 : c[0]);
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
    
    public UserDefinedVariable getUserDefinedVariable(int i){
        TsVariableDescriptor[] vars = inner().getUserDefinedVariables();
        if (vars == null || i >= vars.length) {
            return null;
        }
        TsVariableDescriptor desc=vars[i];
        double[] c = inner().getFixedCoefficients(desc.getName());
        return new UserDefinedVariable(desc.getName(), desc.getEffect().name(), c == null ? 0 : c[0]);
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
