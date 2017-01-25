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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.IEasterVariable;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Jean Palate
 */
public class RegressionSpec implements Cloneable, InformationSetSerializable {

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, OUTLIERS), String[].class);
        dic.put(InformationSet.item(prefix, RAMPS), String[].class);
        CalendarSpec.fillDictionary(InformationSet.item(prefix, CALENDAR), dic);
        InterventionVariable.fillDictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
        TsVariableDescriptor.fillDictionary(InformationSet.item(prefix, USERS), dic);
    }
   
    private CalendarSpec calendar_ = new CalendarSpec();
    private ArrayList<OutlierDefinition> outliers_ = new ArrayList<>();
    private ArrayList<Ramp> ramps_ = new ArrayList<>();
    private ArrayList<InterventionVariable> interventions_ = new ArrayList<>();
    private ArrayList<TsVariableDescriptor> users_ = new ArrayList<>();
    private Map<String, double[]> fcoeff = new LinkedHashMap<>();
    private Map<String, double[]> coeff = new LinkedHashMap<>();

    public void reset() {
        outliers_.clear();
        ramps_.clear();
        interventions_.clear();
        users_.clear();
        fcoeff.clear();
        coeff.clear();
    }

    public boolean isUsed() {
        return calendar_.isUsed() || !outliers_.isEmpty()
                || !ramps_.isEmpty() || !interventions_.isEmpty()
                || !users_.isEmpty();
    }

    public CalendarSpec getCalendar() {
        return calendar_;
    }

    public void setCalendar(CalendarSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(CALENDAR);
        }
        calendar_ = value;
    }

    public OutlierDefinition[] getOutliers() {
        return Jdk6.Collections.toArray(outliers_, OutlierDefinition.class);
    }

    public void setOutliers(OutlierDefinition[] value) {
        outliers_.clear();
        if (value != null) {
            Collections.addAll(outliers_, value);
        }
    }

    public TsVariableDescriptor[] getUserDefinedVariables() {
        return Jdk6.Collections.toArray(users_, TsVariableDescriptor.class);
    }

    public void setUserDefinedVariables(TsVariableDescriptor[] value) {
        users_.clear();
        if (value != null) {
            Collections.addAll(users_, value);
        }
    }

    public InterventionVariable[] getInterventionVariables() {
        return Jdk6.Collections.toArray(interventions_, InterventionVariable.class);
    }

    public void setInterventionVariables(InterventionVariable[] value) {
        interventions_.clear();
        if (value != null) {
            Collections.addAll(interventions_, value);
        }
    }

    public boolean isDefault() {
        return calendar_.isDefault() && users_.isEmpty()
                && outliers_.isEmpty() && interventions_.isEmpty();
    }

    public void add(TsVariableDescriptor svar) {
        users_.add(svar);
    }

    public void add(OutlierDefinition outlier) {
        outliers_.add(outlier);
    }

    public boolean contains(OutlierDefinition outlier) {
        for (OutlierDefinition def : outliers_) {
            if (def.equals(outlier)) {
                return true;
            }
        }
        return false;
    }

    public void add(IOutlierVariable outlier) {
        outliers_.add(new OutlierDefinition(outlier.getPosition(), outlier.getCode()));
    }

    public void add(InterventionVariable ivar) {
        interventions_.add(ivar);
    }

    public void clearUserDefinedVariables() {
        users_.clear();
    }

    public int getUserDefinedVariablesCount() {
        return users_.size();
    }

    public TsVariableDescriptor getUserDefinedVariable(int idx) {
        return users_.get(idx);
    }

    public void clearOutliers() {
        outliers_.clear();
    }

    public int getOutliersCount() {
        return outliers_.size();
    }

    public OutlierDefinition getOutlier(int idx) {
        return outliers_.get(idx);
    }

    public void clearInterventionVariables() {
        interventions_.clear();
    }

    public int getInterventionVariablesCount() {
        return interventions_.size();
    }

    public InterventionVariable getInterventionVariable(int idx) {
        return interventions_.get(idx);
    }

    public String[] getRegressionVariableNames(TsFrequency freq) {
        ArrayList<String> names = new ArrayList<>();
        if (calendar_.isUsed()) {
            // calendar
            if (calendar_.getTradingDays().isDefined()) {

                if (calendar_.getTradingDays().isStockTradingDays()) {
                    names.add(ITradingDaysVariable.NAME);
                }
                String[] user = calendar_.getTradingDays().getUserVariables();
                if (user != null) {
                    names.add(ITradingDaysVariable.NAME + '#' + user.length);
                } else {
                    if (calendar_.getTradingDays().getTradingDaysType() == TradingDaysType.WorkingDays) {
                        names.add(ITradingDaysVariable.NAME);
                    } else {
                        names.add(ITradingDaysVariable.NAME + "#6");
                    }
                    if (calendar_.getTradingDays().isLeapYear()) {
                        names.add(ILengthOfPeriodVariable.NAME);
                    }
                }
            }
            // easter
            if (calendar_.getEaster().isDefined()) {
                names.add(IEasterVariable.NAME);
            }
        }
        // outliers
        outliers_.stream().map((def) -> {
            StringBuilder builder = new StringBuilder();
            builder.append(def.getCode()).append(" (");
            if (freq == TsFrequency.Undefined) {
                builder.append(def.getPosition());
            } else {
                TsPeriod p = new TsPeriod(freq, def.getPosition());
                builder.append(p);
            }
            return builder.append(')');
        }).forEach((builder) -> {
            names.add(builder.toString());
        });

        // ramp
        ramps_.forEach(rp -> names.add(rp.getName()));

        // intervention
        interventions_.forEach(iv
                -> {
            String n = iv.getName();
            if (names.contains(n)) {
                n += '*';
            }
            names.add(n);
        });

        // user
        users_.forEach(uv
                -> {
            int n = uv.getLastLag() - uv.getFirstLag() + 1;
            if (n == 1) {
                names.add(uv.getName());
            } else {
                names.add(uv.getName() + '#' + n);
            }
        });
        String[] all = new String[names.size()];
        return names.toArray(all);
    }

    public double[] getCoefficients(String name) {
        return coeff.get(name);
    }

    public void setCoefficients(String name, double[] c) {
        coeff.put(name, c);
    }

    public void clearAllCoefficients() {
        coeff.clear();
    }

    public void clearCoefficients(String name) {
        coeff.remove(name);
    }
    
    public double[] getFixedCoefficients(String name) {
        return fcoeff.get(name);
    }
    
    public void setAllFixedCoefficients(Map<String, double[]> coeffs) {
        clearAllFixedCoefficients();
        fcoeff.putAll(coeffs);
    }
    
    public Map<String, double[]> getAllFixedCoefficients(){
        return Collections.unmodifiableMap(fcoeff);
    }

    public Map<String, double[]> getAllCoefficients(){
        return Collections.unmodifiableMap(coeff);
    }

    public void setFixedCoefficients(String name, double[] c) {
        fcoeff.put(name, c);
    }

    public void clearAllFixedCoefficients() {
        fcoeff.clear();
    }

    public void clearFixedCoefficients(String name) {
        fcoeff.remove(name);
    }

    public void clearRamps() {
        ramps_.clear();
    }

    public int getRampsCount() {
        return ramps_.size();
    }

    public Ramp getRamp(int idx) {
        return ramps_.get(idx);
    }

    public void add(Ramp rp) {
        ramps_.add(rp);
    }

    public Ramp[] getRamps() {
        return Jdk6.Collections.toArray(ramps_, Ramp.class);
    }

    public void setRamps(Ramp[] value) {
        ramps_.clear();
        if (value != null) {
            Collections.addAll(ramps_, value);
        }
    }

    @Override
    public RegressionSpec clone() {
        try {
            RegressionSpec spec = (RegressionSpec) super.clone();
            spec.interventions_ = new ArrayList<>();
            for (InterventionVariable ii : interventions_) {
                spec.interventions_.add(ii.clone());
            }
            spec.outliers_ = Jdk6.newArrayList(outliers_);
            spec.ramps_ = new ArrayList<>();
            for (Ramp r : ramps_) {
                spec.ramps_.add(r.clone());
            }
            spec.calendar_ = calendar_.clone();
            spec.users_ = new ArrayList<>();
            users_.forEach(var -> spec.users_.add(var.clone()));
            spec.fcoeff = new LinkedHashMap<>();
            fcoeff.forEach((name, c) -> spec.fcoeff.put(name, c));
            spec.coeff = new LinkedHashMap<>();
            coeff.forEach((name, c) -> spec.coeff.put(name, c));
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof RegressionSpec && equals((RegressionSpec) obj));
    }

    private boolean equals(RegressionSpec other) {
        return Comparator.equals(interventions_, other.interventions_)
                && Comparator.equals(users_, other.users_)
                && Comparator.equals(ramps_, other.ramps_)
                && Comparator.equals(outliers_, other.outliers_)
                && Objects.equals(calendar_, other.calendar_)
                && compare(fcoeff, other.fcoeff);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + calendar_.hashCode();
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!isUsed()) {
            return null;
        }
        InformationSet specInfo = new InformationSet();
        if (verbose || !calendar_.isDefault()) {
            InformationSet cinfo = calendar_.write(verbose);
            if (cinfo != null) {
                specInfo.add(CALENDAR, cinfo);
            }
        }
        if (!outliers_.isEmpty()) {
            String[] outliers = new String[outliers_.size()];
            for (int i = 0; i < outliers.length; ++i) {
                outliers[i] = outliers_.get(i).toString();
            }
            specInfo.add(OUTLIERS, outliers);
        }
        if (!ramps_.isEmpty()) {
            String[] ramps = new String[ramps_.size()];
            for (int i = 0; i < ramps.length; ++i) {
                ramps[i] = ramps_.get(i).toString();
            }
            specInfo.add(RAMPS, ramps);
        }
        int idx = 1;
        for (TsVariableDescriptor desc : users_) {
            InformationSet cur = desc.write(verbose);
            if (cur != null) {
                specInfo.add(USER + Integer.toString(idx++), cur);
            }
        }
        idx = 1;
        for (InterventionVariable ivar : interventions_) {
            InformationSet cur = ivar.write(verbose);
            if (cur != null) {
                specInfo.add(INTERVENTION + Integer.toString(idx++), cur);
            }
        }
        if (! fcoeff.isEmpty()){
            InformationSet icoeff = specInfo.subSet(FCOEFF);
            fcoeff.forEach((s,c)->icoeff.set(s, c.length == 1 ? c[0] : c));
        }
        if (! coeff.isEmpty()){
            InformationSet icoeff = specInfo.subSet(COEFF);
            coeff.forEach((s,c)->icoeff.set(s, c.length == 1 ? c[0] : c));
        }
        return specInfo;
    }

    @Override
    public boolean read(InformationSet info) {
        reset();
        InformationSet cinfo = info.getSubSet(CALENDAR);
        if (cinfo != null) {
            boolean tok = calendar_.read(cinfo);
            if (!tok) {
                return false;
            }
        }
        String[] outliers = info.get(OUTLIERS, String[].class);
        if (outliers != null) {
            for (int i = 0; i < outliers.length; ++i) {
                OutlierDefinition o = OutlierDefinition.fromString(outliers[i]);
                if (o != null) {
                    outliers_.add(o);
                } else {
                    return false;
                }
            }
        }
        String[] ramps = info.get(RAMPS, String[].class);
        if (ramps != null) {
            for (int i = 0; i < ramps.length; ++i) {
                Ramp r = Ramp.fromString(ramps[i]);
                if (r != null) {
                    ramps_.add(r);
                } else {
                    return false;
                }
            }
        }
        List<Information<InformationSet>> usel = info.select(USERS, InformationSet.class);
        for (Information<InformationSet> item : usel) {
            TsVariableDescriptor cur = new TsVariableDescriptor();
            if (cur.read(item.value)) {
                users_.add(cur);
            }
        }
        List<Information<InformationSet>> isel = info.select(INTERVENTIONS, InformationSet.class);
        for (Information<InformationSet> item : isel) {
            InterventionVariable cur = new InterventionVariable();
            if (cur.read(item.value)) {
                interventions_.add(cur);
            }
        }
        InformationSet ifcoeff=info.getSubSet(FCOEFF);
        if (ifcoeff != null){
            List<Information<double[]>> all = ifcoeff.select(double[].class);
            all.stream().forEach(reg -> fcoeff.put(reg.name, reg.value));
            List<Information<Double>> sall = ifcoeff.select(Double.class);
            sall.stream().forEach(reg -> fcoeff.put(reg.name, new double[]{reg.value}));
        }
        InformationSet icoeff=info.getSubSet(COEFF);
        if (icoeff != null){
            List<Information<double[]>> all = icoeff.select(double[].class);
            all.stream().forEach(reg -> coeff.put(reg.name, reg.value));
            List<Information<Double>> sall = icoeff.select(Double.class);
            sall.stream().forEach(reg -> coeff.put(reg.name, new double[]{reg.value}));
        }
        return true;
    }

