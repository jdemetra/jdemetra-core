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

import demetra.modelling.regression.InterventionVariable;
import demetra.modelling.regression.Ramp;
import demetra.modelling.regression.UserVariable;
import demetra.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import demetra.modelling.regression.IOutlier;

/**
 *
 * @author Jean Palate
 */
public class RegressionSpec {

    private CalendarSpec calendar; 
    private final ArrayList<IOutlier> outliers = new ArrayList<>();
    private final ArrayList<Ramp> ramps = new ArrayList<>();
    private final ArrayList<InterventionVariable> interventions = new ArrayList<>();
    private final ArrayList<UserVariable> users = new ArrayList<>();
     // the maps with the coefficients use short names...
    private final Map<String, double[]> fcoeff = new LinkedHashMap<>();
    private final Map<String, double[]> coeff = new LinkedHashMap<>();
   
    public RegressionSpec(){
        calendar = new CalendarSpec();
    }

    public RegressionSpec(RegressionSpec other){
        calendar=new CalendarSpec(other.calendar);
        outliers.addAll(other.outliers);
        ramps.addAll(other.ramps);
        interventions.addAll(other.interventions);
        users.addAll(other.users);
        other.coeff.forEach(coeff::put);
        other.fcoeff.forEach(fcoeff::put);
    }

    public void reset() {
        outliers.clear();
        ramps.clear();
        interventions.clear();
        users.clear();
        coeff.clear();
        fcoeff.clear();
    }
    
    
    public Map<String, double[]> getAllFixedCoefficients() {
//        checkFixedCoefficients();
        return Collections.unmodifiableMap(fcoeff);
    }

    public Map<String, double[]> getAllCoefficients() {
        return Collections.unmodifiableMap(coeff);
    }


    public boolean isUsed() {
        return calendar.isUsed() || !outliers.isEmpty()
                || !ramps.isEmpty() || !interventions.isEmpty()
                || !users.isEmpty();
    }

    public CalendarSpec getCalendar() {
        return calendar;
    }

    public void setCalendar(@Nonnull CalendarSpec value) {
        calendar = value;
    }

    public IOutlier[] getOutliers() {
        return outliers.toArray(new IOutlier[outliers.size()]);
    }

    public void setOutliers(@Nonnull IOutlier[] value) {
        outliers.clear();
        Collections.addAll(outliers, value);
    }

    public UserVariable[] getUserDefinedVariables() {
        return users.toArray(new UserVariable[users.size()]);
    }

    public void setUserDefinedVariables(@Nonnull UserVariable[] value) {
        users.clear();
        Collections.addAll(users, value);
    }

    public InterventionVariable[] getInterventionVariables() {
        return interventions.toArray(new InterventionVariable[interventions.size()]);
    }

    public void setInterventionVariables(@Nonnull InterventionVariable[] value) {
        Collections.addAll(interventions, value);
    }

    public boolean isDefault() {
        return calendar.isDefault() && users.isEmpty()
                && outliers.isEmpty() && interventions.isEmpty();
    }

    public void add(UserVariable svar) {
        users.add(svar);
    }

    public void add(IOutlier outlier) {
        outliers.add(outlier);
    }

    public void add(InterventionVariable ivar) {
        interventions.add(ivar);
    }

    public boolean contains(IOutlier outlier) {
        return outliers.stream().anyMatch((def) -> (def.equals(outlier)));
    }

    public void clearUserDefinedVariables() {
        users.clear();
    }

    public int getUserDefinedVariablesCount() {
        return users.size();
    }

    public UserVariable getUserDefinedVariable(int idx) {
        return users.get(idx);
    }

    public void clearOutliers() {
        outliers.clear();
    }

    public int getOutliersCount() {
        return outliers.size();
    }

    public IOutlier getOutlier(int idx) {
        return outliers.get(idx);
    }

    public void clearInterventionVariables() {
        interventions.clear();
    }

    public int getInterventionVariablesCount() {
        return interventions.size();
    }

    public InterventionVariable getInterventionVariable(int idx) {
        return interventions.get(idx);
    }

    public void clearRamps() {
        ramps.clear();
    }

    public int getRampsCount() {
        return ramps.size();
    }

    public Ramp getRamp(int idx) {
        return ramps.get(idx);
    }

    public void add(Ramp rp) {
        ramps.add(rp);
    }

    public Ramp[] getRamps() {
        return ramps.toArray(new Ramp[ramps.size()]);
    }

    public void setRamps(@Nonnull Ramp[] value) {
        ramps.clear();
        Collections.addAll(ramps, value);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof RegressionSpec && equals((RegressionSpec) obj));
    }

    private boolean equals(RegressionSpec other) {
        return Comparator.equals(interventions, other.interventions)
                && Comparator.equals(users, other.users)
                && Comparator.equals(ramps, other.ramps)
                && Comparator.equals(outliers, other.outliers)
                && Objects.equals(calendar, other.calendar);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + calendar.hashCode();
        return hash;
    }
}
