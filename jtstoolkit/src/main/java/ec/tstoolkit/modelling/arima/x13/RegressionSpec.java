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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.regression.*;
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

    public static final double DEF_AICCDIFF = 0;

    public static final String AICDIFF = "aicdiff",
            TD = "tradingdays",
            MH = "mh", MHS = "mh*",
            OUTLIER = "outlier", OUTLIERS = "outliers",
            RAMP = "ramp", RAMPS = "ramps",
            USER = "user", USERS = "user*",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, AICDIFF), Double.class);
        dic.put(InformationSet.item(prefix, OUTLIERS), String[].class);
        dic.put(InformationSet.item(prefix, RAMPS), String[].class);
        TsVariableDescriptor.fillDictionary(InformationSet.item(prefix, USERS), dic);
        MovingHolidaySpec.fillDictionary(InformationSet.item(prefix, MHS), dic);
        TradingDaysSpec.fillDictionary(InformationSet.item(prefix, TD), dic);
        InterventionVariable.fillDictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
    }

    private double aicdiff_ = DEF_AICCDIFF;
//        private SinCosVariablesSpec m_sincos;
//        private SeasonalVariablesSpec m_seasonal;
//        private ConstVariableSpec m_const;
    private TradingDaysSpec td_ = new TradingDaysSpec();
    private ArrayList<MovingHolidaySpec> mh_ = new ArrayList<>();
    private ArrayList<OutlierDefinition> outliers_ = new ArrayList<>();
    private ArrayList<TsVariableDescriptor> users_ = new ArrayList<>();
    private ArrayList<InterventionVariable> interventions_ = new ArrayList<>();
    private ArrayList<Ramp> ramps_ = new ArrayList<>();

    public RegressionSpec() {
    }

    public void reset() {
        mh_.clear();
        outliers_.clear();
        users_.clear();
        interventions_.clear();
        ramps_.clear();
        aicdiff_ = DEF_AICCDIFF;
    }

    public boolean isUsed() {
        return td_.isUsed() || !mh_.isEmpty()
                || !outliers_.isEmpty() || !users_.isEmpty()
                || !ramps_.isEmpty() || !interventions_.isEmpty();
    }

    public double getAICCDiff() {
        return aicdiff_;
    }

    public void setAICCDiff(double value) {
        aicdiff_ = value;
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

    public MovingHolidaySpec getEaster() {
        for (MovingHolidaySpec mh : mh_) {
            if (mh.getType() == MovingHolidaySpec.Type.Easter) {
                return mh;
            }
        }
        return null;
    }

    public MovingHolidaySpec[] getMovingHolidays() {
        return Jdk6.Collections.toArray(mh_, MovingHolidaySpec.class);
    }

    public void setMovingHolidays(MovingHolidaySpec[] value) {
        mh_.clear();
        if (value != null) {
            Collections.addAll(mh_, value);
        }

    }

    public void clearMovingHolidays() {
        mh_.clear();
    }

    public void removeMovingHolidays(MovingHolidaySpec espec) {
        mh_.remove(espec);
    }

    public MovingHolidaySpec search(MovingHolidaySpec.Type type) {
        for (MovingHolidaySpec mh : mh_) {
            if (mh.getType() == type) {
                return mh;
            }
        }
        return null;
    }

    public void add(MovingHolidaySpec spec) {
        mh_.add(spec);
    }

    public int getOutliersCount() {
        return outliers_.size();
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

    public void clearOutliers() {
        outliers_.clear();
    }

    public OutlierDefinition[] search(OutlierType type) {
        ArrayList<OutlierDefinition> desc = new ArrayList<>();
        for (OutlierDefinition o : outliers_) {
            if (o.type == type) {
                desc.add(o);
            }
        }
        return Jdk6.Collections.toArray(desc, OutlierDefinition.class);
    }

    public void add(OutlierDefinition o) {
        outliers_.add(o);
    }

    public void add(IOutlierVariable item) {
        outliers_.add(new OutlierDefinition(item.getPosition(), item.getOutlierType(), item.isPrespecified()));
    }

    public boolean contains(OutlierDefinition outlier) {
        for (OutlierDefinition def : outliers_) {
            if (def.equals(outlier)) {
                return true;
            }
        }
        return false;
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

    public int getUserDefinedVariablesCount() {
        return users_.size();
    }

    public void clearUserDefinedVariables() {
        users_.clear();
    }

    public void add(TsVariableDescriptor spec) {
        users_.add(spec);
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

    public void add(InterventionVariable ivar) {
        interventions_.add(ivar);
    }

    public void clearInterventionVariables() {
        interventions_.clear();
    }

    public int getInterventionVariablesCount() {
        return interventions_.size();
    }

    public void clearRamps() {
        ramps_.clear();
    }

    public int getRampsCount() {
        return ramps_.size();
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
            spec.mh_ = new ArrayList<>();
            for (MovingHolidaySpec mh : mh_) {
                spec.mh_.add(mh.clone());
            }
            spec.outliers_ = Jdk6.newArrayList(outliers_);
            spec.ramps_ = new ArrayList<>();
            for (Ramp r : ramps_) {
                spec.ramps_.add(r.clone());
            }
            spec.td_ = td_.clone();
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
        return aicdiff_ == other.aicdiff_
                && Objects.equals(td_, other.td_)
                && Comparator.equals(users_, other.users_)
                && Comparator.equals(ramps_, other.ramps_)
                && Comparator.equals(outliers_, other.outliers_)
                && Comparator.equals(mh_, other.mh_)
                && Comparator.equals(interventions_, other.interventions_);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Jdk6.Double.hashCode(this.aicdiff_);
        hash = 89 * hash + td_.hashCode();
        hash = 89 * hash + mh_.hashCode();
        hash = 89 * hash + outliers_.hashCode();
        hash = 89 * hash + users_.hashCode();
        hash = 89 * hash + interventions_.hashCode();
        hash = 89 * hash + ramps_.hashCode();
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!isUsed()) {
            return null;
        }
        InformationSet specInfo = new InformationSet();
        if (verbose || aicdiff_ != DEF_AICCDIFF) {
            specInfo.add(AICDIFF, aicdiff_);
        }
        if (verbose || td_.isUsed()) {
            InformationSet tdinfo = td_.write(verbose);
            if (tdinfo != null) {
                specInfo.add(TD, tdinfo);
            }
        }
        int idx = 1;
        for (MovingHolidaySpec spec : mh_) {
            InformationSet cmh = spec.write(verbose);
            if (cmh != null) {
                specInfo.add(MH + Integer.toString(idx++), cmh);
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

        idx = 1;
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
        try {
            reset();
            Double aic = info.get(AICDIFF, Double.class);
            if (aic != null) {
                aicdiff_ = aic;
            }
            InformationSet tdinfo = info.getSubSet(TD);
            if (tdinfo != null) {
                td_ = new TradingDaysSpec();
                if (!td_.read(tdinfo)) {
                    return false;
                }
            }
            List<Information<InformationSet>> sel = info.select(MHS, InformationSet.class);
            for (Information<InformationSet> item : sel) {
                MovingHolidaySpec cmh = new MovingHolidaySpec();
                if (cmh.read(item.value)) {
                    mh_.add(cmh);
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
        } catch (Exception err) {
            return false;
        }
    }

//    @Override
//    public void fillDictionary(String prefix, List<String> dic) {
//        dic.add(InformationSet.item(prefix, AICDIFF));
//        TradingDaysSpec.dictionary(InformationSet.item(prefix, TD), dic);
//        MovingHolidaySpec.dictionary(InformationSet.item(prefix, MHS), dic);
//        dic.add(InformationSet.item(prefix, OUTLIERS));
//        dic.add(InformationSet.item(prefix, RAMPS));
//        TsVariableDescriptor.dictionary(InformationSet.item(prefix, USERS), dic);
//        InterventionVariable.dictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
//    }
}
