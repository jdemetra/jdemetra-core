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


package ec.tss.sa;

import ec.satoolkit.GenericSaResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoProcessor;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.stats.LjungBoxTest;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.DoubleList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Kristof Bayens
 */
public class RegArimaReport implements ISaReport {
    private final int freq_;
    private final DoubleList lb_ = new DoubleList();
    private final Map<SarimaSpecification, Integer> arima_ = new HashMap<>();

    public int Total;
    public int NUndecompsable;
    public int TdCount;
    public int LogCount;
    public int LpCount, EasterCount;
    public int AoCount, LsCount, TcCount, SoCount;
    public int MeanCount;
    public long T0, T1;

    public RegArimaReport(int freq) {
        freq_ = freq;
    }

    public int getFrequency() {
        return freq_;
    }

    public SarimaSpecification[] getModels() {
        SarimaSpecification[] m = arima_.keySet().stream().toArray(SarimaSpecification[]::new);
        //Arrays.sort(m, null);
        return m;
    }

    public int getModelCount(SarimaSpecification spec) {
        int n = 0;
        try {
            n = arima_.get(spec);
        }
        catch(Exception ex) {
        }
        return n;
    }

    //ISaReport Members
    @Override
    public void start() {
        clear();
        T0 = System.currentTimeMillis();
    }

    protected void clear() {
        lb_.clear();

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

    @Override
    public void end() {
        T1 = System.currentTimeMillis();
    }

    @Override
    public boolean add(CompositeResults rslts) {
        if (rslts == null)
            return false;
        PreprocessingModel mdl=GenericSaResults.getPreprocessingModel(rslts);
        if (mdl != null) {
             Total++;
            try{
            addArima(mdl.estimation.getRegArima());
            TsVariableList vars = mdl.description.buildRegressionVariables();
            addTransform(mdl.isMultiplicative());
            addCalendar(vars, mdl.description.getLengthOfPeriodType());
            addOutliers(vars);
            addStats(mdl.description.getArimaComponent().getFreeParametersCount(), mdl.getFullResiduals());
            return true;
            }
            catch (Exception err){
                return false;
            }
        }
        else
            return false;
    }

    private void addArima(RegArimaModel<SarimaModel> stmodel) {
        SarimaSpecification spec = stmodel.getArima().getSpecification();
        Integer count = arima_.get(spec);
        if (count == null)
            arima_.put(spec, 1);
        else
            arima_.put(spec, count + 1);
        if (stmodel.isMeanCorrection())
            ++MeanCount;
    }
    
        private void addStats(int np, TsData residuals)
        {
            if (residuals == null)
                return;
                try
                {
                    LjungBoxTest lb = new LjungBoxTest();
                    lb.setHyperParametersCount(np);
                    lb.setK(TramoProcessor.calcLBLength(freq_));
                    lb.test(residuals);
                    if (lb.isValid()){
                            this.lb_.add(lb.getValue());
                    }
                }
                catch (Exception err){ }
            }
        

        private void addCalendar(TsVariableList vars, LengthOfPeriodType adjust)
        {
            if (vars.select(ICalendarVariable.class).getItemsCount()> 0)
                ++TdCount;
            if (adjust != LengthOfPeriodType.None || vars.select(ILengthOfPeriodVariable.class).getItemsCount()> 0)
                    ++LpCount;
           if (vars.select(IEasterVariable.class).getItemsCount()> 0)
                ++EasterCount;
            
        }

     private void addOutliers(TsVariableList vars) {
        AoCount+=vars.select(OutlierType.AO, false).getItemsCount();
        LsCount+=vars.select(OutlierType.LS, false).getItemsCount();
        TcCount+=vars.select(OutlierType.TC, false).getItemsCount();
        SoCount+=vars.select(OutlierType.SO, false).getItemsCount();
    }

    private void addTransform(boolean multiplicative) {
        if (multiplicative)
        ++LogCount;

    }


}
