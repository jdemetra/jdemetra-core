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
package demetra.tramo;

import demetra.design.Development;
import demetra.modelling.regression.InterventionVariable;
import demetra.modelling.regression.Ramp;
import demetra.modelling.regression.UserVariable;
import demetra.modelling.regression.IOutlier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Data
public final class RegressionSpec implements Cloneable {

    private static final IOutlier[] NOOUTLIER = new IOutlier[0];
    private static final Ramp[] NORAMP = new Ramp[0];
    private static final InterventionVariable[] NOII = new InterventionVariable[0];
    private static final UserVariable[] NOUSER = new UserVariable[0];

    private CalendarSpec calendar;
    private IOutlier[] outliers = NOOUTLIER;
    private Ramp[] ramps = NORAMP;
    private InterventionVariable[] interventionVariables = NOII;
    private UserVariable[] userDefinedVariables = NOUSER;
    // the maps with the coefficients use short names...
    private Map<String, double[]> fixedCoefficients = new LinkedHashMap<>();
    private Map<String, double[]> coefficients = new LinkedHashMap<>();
    
    private static final RegressionSpec DEFAULT=new RegressionSpec();

    public RegressionSpec() {
        calendar = new CalendarSpec();
    }

    @Override
    public RegressionSpec clone() {
        try {
            RegressionSpec c = (RegressionSpec) super.clone();
            c.calendar = calendar.clone();
            c.coefficients=new LinkedHashMap();
            c.coefficients.putAll(coefficients);
            c.fixedCoefficients=new LinkedHashMap();
            c.fixedCoefficients.putAll(fixedCoefficients);
            return c;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public void reset() {
        outliers = NOOUTLIER;
        ramps = NORAMP;
        interventionVariables = NOII;
        userDefinedVariables = NOUSER;
        coefficients.clear();
        fixedCoefficients.clear();
    }

    public boolean isUsed() {
        return calendar.isUsed() || outliers.length > 0
                || ramps.length > 0 || interventionVariables.length > 0 || userDefinedVariables.length > 0;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public int getUserDefinedVariablesCount() {
        return userDefinedVariables.length;
    }

    public UserVariable getUserDefinedVariable(int idx) {
        return userDefinedVariables[idx];
    }

     public int getOutliersCount() {
        return outliers.length;
    }

    public IOutlier getOutlier(int idx) {
        return outliers[idx];
    }

    public int getInterventionVariablesCount() {
        return interventionVariables.length;
    }

    public InterventionVariable getInterventionVariable(int idx) {
        return interventionVariables[idx];
    }

    public int getRampsCount() {
        return ramps.length;
    }

    public Ramp getRamp(int idx) {
        return ramps[idx];
    }

}
