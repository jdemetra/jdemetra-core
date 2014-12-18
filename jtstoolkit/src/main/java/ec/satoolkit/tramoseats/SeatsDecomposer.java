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

package ec.satoolkit.tramoseats;

import ec.tstoolkit.modelling.ComponentInformation;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.IDefaultSeriesDecomposer;
import ec.satoolkit.IPreprocessingFilter;
import ec.satoolkit.seats.SeatsKernel;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.seats.SeatsSpecification;
import ec.satoolkit.seats.SeatsToolkit;
import ec.tstoolkit.arima.estimation.FastArimaML;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoProcessor;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.stats.LjungBoxTest;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author pcuser
 */
public class SeatsDecomposer implements IDefaultSeriesDecomposer<SeatsResults> {

    private SeatsSpecification spec_;
    private SeatsResults results_;

    public SeatsDecomposer(SeatsSpecification spec) {
        spec_ = spec;
    }

    @Override
    public boolean decompose(TsData y) {
        SeatsToolkit toolkit = SeatsToolkit.create(spec_);
        SeatsKernel kernel = new SeatsKernel();
        kernel.setToolkit(toolkit);
        results_ = kernel.process(y);
        if (results_ == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean decompose(PreprocessingModel model, IPreprocessingFilter filter) {
        SeatsSpecification spec = prepareSpec(model, filter);
        SeatsToolkit toolkit = SeatsToolkit.create(spec);
        SeatsKernel kernel = new SeatsKernel();
        kernel.setToolkit(toolkit);
        TsData y = filter.getCorrectedSeries(true);
        correctBias(y, filter);
        results_ = kernel.process(y);
        if (results_ == null) {
            return false;
        }
        correctBias(results_.getSeriesDecomposition(), filter);
        LikelihoodStatistics stat = model.estimation.getStatistics();
        results_.getModel().setSer(Math.sqrt(stat.SsqErr/(stat.effectiveObservationsCount-stat.estimatedParametersCount)));
        return true;
    }

    @Override
    public SeatsResults getDecomposition() {
        return results_;
    }

    private SeatsSpecification prepareSpec(PreprocessingModel model, IPreprocessingFilter filter) {

        SeatsSpecification spec = spec_.clone();
        spec.setLog(model.description.getTransformation() == DefaultTransformationType.Log);
        spec.setArima(model.description.getArimaComponent().clone());
        /*if (!spec.arima.isMean()) {
         if (process.isSeasonalMeanCorrection() || isMean(model)) {
         spec.arima.setMean(true);
         }
         }*/
        return spec;
    }

    private boolean isMean(PreprocessingModel model) {
        int nhp = model.description.getArimaComponent().getFreeParametersCount();
        if (model.description.isMean()) {
            ConcentratedLikelihood ll = model.estimation.getLikelihood();
            double mu = ll.getB()[0];
            double emu = ll.getBSer(0, true, nhp);
            double tmu = mu / emu;
            if (Math.abs(tmu) > TMU) {
                return true;
            }
            // compute LB of the residuals, of the model (lin + mean) and (lin)
            int nlb = TramoProcessor.calcLBLength(model.description.getFrequency());
            double lbTramo = ljungBox(ll.getResiduals(), nlb);

            TsData lin = model.linearizedSeries(true);
            double lbMean = test(lin, model.estimation.getArima(), true, nlb);
            // LB_LIMIT should depend on lb lenght (-> freq)) !!!
            if (lbMean <= LB_FACTOR * lbTramo || lbMean <= LB_LIMIT) {
                return true;
            }
            double lbNoMean = test(lin, model.estimation.getArima(), false, nlb);
            if (lbNoMean > LB_FACTOR * lbTramo && lbNoMean > LB_LIMIT) {
                return true;
            }
        }
        return false;
    }
    private static final double TMU = 1.9;
    private static final double LB_FACTOR = 1.5;
    private static final double LB_LIMIT = 50;

    private double ljungBox(double[] residuals, int nlb) {
        LjungBoxTest tlb = new LjungBoxTest();
        tlb.setK(nlb);
        tlb.test(new ReadDataBlock(residuals));
        if (tlb.isValid()) {
            return tlb.getValue();
        } else {
            return 0;   // should not happen
        }
    }

    private double test(TsData lin, SarimaModel arima, boolean mean, int nlb) {
        FastArimaML m1 = new FastArimaML();
        m1.setMeanCorrection(mean);
        m1.setModel(arima);
        DataBlock data = new DataBlock(lin.getValues());
        if (m1.process(data)) {
            return ljungBox(m1.getResiduals(), nlb);
        } else {
            return 0;
        }
    }

    private void correctBias(TsData y, IPreprocessingFilter filter) {
        double bias = filter.getBiasCorrection(ComponentType.Series);
        if (bias == 0) {
            return;
        }
        y.getValues().add(bias);
    }

    private void correctBias(DefaultSeriesDecomposition decomposition, IPreprocessingFilter filter) {
        double bias = filter.getBiasCorrection(ComponentType.Series);
        if (bias == 0) {
            return;
        }
        boolean mul = decomposition.getMode() != DecompositionMode.Additive;
        if (mul) {
            bias = Math.exp(bias);
        }
        TsData s = decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        if (s != null) {
            if (mul) {
                s.getValues().div(bias);
            } else {
                s.getValues().sub(bias);
            }
        }
        s = decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
        if (s != null) {
            if (mul) {
                s.getValues().div(bias);
            } else {
                s.getValues().sub(bias);
            }
        }
        s = decomposition.getSeries(ComponentType.Series, ComponentInformation.Value);
        if (s != null) {
            if (mul) {
                s.getValues().div(bias);
            } else {
                s.getValues().sub(bias);
            }
        }
        s = decomposition.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        if (s != null) {
            if (mul) {
                s.getValues().div(bias);
            } else {
                s.getValues().sub(bias);
            }
        }
    }
}
