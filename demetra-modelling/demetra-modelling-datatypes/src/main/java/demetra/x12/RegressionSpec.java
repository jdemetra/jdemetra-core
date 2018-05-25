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
package demetra.x12;

import demetra.modelling.regression.InterventionVariableDefinition;
import demetra.modelling.regression.OutlierDefinition;
import demetra.modelling.regression.RampDefinition;
import demetra.modelling.regression.TsVariableDefinition;
import java.util.*;
import demetra.utilities.Comparator;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
public class RegressionSpec {

    public static final double DEF_AICCDIFF = 0;

    private double aicdiff = DEF_AICCDIFF;
//        private SinCosVariablesSpec m_sincos;
//        private SeasonalVariablesSpec m_seasonal;
//        private ConstVariableSpec m_const;
    private TradingDaysSpec td = new TradingDaysSpec();
    private final ArrayList<MovingHolidaySpec> mh = new ArrayList<>();
    private final ArrayList<OutlierDefinition> outliers = new ArrayList<>();
    private final ArrayList<TsVariableDefinition> users = new ArrayList<>();
    private final ArrayList<InterventionVariableDefinition> interventions = new ArrayList<>();
    private final ArrayList<RampDefinition> ramps = new ArrayList<>();
    private final Map<String, double[]> fcoeff = new LinkedHashMap<>();
    private final Map<String, double[]> coeff = new LinkedHashMap<>();

    public RegressionSpec() {
    }

    public RegressionSpec(RegressionSpec other) {
        this.aicdiff = other.aicdiff;
        this.td = new TradingDaysSpec(other.td);
        mh.addAll(other.mh);
        outliers.addAll(other.outliers);
        ramps.addAll(other.ramps);
        interventions.addAll(other.interventions);
        users.addAll(other.users);
        other.coeff.forEach(coeff::put);
        other.fcoeff.forEach(fcoeff::put);
    }

    public void reset() {
        mh.clear();
        outliers.clear();
        users.clear();
        interventions.clear();
        ramps.clear();
        fcoeff.clear();
        coeff.clear();
        aicdiff = DEF_AICCDIFF;
    }

    public boolean isUsed() {
        return td.isUsed() || !mh.isEmpty()
                || !outliers.isEmpty() || !users.isEmpty()
                || !ramps.isEmpty() || !interventions.isEmpty();
    }

    public double getAICCDiff() {
        return aicdiff;
    }

    public void setAICCDiff(double value) {
        aicdiff = value;
    }

    public TradingDaysSpec getTradingDays() {
        return td;
    }

    public void setTradingDays(@Nonnull TradingDaysSpec value) {
        td = value;
    }

    public MovingHolidaySpec getEaster() {
        for (MovingHolidaySpec mh : mh) {
            if (mh.getType() == MovingHolidaySpec.Type.Easter || mh.getType() == MovingHolidaySpec.Type.JulianEaster) {
                return mh;
            }
        }
        return null;
    }

    public MovingHolidaySpec[] getMovingHolidays() {
        return mh.toArray(new MovingHolidaySpec[mh.size()]);
    }

    public void setMovingHolidays(@Nonnull MovingHolidaySpec[] value) {
        mh.clear();
        Collections.addAll(mh, value);
    }

    public void clearMovingHolidays() {
        mh.clear();
    }

    public void removeMovingHolidays(MovingHolidaySpec espec) {
        mh.remove(espec);
    }

    public MovingHolidaySpec search(MovingHolidaySpec.Type type) {
        for (MovingHolidaySpec mh : mh) {
            if (mh.getType() == type) {
                return mh;
            }
        }
        return null;
    }

    public void add(MovingHolidaySpec spec) {
        mh.add(spec);
    }

    public int getOutliersCount() {
        return outliers.size();
    }

    public OutlierDefinition[] getOutliers() {
        return outliers.toArray(new OutlierDefinition[outliers.size()]);
    }

    public void setOutliers(@Nonnull OutlierDefinition[] value) {
        outliers.clear();
        Collections.addAll(outliers, value);
    }

    public void clearOutliers() {
        outliers.clear();
    }

    public OutlierDefinition[] search(String type) {
        ArrayList<OutlierDefinition> desc = new ArrayList<>();
        for (OutlierDefinition o : outliers) {
            if (o.getCode().equals(type)) {
                desc.add(o);
            }
        }
        return desc.toArray(new OutlierDefinition[desc.size()]);
    }

    public void add(OutlierDefinition o) {
        outliers.add(o);
    }

    public boolean contains(OutlierDefinition outlier) {
        for (OutlierDefinition def : outliers) {
            if (def.equals(outlier)) {
                return true;
            }
        }
        return false;
    }

    public TsVariableDefinition[] getUserDefinedVariables() {
        return users.toArray(new TsVariableDefinition[users.size()]);
    }

    public void setUserDefinedVariables(@Nonnull TsVariableDefinition[] value) {
        users.clear();
        Collections.addAll(users, value);
    }

    public int getUserDefinedVariablesCount() {
        return users.size();
    }

    public void clearUserDefinedVariables() {
        users.clear();
    }

    public void add(TsVariableDefinition spec) {
        users.add(spec);
    }

    public InterventionVariableDefinition[] getInterventionVariables() {
        return interventions.toArray(new InterventionVariableDefinition[interventions.size()]);
    }

    public void setInterventionVariables(@Nonnull InterventionVariableDefinition[] value) {
        interventions.clear();
            Collections.addAll(interventions, value);
    }

    public void add(InterventionVariableDefinition ivar) {
        interventions.add(ivar);
    }

    public void clearInterventionVariables() {
        interventions.clear();
    }

    public int getInterventionVariablesCount() {
        return interventions.size();
    }

    public Map<String, double[]> getAllFixedCoefficients() {
//        checkFixedCoefficients();
        return Collections.unmodifiableMap(fcoeff);
    }

    public Map<String, double[]> getAllCoefficients() {
        return Collections.unmodifiableMap(coeff);
    }

    public void clearRamps() {
        ramps.clear();
    }

    public int getRampsCount() {
        return ramps.size();
    }

    public void add(RampDefinition rp) {
        ramps.add(rp);
    }

    public RampDefinition[] getRamps() {
        return ramps.toArray(new RampDefinition[ramps.size()]);
    }

    public void setRamps(RampDefinition[] value) {
        ramps.clear();
        if (value != null) {
            Collections.addAll(ramps, value);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof RegressionSpec && equals((RegressionSpec) obj));
    }

    private boolean equals(RegressionSpec other) {
        return aicdiff == other.aicdiff
                && Objects.equals(td, other.td)
                && Comparator.equals(users, other.users)
                && Comparator.equals(ramps, other.ramps)
                && Comparator.equals(outliers, other.outliers)
                && Comparator.equals(mh, other.mh)
                && Comparator.equals(interventions, other.interventions)
                && compare(fcoeff, other.fcoeff);

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Double.hashCode(this.aicdiff);
        hash = 89 * hash + td.hashCode();
        hash = 89 * hash + mh.hashCode();
        hash = 89 * hash + outliers.hashCode();
        hash = 89 * hash + users.hashCode();
        hash = 89 * hash + interventions.hashCode();
        hash = 89 * hash + ramps.hashCode();
        return hash;
    }

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

}
