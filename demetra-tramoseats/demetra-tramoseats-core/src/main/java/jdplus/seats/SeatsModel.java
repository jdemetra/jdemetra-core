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
import demetra.arima.SarimaSpec;
import demetra.sa.ComponentType;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.SarimaModel;
import jdplus.ucarima.UcarimaModel;
import demetra.data.DoubleSeq;
import demetra.data.ParameterSpec;
import demetra.likelihood.LikelihoodStatistics;
import demetra.sa.SeriesDecomposition;
import demetra.seats.SeatsException;
import demetra.seats.SeatsSpec;
import jdplus.arima.ArimaModel;
import static jdplus.math.linearfilters.BackFilter.D1;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaToolkit;
import jdplus.regsarima.regular.SeasonalityDetector;
import jdplus.tramo.TramoSeasonalityDetector;

/**
 *
 * @author Jean Palate
 */
@lombok.Data()
public class SeatsModel {

    public static SeatsModel of(DoubleSeq series, int period, SeatsSpec spec) {
        if (series.length() < 3 * period) {
            throw new SeatsException(SeatsException.ERR_LENGTH);
        }
        SeatsModel model;
        if (spec.getModelSpec().getSarimaSpec().isFixed()) {
            model = buildDefinedModel(series, period, spec);
        } else {
            model = buildEstimatedModel(series, period, spec);
        }

        model.setBackcastsCount(spec.getComponentsSpec().getBackCastCount());
        model.setForecastsCount(spec.getComponentsSpec().getForecastCount());

        return model;
    }

    private static SeatsModel buildDefinedModel(DoubleSeq series, int period, SeatsSpec spec) {
        boolean log = spec.getModelSpec().isLog();
        DoubleSeq nseries = log ? series.log() : series;
        boolean mean = spec.getModelSpec().isMeanCorrection();
        SarimaSpec sarimaSpec = spec.getModelSpec().getSarimaSpec();
        SarimaOrders orders = sarimaSpec.specification(period);
        SarimaModel sarima = SarimaModel.builder(orders)
                .phi(ParameterSpec.values(sarimaSpec.getPhi()))
                .bphi(ParameterSpec.values(sarimaSpec.getBphi()))
                .theta(ParameterSpec.values(sarimaSpec.getTheta()))
                .btheta(ParameterSpec.values(sarimaSpec.getBtheta()))
                .build();
        SeasonalityDetector detector = new TramoSeasonalityDetector();
        SeasonalityDetector.Seasonality seas = detector.hasSeasonality(nseries, period);
        SeatsModel seatsModel = new SeatsModel(series, nseries, log, sarima, seas.getAsInt() > 1);
        seatsModel.meanCorrection = mean;
        return seatsModel;
    }

