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
package jdplus.regsarima.regular;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleList;
import demetra.modelling.implementations.SarimaSpec;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.Variable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jdplus.stats.tests.LjungBox;

/**
 *
 * @author Kristof Bayens
 */
public class RegSarimaReport {

    /**
     * @return the Total
     */
    public int getTotal() {
        return Total;
    }

    /**
     * @return the NUndecompsable
     */
    public int getNUndecompsable() {
        return NUndecompsable;
    }

    /**
     * @return the TdCount
     */
    public int getTdCount() {
        return TdCount;
    }

    /**
     * @return the LogCount
     */
    public int getLogCount() {
        return LogCount;
    }

    /**
     * @return the LpCount
     */
    public int getLpCount() {
        return LpCount;
    }

    /**
     * @return the EasterCount
     */
    public int getEasterCount() {
        return EasterCount;
    }

    /**
     * @return the AoCount
     */
    public int getAoCount() {
        return AoCount;
    }

    /**
     * @return the LsCount
     */
    public int getLsCount() {
        return LsCount;
    }

    /**
     * @return the TcCount
     */
    public int getTcCount() {
        return TcCount;
    }

    /**
     * @return the SoCount
     */
    public int getSoCount() {
        return SoCount;
    }

    /**
     * @return the MeanCount
     */
    public int getMeanCount() {
        return MeanCount;
    }

    /**
     * @return the T0
     */
    public long getT0() {
        return T0;
    }

    /**
     * @return the T1
     */
    public long getT1() {
        return T1;
    }

    private final int period;
    private final DoubleList ljungBox = new DoubleList();
    private final Map<SarimaOrders, Integer> arima = new HashMap<>();

    private int Total;
    private int NUndecompsable;
    private int TdCount;
    private int LogCount;
    private int LpCount;
    private int EasterCount;
    private int AoCount;
    private int LsCount;
    private int TcCount;
    private int SoCount;
    private int MeanCount;
    private long T0;
    private long T1;

    public RegSarimaReport(int freq) {
        period = freq;
    }

    public int getFrequency() {
        return period;
    }

    public SarimaOrders[] getModels() {
        SarimaOrders[] m = arima.keySet().stream().toArray(SarimaOrders[]::new);
        //Arrays.sort(m, null);
        return m;
    }

    public int getModelCount(SarimaOrders spec) {
        int n = 0;
        try {
            n = arima.get(spec);
        } catch (Exception ex) {
        }
        return n;
    }

    //ISaReport Members
    public void start() {
        clear();
        T0 = System.currentTimeMillis();
    }

    protected void clear() {
        ljungBox.clear();

        Total = 0;
        TdCount = 0;
        LpCount = 0;
        EasterCount = 0;
        LogCount = 0;
        AoCount = 0;
        LsCount = 0;
        TcCount = 0;
        SoCount = 0;
        MeanCount = 0;
    }

    public void end() {
        T1 = System.currentTimeMillis();
    }

    public boolean add(RegSarimaModel mdl) {
        if (mdl != null) {
            Total++;
            try {
                Variable[] variables = mdl.getDescription().getVariables();
                addArima(mdl.getDescription().getStochasticComponent());
                addTransform(mdl.getDescription().isLogTransformation());
                addCalendar(variables);
                addOutliers(variables);
                addStats(mdl);
                return true;
            } catch (Exception err) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void addArima(SarimaSpec arima) {
        SarimaOrders spec = arima.orders();
        Integer count = this.arima.get(spec);
        if (count == null) {
            this.arima.put(spec, 1);
        } else {
            this.arima.put(spec, count + 1);
        }
    }

    private void addStats(RegSarimaModel mdl) {
        TsData res = mdl.fullResiduals();
        if (res == null) {
            return;
        }
        try {
            StatisticalTest lb = new LjungBox(res.getValues())
                    .hyperParametersCount(mdl.freeArimaParametersCount())
                    .autoCorrelationsCount(LjungBox.defaultAutoCorrelationsCount(period))
                    .build();
            this.ljungBox.add(lb.getValue());
        } catch (Exception err) {
        }
    }

    private void addCalendar(Variable[] vars) {
        int ntd = Arrays.stream(vars).filter(var -> var.getCore() instanceof ITradingDaysVariable).mapToInt(var -> var.dim()).sum();
        int nlp = (int) Arrays.stream(vars).filter(var -> var.getCore() instanceof ILengthOfPeriodVariable).count();
        int nee = (int) Arrays.stream(vars).filter(var -> var.getCore() instanceof IEasterVariable).count();
        if (ntd > 0) {
            ++TdCount;
        }
        if (nlp > 0) {
            ++LpCount;
        }
        if (nee > 0) {
            ++EasterCount;
        }

    }

    private void addOutliers(Variable[] vars) {
        AoCount += Arrays.stream(vars).filter(var -> var.getCore() instanceof AdditiveOutlier).count();
        LsCount += Arrays.stream(vars).filter(var -> var.getCore() instanceof LevelShift).count();
        TcCount += Arrays.stream(vars).filter(var -> var.getCore() instanceof TransitoryChange).count();
        SoCount += Arrays.stream(vars).filter(var -> var.getCore() instanceof PeriodicOutlier).count();
    }

    private void addTransform(boolean multiplicative) {
        if (multiplicative) {
            ++LogCount;
        }

    }

}
