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
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import java.util.*;
import java.util.stream.Collectors;

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
    // the maps with the coefficients use short names...
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
        return outliers_.stream().anyMatch((def) -> (def.equals(outlier)));
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
        return getRegressionVariableNames(freq, false);
    }

    public String[] getRegressionVariableShortNames(TsFrequency freq) {
        return getRegressionVariableNames(freq, true);
    }

    private String[] getRegressionVariableNames(TsFrequency freq, boolean shortname) {
        ArrayList<String> names = new ArrayList<>();
        if (calendar_.isUsed()) {
            // calendar
            if (calendar_.getTradingDays().isDefined()) {

                if (calendar_.getTradingDays().isStockTradingDays()) {
                    if (shortname) {
                        names.add(ITradingDaysVariable.NAME);
                    } else {
                        names.add(ITradingDaysVariable.NAME + "#6");
                    }
                } else {
                    String[] user = calendar_.getTradingDays().getUserVariables();
                    if (user != null) {
//                        if (shortname) {
//                            names.add("td");
//                        } else {
                        for (String username : user) {
                            if (!username.startsWith("td|")) {
                                username = "td|" + username;
                            }
                            names.add(ITsVariable.validName(username));
                        }
//                        }
                    } else {
                        if (calendar_.getTradingDays().getTradingDaysType() == TradingDaysType.WorkingDays || shortname) {
                            names.add(ITradingDaysVariable.NAME);
                        } else {
                            names.add(ITradingDaysVariable.NAME + "#6");
                        }
                        if (calendar_.getTradingDays().isLeapYear()) {
                            names.add(ILengthOfPeriodVariable.NAME);
                        }
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
            if (n == 1 || shortname) {
                names.add(validName(uv.getName()));
            } else {
                names.add(validName(uv.getName()) + '#' + n);
            }
        });
        String[] all = new String[names.size()];
        return names.toArray(all);
    }

    private static String validName(String name) {
        return name.replace('.', '@');
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
//        checkFixedCoefficients();
        return fcoeff.get(name);
    }

    public void setAllFixedCoefficients(Map<String, double[]> coeffs) {
        clearAllFixedCoefficients();
        fcoeff.putAll(coeffs);
    }

    public Map<String, double[]> getAllFixedCoefficients() {
        checkFixedCoefficients();
        return Collections.unmodifiableMap(fcoeff);
    }

    public Map<String, double[]> getAllCoefficients() {
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
            interventions_.forEach(ii -> spec.interventions_.add(ii.clone()));
            spec.outliers_ = Jdk6.newArrayList(outliers_);
            spec.ramps_ = new ArrayList<>();
            ramps_.forEach(r -> spec.ramps_.add(r.clone()));
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
        if (!fcoeff.isEmpty()) {
            InformationSet icoeff = specInfo.subSet(FCOEFF);
            fcoeff.forEach((s, c) -> icoeff.set(s, c.length == 1 ? c[0] : c));
        }
        if (!coeff.isEmpty()) {
            InformationSet icoeff = specInfo.subSet(COEFF);
            coeff.forEach((s, c) -> icoeff.set(s, c.length == 1 ? c[0] : c));
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
        usel.forEach((item) -> {
            TsVariableDescriptor cur = new TsVariableDescriptor();
            if (cur.read(item.value)) {
                users_.add(cur);
            }
        });
        List<Information<InformationSet>> isel = info.select(INTERVENTIONS, InformationSet.class);
        isel.forEach((item) -> {
            InterventionVariable cur = new InterventionVariable();
            if (cur.read(item.value)) {
                interventions_.add(cur);
            }
        });
        InformationSet ifcoeff = info.getSubSet(FCOEFF);
        if (ifcoeff != null) {
            TradingDaysSpec td = calendar_.getTradingDays();
            List<Information<double[]>> all = ifcoeff.select(double[].class);

            all.forEach((item) -> {
                //Version 2.2.0 fixed regressors for user defined calendar
                if (td != null && td.getUserVariables() != null && "td".equals(item.name) && item.value.length == td.getUserVariables().length) {
                    for (int j = 0; j < item.value.length; j++) {
                        fcoeff.put(ITsVariable.validName("td|" + td.getUserVariables()[j]), new double[]{item.value[j]});
                    }
                } else {
                    fcoeff.put(item.name, item.value);
                }

            });

            List<Information<Double>> sall = ifcoeff.select(Double.class);
            sall.forEach((item) -> {
                //Version 2.2.0 fixed regressors for user defined calendar
                if (td != null && td.getUserVariables() != null && "td".equals(item.name) && 1 == td.getUserVariables().length) {
                    fcoeff.put(ITsVariable.validName("td|" + td.getUserVariables()[0]), new double[]{item.value});
                } else {
                    fcoeff.put(item.name, new double[]{item.value});
                }
            });

        }
        InformationSet icoeff = info.getSubSet(COEFF);
        if (icoeff != null) {
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
            COEFF = "coefficients", FCOEFF = "fixedcoefficients";
    private static final String[] DICTIONARY = new String[]{
        CALENDAR, OUTLIERS, USERS, RAMPS, INTERVENTIONS, COEFF, FCOEFF
    };

    private boolean compare(Map<String, double[]> cl, Map<String, double[]> cr) {
        if (cl.size() != cr.size()) {
            return false;
        }
        Optional<Map.Entry<String, double[]>> any = cl.entrySet().stream().filter(entry -> {
            if (!cr.containsKey(entry.getKey())) {
                return true;
            } else {
                return !Arrays.equals(entry.getValue(), cr.get(entry.getKey()));
            }
        }).findAny();

        return !any.isPresent();
    }

    public boolean hasFixedCoefficients() {
        return !this.fcoeff.isEmpty();
    }

    public boolean hasFixedCoefficients(String shortname) {
        Optional<String> any = fcoeff.keySet().stream().filter(x -> shortname.equals(ITsVariable.shortName(x))).findAny();
        return any.isPresent();
    }

    private void checkFixedCoefficients() {
        String[] names = getRegressionVariableShortNames(TsFrequency.Undefined);
        Arrays.sort(names);
        // Fixed coefficients that are not used anymore should be removed.
        // Be careful with user-defined calendar variables: td is useless if all the variables are in the list of fixed coefficients
        List<String> toremove = fcoeff.keySet().stream().filter(s -> Arrays.binarySearch(names, s) < 0).collect(Collectors.toList());
        if (toremove.contains("td")) {
            for (String name : names) {
                if (name.startsWith("td|") && ! fcoeff.keySet().contains(name)) {
                    toremove.remove("td");
                    break;
                }
            }
        }
        toremove.forEach(s -> fcoeff.remove(s));

//        List<String> toremove = fcoeff.keySet().stream().filter(s -> Arrays.binarySearch(names, s) < 0).collect(Collectors.toList());
//        // if the number of user-defined calendar variables names is not equal to the number of coefficents fcoeff for "td" td is needed
//        int i = 0;
//        for (String name : names) {
//            if (name.startsWith("td|")) {
//                ++i;
//            }
//        }
//
//        if (toremove.contains("td") && fcoeff.get("td").length != i) {
//            toremove.remove("td");
//        }
//        toremove.forEach(s -> fcoeff.remove(s));
    }

}