//    @Override
//    public void fillDictionary(String prefix, List<String> dic) {
//        calendar_.fillDictionary(InformationSet.item(prefix, CALENDAR), dic);
//        dic.add(InformationSet.item(prefix, OUTLIERS));
//        dic.add(InformationSet.item(prefix, RAMPS));
//        TsVariableDescriptor.dictionary(InformationSet.item(prefix, USERS), dic);
//        InterventionVariable.dictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
//    }
    public static final String CALENDAR = "calendar",
            OUTLIERS = "outliers",
            USER = "user", USERS = "user*", RAMPS = "ramps",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*", 
            COEFF="coefficients", FCOEFF="fixedcoefficients";
    private static final String[] DICTIONARY = new String[]{
        CALENDAR, OUTLIERS, USERS, RAMPS, INTERVENTIONS, COEFF, FCOEFF
    };

    private boolean compare(Map<String, double[]> cl, Map<String, double[]> cr) {
        if (cl.size() != cr.size())
            return false;
        Optional<Map.Entry<String, double[]>>any = cl.entrySet().stream().filter(entry-> {
            if (!cr.containsKey(entry.getKey()))
                return true;
            else 
                return !Arrays.equals(entry.getValue(), cr.get(entry.getKey()));
        }).findAny();
        
        return !any.isPresent();
    }
}