    private static SeatsModel buildEstimatedModel(DoubleSeq series, int period, SeatsSpec spec) {
        boolean log = spec.getModelSpec().isLog();
        DoubleSeq nseries = log ? series.log() : series;
        boolean mean = spec.getModelSpec().isMeanCorrection();
        SarimaSpec sarimaSpec = spec.getModelSpec().getSarimaSpec();
        if (sarimaSpec.hasFixedParameters()) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        SarimaOrders orders = sarimaSpec.specification(period);
        SarimaModel sarima = SarimaModel.builder(orders)
                .phi(ParameterSpec.values(sarimaSpec.getPhi()))
                .bphi(ParameterSpec.values(sarimaSpec.getBphi()))
                .theta(ParameterSpec.values(sarimaSpec.getTheta()))
                .btheta(ParameterSpec.values(sarimaSpec.getBtheta()))
                .build();
        RegArimaModel regarima = RegArimaModel.<SarimaModel>builder()
                .y(nseries)
                .arima(sarima)
                .meanCorrection(mean)
                .build();
        RegArimaEstimation<SarimaModel> estimation = RegArimaToolkit.robustEstimation(regarima, null);

        SarimaModel arima = estimation.getModel().arima();
        LikelihoodStatistics stat = estimation.statistics();
        double var = stat.getSsqErr() / (stat.getEffectiveObservationsCount() - stat.getEstimatedParametersCount());
        SeasonalityDetector detector = new TramoSeasonalityDetector();
        SeasonalityDetector.Seasonality seas = detector.hasSeasonality(nseries, period);

        SeatsModel seatsModel = new SeatsModel(series, nseries, log, arima, seas.getAsInt() > 1);
        seatsModel.meanCorrection = mean;
        seatsModel.innovationVariance = var;
        return seatsModel;
    }

//    public static Builder builder(DoubleSeq series, SarimaModel model) {
//        return new Builder(series, model);
//    }
//
//    public static Builder builder(DoubleSeq series, int period) {
//        return new Builder(series, period);
//    }
//
//    public static class Builder {
//
//        private final DoubleSeq series;
//        private final SarimaModel model;
//        private final int period;
//        private boolean log, meanCorrection;
//        private int seasonality = -1;
//        private double innovationVariance = 1;
//
//        private Builder(DoubleSeq series, SarimaModel model) {
//            this.series = series;
//            this.model = model;
//            this.period = model.getFrequency();
//        }
//
//        private Builder(DoubleSeq series, int period) {
//            this.series = series;
//            this.model = null;
//            this.period = period;
//        }
//
//        public Builder log(boolean log) {
//            this.log = log;
//            return this;
//        }
//
//        public Builder meanCorrection(boolean mean) {
//            this.meanCorrection = mean;
//            return this;
//        }
//
//        public Builder innovationVariance(double ivar) {
//            if (model == null) {
//                throw new IllegalArgumentException();
//            }
//            this.innovationVariance = ivar;
//            return this;
//        }
//
//        public Builder hasSeasonality(boolean seas) {
//            this.seasonality = 1;
//            return this;
//        }
//
//        private SeatsModel buildDefaultModel() {
//            SarimaOrders spec = SarimaOrders.airline(period);
//            SarimaModel airline = SarimaModel.builder(spec)
//                    .setDefault()
//                    .build();
//            RegArimaModel regarima = RegArimaModel.<SarimaModel>builder()
//                    .y(series)
//                    .arima(airline)
//                    .meanCorrection(meanCorrection)
//                    .build();
//            RegArimaEstimation<SarimaModel> estimation = GlsSarimaProcessor.PROCESSOR.process(regarima, null);
//
//            SarimaModel arima = estimation.getModel().arima();
//            LikelihoodStatistics stat = estimation.statistics();
//            double var = stat.getSsqErr() / (stat.getEffectiveObservationsCount() - stat.getEstimatedParametersCount());
//
//            SeatsModel seatsModel = new SeatsModel(series, log, arima, seasonality == 1);
//            seatsModel.meanCorrection = meanCorrection;
//            seatsModel.innovationVariance = var;
//            return seatsModel;
//        }
//
//        public SeatsModel build() {
//            if (series.length() < 3 * period) {
//                throw new SeatsException(SeatsException.ERR_LENGTH);
//            }
//            if (seasonality < 0) {
//                SeasonalityDetector detector = new TramoSeasonalityDetector();
//                SeasonalityDetector.Seasonality seas = detector.hasSeasonality(series, period);
//                if (seas.getAsInt() > 1) {
//                    seasonality = 1;
//                } else {
//                    seasonality = 0;
//                }
//            }
//            if (model == null) {
//                return buildDefaultModel();
//            }
//
//            SeatsModel seatsModel = new SeatsModel(series, log, model, seasonality == 1);
//            seatsModel.meanCorrection = meanCorrection;
//            seatsModel.innovationVariance = innovationVariance;
//            return seatsModel;
//        }
//    }
    private final DoubleSeq originalSeries, transformedSeries;
    private final boolean logTransformation;
    private final SarimaModel originalModel;
    private final boolean significantSeasonality;
    private boolean meanCorrection;
    private int forecastsCount, backcastsCount;
    private SarimaModel currentModel;
    private UcarimaModel ucarimaModel;
    private SeriesDecomposition initialComponents, finalComponents;
    private double innovationVariance;

    public RegArimaModel<SarimaModel> asRegarima() {
        return RegArimaModel.<SarimaModel>builder()
                .y(transformedSeries)
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
                .y(transformedSeries)
                .arima(this.currentModel)
                .meanCorrection(meanCorrection)
                .build();
        RegArimaEstimation<SarimaModel> estimation = RegArimaToolkit.concentratedLikelihood(regarima);
        LikelihoodStatistics stat = estimation.statistics();
        double var = stat.getSsqErr() / (stat.getEffectiveObservationsCount() - stat.getEstimatedParametersCount());
        if (set) {
            this.innovationVariance = var;
        }
        return var;
    }

    public int getPeriod() {
        return originalModel.getFrequency();
    }

    public int getBackcastsCount() {
        if (backcastsCount < 0) {
            return -getPeriod() * backcastsCount;
        } else {
            return backcastsCount;
        }
    }

    public int getForecastsCount() {
        if (forecastsCount < 0) {
            return -getPeriod() * forecastsCount;
        } else {
            return forecastsCount;
        }
    }

    /**
     * Gets the compact form of the model.
     * The returned UCARIMA model is modified as follows:
     * - the mean correction is replaced by unit roots in the auto-regressive
     * and
     * moving average parts.
     * - the transitory component and the noise are added up
     * - empty (null) components are removed
     * The components in the compact form can be retrieved by means of the
     * componentsType method
     *
     * @return
     */
    public UcarimaModel compactUcarimaModel() {
        if (ucarimaModel == null) {
            return null;
        }
        // correction for mean...
        UcarimaModel ucm = ucarimaModel;
        if (meanCorrection) {
            UcarimaModel.Builder tmp = UcarimaModel.builder();
            ArimaModel tm = ucm.getComponent(0);
            if (tm.isNull()) {
                tm = new ArimaModel(null, D1, D1, 0);
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
