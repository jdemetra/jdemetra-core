/*
 * Copyright 2013-2014 National Bank of Belgium
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

package ec.tstoolkit.arima.special;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.AbstractTsVariableBox;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutliersFactory;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.regression.StockTradingDaysVariables;
import ec.tstoolkit.timeseries.regression.TsVariableGroup;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class RegressionSpec implements Cloneable, InformationSetSerializable {

    final static OutliersFactory fac = new OutliersFactory(true);

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, OUTLIERS), String[].class);
        dic.put(InformationSet.item(prefix, RAMPS), String[].class);
        TradingDaysSpec.fillDictionary(InformationSet.item(prefix, TD), dic);
        EasterSpec.fillDictionary(InformationSet.item(prefix, EASTER), dic);
        InterventionVariable.fillDictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
        TsVariableDescriptor.fillDictionary(InformationSet.item(prefix, USERS), dic);
    }
    private TradingDaysSpec td_ = new TradingDaysSpec();
    private EasterSpec easter_ = new EasterSpec();
    private ArrayList<OutlierDefinition> outliers_ = new ArrayList<>();
    private ArrayList<Ramp> ramps_ = new ArrayList<>();
    private ArrayList<InterventionVariable> interventions_ = new ArrayList<>();
    private ArrayList<TsVariableDescriptor> users_ = new ArrayList<>();

    public void reset() {
        outliers_.clear();
        ramps_.clear();
        interventions_.clear();
        users_.clear();
    }

    public boolean isUsed() {
        return td_.isUsed() || easter_.isUsed() || !outliers_.isEmpty()
                || !ramps_.isEmpty() || !interventions_.isEmpty()
                || !users_.isEmpty();
    }

    public TradingDaysSpec getTradingDays() {
        return td_;
    }

    public void setTradingDays(TradingDaysSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(TD);
        }
        td_ = value;
    }

    public EasterSpec getEaster() {
        return easter_;
    }

    public void setEaster(EasterSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(EASTER);
        }
        easter_ = value;
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
        return !td_.isUsed() && !easter_.isUsed()&& users_.isEmpty()
                && outliers_.isEmpty() && interventions_.isEmpty()
                && ramps_.isEmpty();
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
            spec.td_ = td_.clone();
            spec.easter_=easter_.clone();
            spec.users_ = new ArrayList<>();
            for (TsVariableDescriptor var : users_) {
                spec.users_.add(var.clone());
            }
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
                && Objects.equals(td_, other.td_)
                && Objects.equals(easter_, other.easter_);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + td_.hashCode();
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!isUsed()) {
            return null;
        }
        InformationSet specInfo = new InformationSet();
        if (verbose || td_.isUsed()) {
            InformationSet cinfo = td_.write(verbose);
            if (cinfo != null) {
                specInfo.add(TD, cinfo);
            }
        }
        if (verbose || easter_.isUsed()) {
            InformationSet cinfo = easter_.write(verbose);
            if (cinfo != null) {
                specInfo.add(EASTER, cinfo);
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
        return specInfo;
    }

    @Override
    public boolean read(InformationSet info) {
        reset();
        InformationSet cinfo = info.getSubSet(TD);
        if (cinfo != null) {
            boolean tok = td_.read(cinfo);
            if (!tok) {
                return false;
            }
        }
        cinfo = info.getSubSet(EASTER);
        if (cinfo != null) {
            boolean tok = easter_.read(cinfo);
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
        return true;
    }
    
    public void fill(final TsVariableList regs, final TsFrequency freq, final ProcessingContext context){
        initializeTradingDays(regs, context);
        initializeEaster(regs);
        initializeOutliers(regs, freq);
        initializeRamps(regs);
        initializeInterventions(regs);
        initializeUsers(regs, context);
    }

    private void initializeTradingDays(TsVariableList regs, ProcessingContext context) {
        if (!td_.isUsed()) {
            return;
        }
        if (td_.isStockTradingDays()) {
            initializeStockTradingDays(regs);
        }
        if (td_.getHolidays() != null) {
            initializeHolidays(regs, context);
        } else if (td_.getUserVariables() != null) {
            initializeUserHolidays(regs, context);
        } else if (td_.getTradingDaysType() != TradingDaysType.None) {
            initializeDefaultTradingDays(regs);
        }
    }

    private void initializeEaster(TsVariableList regs) {
        if (!easter_.isUsed()) {
            return;
        }
        EasterVariable var = new EasterVariable();
        var.setDuration(easter_.getDuration());
        var.setType(EasterVariable.Type.Tramo);
        var.includeEaster(easter_.getOption().containsEaster());
        var.includeEasterMonday(easter_.getOption().containsEasterMonday());
        regs.add(var);
    }

    private void initializeOutliers(TsVariableList regs, TsFrequency freq) {
        ArrayList<IOutlierVariable> var = new ArrayList<>();
        ArrayList<IOutlierVariable> pvar = new ArrayList<>();
        for (OutlierDefinition outlier : outliers_) {
            IOutlierVariable v = fac.make(outlier);
            regs.add(v);
        }
    }

    private void initializeUsers(TsVariableList regs, ProcessingContext context) {
         for (TsVariableDescriptor desc : users_) {
            ITsVariable var = desc.toTsVariable(context);
            if (var != null)
                regs.add(var);
        }
    }

    private void initializeInterventions(TsVariableList regs) {
         for (InterventionVariable var : interventions_) {
            regs.add(var);
        }
    }

    private void initializeRamps(TsVariableList regs) {
        if (ramps_ == null) {
            return;
        }
        for (Ramp ramp : ramps_) {
            regs.add(ramp);
        }
    }

    private void initializeHolidays(TsVariableList regs, ProcessingContext context) {
        IGregorianCalendarProvider cal = context.getGregorianCalendars().get(td_.getHolidays());
        if (cal == null) {
            return;
        }
        TradingDaysType tdType = td_.getTradingDaysType();
        ITsVariable var = new GregorianCalendarVariables(cal, tdType);
        regs.add(var);
        if (td_.isLeapYear()) {
            LeapYearVariable lp = new LeapYearVariable(LengthOfPeriodType.LeapYear);
            regs.add(lp);
        }
    }

    private void initializeUserHolidays(TsVariableList regs, ProcessingContext context) {
        String[] userVariables = td_.getUserVariables();
        if (userVariables == null || userVariables.length == 0) {
            return;
        }
        ITsVariable[] vars = new ITsVariable[userVariables.length];

        for (int i = 0; i < vars.length; ++i) {
            vars[i] = context.getTsVariable(userVariables[i]);
            if (vars[i] == null) {
                throw new TsException(userVariables[i] + " not found");
            }
        }

        TsVariableGroup var = new TsVariableGroup("User-defined calendar variables", vars);
        ITradingDaysVariable td = AbstractTsVariableBox.tradingDays(var);
        regs.add(td);
    }

    private void initializeDefaultTradingDays(TsVariableList regs) {
        TradingDaysType tdType = td_.getTradingDaysType();
        GregorianCalendarVariables var = GregorianCalendarVariables.getDefault(tdType);
        regs.add(var);

        if (td_.isLeapYear()) {
            LeapYearVariable lp = new LeapYearVariable(LengthOfPeriodType.LeapYear);
            regs.add(lp);
        }
    }

    private void initializeStockTradingDays(TsVariableList regs) {
        ITsVariable var = new StockTradingDaysVariables(td_.getStockTradingDays());
        regs.add(var);
    }
    
    public static final String TD = "td", EASTER="easter",
            OUTLIERS = "outliers",
            USER = "user", USERS = "user*", RAMPS = "ramps",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*";
    private static final String[] DICTIONARY = new String[]{
        TD, EASTER, OUTLIERS, USERS, RAMPS, INTERVENTIONS
    };
}
