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
package ec.satoolkit.diagnostics;

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.tramo.ArmaModule;
import ec.tstoolkit.modelling.arima.tramo.DifferencingModule;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class FTest {

    private RegArimaModel<SarimaModel> regmodel_;
    private RegArimaEstimation<SarimaModel> seasonalModel_;
    private StatisticalTest f_;
    private double sensitivity_ = .01;
    private int nseas_;

    public FTest() {
    }

    public boolean test(TsData s) {
        clear();
        prepareSeasonalModel(s);
        addSeasonalDummies(s.getDomain());
        if (!estimateModel()) {
            return false;
        }
        computeStatistics();
        return true;
    }

    public boolean test(ModelDescription m) {
        clear();
        prepareSeasonalModel(m.clone());
        addSeasonalDummies(m.getEstimationDomain());
        if (!estimateModel()) {
            return false;
        }
        computeStatistics();
        return true;
    }

    public boolean testAMI(TsData s) {
        clear();
        return searchSeasonalModel(s);
    }

    /**
     * @return the sensibility_
     */
    public double getSensitivity() {
        return sensitivity_;
    }

    /**
     * @param sensitivity the sensitivity to set
     */
    public void setSensitivity(double sensitivity) {
        this.sensitivity_ = sensitivity;
    }

    public RegArimaEstimation<SarimaModel> getEstimatedModel() {
        return seasonalModel_;
    }

    public StatisticalTest getFTest() {
        return f_;
    }

    private void prepareSeasonalModel(TsData input) {
        SarimaSpecification rspec = new SarimaSpecification(input.getFrequency().intValue());
        rspec.airline(false);
        SarimaModel arima = new SarimaModel(rspec);
        regmodel_ = new RegArimaModel<>(arima);
        regmodel_.setY(new DataBlock(input.internalStorage()));
        regmodel_.setMeanCorrection(true);
    }

    private void prepareSeasonalModel(ModelDescription m) {
        SarimaSpecification rspec = m.getSpecification();
        rspec.setBD(0);
        rspec.setBP(0);
        rspec.setBQ(0);
        m.setSpecification(rspec);
        regmodel_ = m.buildRegArima();
        regmodel_.setMeanCorrection(true);
    }

    private void addSeasonalDummies(TsDomain domain) {
        // makes seasonal dummies
        SeasonalDummies dummies = new SeasonalDummies(domain.getFrequency());
        List<DataBlock> regs = RegressionUtilities.data(dummies, domain);
        for (DataBlock reg : regs) {
            regmodel_.addX(reg);
        }
        nseas_ = dummies.getDim();
    }

    private boolean estimateModel() {
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        seasonalModel_ = monitor.process(regmodel_);
        return seasonalModel_ != null;
    }

    private boolean estimateContext(ModellingContext context) {
        ModelDescription model = context.description;
        // force mean correction when the model is stationary
        if (model.getArimaComponent().getDifferencingOrder() == 0) {
            model.setMean(true);
        }
        context.estimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        return context.estimation.compute(monitor, context.description.getArimaComponent().getFreeParametersCount());
    }

    private void computeStatistics() {
        JointRegressionTest test = new JointRegressionTest(sensitivity_);
        int nvars = regmodel_.getVarsCount();
        test.accept(seasonalModel_.likelihood, regmodel_.getArma().getParametersCount(), nvars - nseas_, nseas_, null);
        f_ = test.getTest();
    }

    private void clear() {
        regmodel_ = null;
        seasonalModel_ = null;
        f_ = null;
    }

    private boolean searchSeasonalModel(TsData s) {
        ModellingContext context = new ModellingContext();
        context.description = new ModelDescription(s, null);
        context.description.setAirline(false);
        context.hasseas = false;
        SeasonalDummies dummies = new SeasonalDummies(s.getFrequency());
        nseas_ = dummies.getDim();
        context.description.getUserVariables().add(new Variable(dummies, ComponentType.Seasonal));
        if (!estimateContext(context)) {
            return false;
        }
        context.description.setSpecification(
                new SarimaSpecification(context.description.getFrequency()));

        DifferencingModule diff = new DifferencingModule();
        diff.process(context);
        context.estimation = null;
        ArmaModule arma = new ArmaModule();
        arma.setAcceptingWhiteNoise(true);
        arma.process(context);
        if (!estimateContext(context)) {
            return false;
        }
        regmodel_ = context.estimation.getRegArima();
        seasonalModel_ = new RegArimaEstimation<>(regmodel_, context.estimation.getLikelihood());
        computeStatistics();
        return true;
    }
}
