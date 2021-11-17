/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.seats;

import demetra.arima.SarimaOrders;
import demetra.modelling.implementations.SarimaSpec;
import demetra.sa.ComponentType;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.SarimaModel;
import jdplus.ucarima.UcarimaModel;
import demetra.data.Parameter;
import demetra.likelihood.LikelihoodStatistics;
import demetra.sa.SeriesDecomposition;
import demetra.seats.SeatsModelSpec;
import demetra.timeseries.TsData;
import jdplus.arima.ArimaModel;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.math.linearfilters.BackFilter;
import static jdplus.math.linearfilters.BackFilter.D1;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaToolkit;
import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import jdplus.regsarima.regular.SeasonalityDetector;
import jdplus.tramo.TramoSeasonalityDetector;

/**
 *
 * @author Jean Palate
 */
@lombok.Data()
public class SeatsModel {

    public static SeatsModel of(SeatsModelSpec spec) {
        SeatsModel model;
        if (!spec.getSarimaSpec().isDefined()) {
            model = buildEstimatedModel(spec);
        } else {
            model = buildDefinedModel(spec);
        }

//        model.setBackcastsCount(spec.getComponentsSpec().getBackCastCount());
//        model.setForecastsCount(spec.getComponentsSpec().getForecastCount());
        return model;
    }

    private static SeatsModel buildDefinedModel(SeatsModelSpec spec) {
        TsData series = spec.getSeries();
        int period = series.getAnnualFrequency();
        boolean log = spec.isLog();
        TsData nseries = log ? series.log() : series;
        boolean mean = spec.isMeanCorrection();
        SarimaSpec sarimaSpec = spec.getSarimaSpec().withPeriod(period);
        SarimaOrders orders = sarimaSpec.orders();
        SarimaModel sarima = SarimaModel.builder(orders)
                .phi(Parameter.values(sarimaSpec.getPhi()))
                .bphi(Parameter.values(sarimaSpec.getBphi()))
                .theta(Parameter.values(sarimaSpec.getTheta()))
                .btheta(Parameter.values(sarimaSpec.getBtheta()))
                .build();
        SeasonalityDetector detector = new TramoSeasonalityDetector();
        SeasonalityDetector.Seasonality seas = detector.hasSeasonality(nseries.getValues(), period);
        SeatsModel seatsModel = new SeatsModel(series, nseries, log, sarima, seas.getAsInt() > 1);
        seatsModel.meanCorrection = mean;
        double var = spec.getInnovationVariance();
        if (var == 0) {
            RegArimaModel regarima = RegArimaModel.<SarimaModel>builder()
                    .y(nseries.getValues())
                    .arima(sarima)
                    .meanCorrection(mean)
                    .build();
            ConcentratedLikelihoodWithMissing ll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
            var = ll.ssq() / (ll.dim() - orders.getParametersCount());
        }
        seatsModel.innovationVariance = var;
        return seatsModel;
    }

    private static SeatsModel buildEstimatedModel(SeatsModelSpec spec) {
        TsData series = spec.getSeries();
        int period = series.getAnnualFrequency();
        boolean log = spec.isLog();
        TsData nseries = log ? series.log() : series;
        boolean mean = spec.isMeanCorrection();
        SarimaSpec sarimaSpec = spec.getSarimaSpec().withPeriod(period);
        if (sarimaSpec.hasFixedParameters()) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        SarimaOrders orders = sarimaSpec.orders();
        SarimaModel sarima = SarimaModel.builder(orders)
                .phi(Parameter.values(sarimaSpec.getPhi()))
                .bphi(Parameter.values(sarimaSpec.getBphi()))
                .theta(Parameter.values(sarimaSpec.getTheta()))
                .btheta(Parameter.values(sarimaSpec.getBtheta()))
                .build();
        RegArimaModel regarima = RegArimaModel.<SarimaModel>builder()
                .y(nseries.getValues())
                .arima(sarima)
                .meanCorrection(mean)
                .build();
        RegArimaEstimation<SarimaModel> estimation = RegArimaToolkit.robustEstimation(regarima, null);
        SarimaModel arima = estimation.getModel().arima();
        LikelihoodStatistics stat = estimation.statistics();
        double var = stat.getSsqErr() / (stat.getEffectiveObservationsCount() - stat.getEstimatedParametersCount());
        SeasonalityDetector detector = new TramoSeasonalityDetector();
        SeasonalityDetector.Seasonality seas = detector.hasSeasonality(nseries.getValues(), period);

        SeatsModel seatsModel = new SeatsModel(series, nseries, log, arima, seas.getAsInt() > 1);
        seatsModel.meanCorrection = mean;
        seatsModel.innovationVariance = var;
        return seatsModel;
    }

