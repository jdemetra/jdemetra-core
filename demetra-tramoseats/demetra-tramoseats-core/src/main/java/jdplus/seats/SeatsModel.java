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
import demetra.seats.SeatsModelSpec;
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

    public static SeatsModel of(SeatsModelSpec spec) {
        SeatsModel model;
        if (spec.getSarimaSpec().hasFreeParameters()) {
            model = buildEstimatedModel(spec);
        } else {
            model = buildDefinedModel(spec);
        }

//        model.setBackcastsCount(spec.getComponentsSpec().getBackCastCount());
//        model.setForecastsCount(spec.getComponentsSpec().getForecastCount());

        return model;
    }

    private static SeatsModel buildDefinedModel(SeatsModelSpec spec) {
        DoubleSeq series=spec.getSeries();
        int period=spec.getPeriod();
        boolean log = spec.isLog();
        DoubleSeq nseries = log ? series.log() : series;
        boolean mean = spec.isMeanCorrection();
        SarimaSpec sarimaSpec = spec.getSarimaSpec();
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

    private static SeatsModel buildEstimatedModel(SeatsModelSpec spec) {
        DoubleSeq series=spec.getSeries();
        int period=spec.getPeriod();
        boolean log = spec.isLog();
        DoubleSeq nseries = log ? series.log() : series;
        boolean mean = spec.isMeanCorrection();
        SarimaSpec sarimaSpec = spec.getSarimaSpec();
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

    private final DoubleSeq originalSeries, transformedSeries;
    private final boolean logTransformation;
    private final SarimaModel originalModel;
    private final boolean significantSeasonality;
    private boolean meanCorrection;
//    private int forecastsCount, backcastsCount;
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

    public int extrapolationCount(int nf) {
        if (nf < 0) {
            return -getPeriod() * nf;
        } else {
            return nf;
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
