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
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoProcessor;
import ec.tstoolkit.sarima.SarimaComponent;
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
        } catch (Exception ex) {
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
        if (rslts == null) {
            return false;
        }
        PreprocessingModel mdl = GenericSaResults.getPreprocessingModel(rslts);
        if (mdl != null) {
            Total++;
            try {
                addArima(mdl.description.getArimaComponent());
                addTransform(mdl.isMultiplicative());
                addCalendar(mdl.description);
                addOutliers(mdl.description);
                addStats(mdl.description.getArimaComponent().getFreeParametersCount(), mdl.getFullResiduals());
                return true;
            } catch (Exception err) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void addArima(SarimaComponent arima) {
        SarimaSpecification spec = arima.getSpecification();
        Integer count = arima_.get(spec);
        if (count == null) {
            arima_.put(spec, 1);
        } else {
            arima_.put(spec, count + 1);
        }
        if (arima.isMean()) {
            ++MeanCount;
        }
    }

    private void addStats(int np, TsData residuals) {
        if (residuals == null) {
            return;
        }
        try {
            LjungBoxTest lb = new LjungBoxTest();
            lb.setHyperParametersCount(np);
            lb.setK(TramoProcessor.calcLBLength(freq_));
            lb.test(residuals);
            if (lb.isValid()) {
                this.lb_.add(lb.getValue());
            }
        } catch (Exception err) {
        }
    }

    private void addCalendar(ModelDescription desc) {
        int ntd = desc.countRegressors(var -> var.isCalendar() && var.status.isSelected());
        int nftd = desc.countFixedRegressors(var -> var.isCalendar());

        int nlp = desc.countRegressors(var -> var.getVariable() instanceof ILengthOfPeriodVariable && var.status.isSelected());
        int nflp = desc.countFixedRegressors(var -> var.getVariable() instanceof ILengthOfPeriodVariable);

        ntd -= nftd;
        nftd -= nflp;

        int nee = desc.countRegressors(var -> var.isMovingHoliday() && var.status.isSelected());
        int nfee = desc.countFixedRegressors(var -> var.isMovingHoliday());
        if (ntd > 0 || nftd > 0) {
            ++TdCount;
        }
        if (desc.getLengthOfPeriodType() != LengthOfPeriodType.None || nlp > 0 || nflp > 0) {
            ++LpCount;
        }
        if (nee > 0 || nfee > 0) {
            ++EasterCount;
        }

    }

    private void addOutliers(ModelDescription desc) {
        AoCount += countOutlier(desc, OutlierType.AO.name());
        LsCount += countOutlier(desc, OutlierType.LS.name());
        TcCount += countOutlier(desc, OutlierType.TC.name());
        SoCount += countOutlier(desc, OutlierType.SO.name());
    }
    
    private int countOutlier(ModelDescription desc, String code){
        int n=desc.countRegressors(var->var.isOutlier() && ((IOutlierVariable)var.getVariable()).getCode().equals(code));
        int nf=desc.countFixedRegressors(var->var.isOutlier() && ((IOutlierVariable)var.getVariable()).getCode().equals(code));
        return n+nf;
    }

    private void addTransform(boolean multiplicative) {
        if (multiplicative) {
            ++LogCount;
        }

    }

}