    private final TsData originalSeries, transformedSeries;
    private final boolean logTransformation;
    private final SarimaModel originalModel;
    private final boolean significantSeasonality;
    private boolean meanCorrection;
//    private int forecastsCount, backcastsCount;
    private SarimaModel currentModel;
    private boolean parametersCutOff, modelChanged;
    private UcarimaModel ucarimaModel;
    private SeriesDecomposition initialComponents, finalComponents;
    private double innovationVariance;

    public RegArimaModel<SarimaModel> asRegarima() {
        return RegArimaModel.<SarimaModel>builder()
                .y(transformedSeries.getValues())
                .arima(currentModel)
                .meanCorrection(meanCorrection)
                .build();
    }

    /**
     * Computes the default innovation variance of the model
     *
     * @param set True if the result is set in the model for future use, false
     * otherwise
     * @return
     */
    public double defaultInnovation(boolean set) {
        RegArimaModel regarima = RegArimaModel.<SarimaModel>builder()
                .y(transformedSeries.getValues())
                .arima(this.currentModel)
                .meanCorrection(meanCorrection)
                .build();
        RegArimaEstimation<SarimaModel> estimation = RegArimaToolkit.concentratedLikelihood(regarima);
        LikelihoodStatistics stat = estimation.statistics();
        double var = stat.getSsqErr() / (stat.getEffectiveObservationsCount() - stat.getEstimatedParametersCount() + 1);
        if (set) {
            this.innovationVariance = var;
        }
        return var;
    }

    public int getPeriod() {
        return originalModel.getPeriod();
    }

    public int extrapolationCount(int nf) {
        if (nf < 0) {
            return -getPeriod() * nf;
        } else {
            return nf;
        }
    }

    /**
     * Gets the compact form of the model.The returned UCARIMA model is modified
     * as follows:
     * - if need be, the mean correction is replaced by unit roots in the
     * auto-regressive
     * and
     * moving average parts.
     * - the transitory component and the noise are added up
     * - empty (null) components are removed
     * The components in the compact form can be retrieved by means of the
     * componentsType method
     *
     * @param mean Pu the mean correction in the model (for estimation only)
     * @return
     */
    public UcarimaModel compactUcarimaModel(boolean mean) {
        if (ucarimaModel == null) {
            return null;
        }
        // correction for mean...
        UcarimaModel ucm = ucarimaModel;
        if (mean && meanCorrection) {
            UcarimaModel.Builder tmp = UcarimaModel.builder();
            ArimaModel tm = ucm.getComponent(0);
            if (tm.isNull()) {
                tm = new ArimaModel(BackFilter.ONE, D1, D1, 0);
            } else {
                tm = new ArimaModel(tm.getStationaryAr(), tm.getNonStationaryAr().times(D1),
                        tm.getMa().times(D1),
                        tm.getInnovationVariance());
            }
            tmp.add(tm);
            for (int i = 1; i < ucm.getComponentsCount(); ++i) {
                tmp.add(ucm.getComponent(i));
            }
            ucm = tmp.build();
        }
        if (ucm.getComponentsCount() > 3) {
            ucm = ucm.compact(2, 2);
        }
        return ucm.simplify();
    }

    public ComponentType[] componentsType() {
        if (ucarimaModel == null) {
            return null;
        }
        boolean hast = !ucarimaModel.getComponent(0).isNull()
                || meanCorrection;
        boolean hass = !ucarimaModel.getComponent(1).isNull();
        boolean hastr = !ucarimaModel.getComponent(2).isNull();
        boolean hasirr = hastr || !ucarimaModel.getComponent(3).isNull();
        int ncmps = (hast ? 1 : 0) + (hass ? 1 : 0) + (hasirr ? 1 : 0);
        ComponentType[] cmps = new ComponentType[ncmps];
        int icmp = 0;
        if (hast) {
            cmps[icmp++] = ComponentType.Trend;
        }
        if (hass) {
            cmps[icmp++] = ComponentType.Seasonal;
        }
        if (hasirr) {
            cmps[icmp++] = ComponentType.Irregular;
        }
        return cmps;
    }

}
