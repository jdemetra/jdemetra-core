/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.seats.DecompositionSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class DecompositionProto {

    public void fill(DecompositionSpec spec, TramoSeatsProtos.DecompositionSpec.Builder builder) {
        builder.setXlBoundary(spec.getXlBoundary())
                .setTrendBoundary((spec.getTrendBoundary()))
                .setSeastolerance(spec.getSeasTolerance())
                .setSeasBoundary(spec.getSeasBoundary())
                .setSeasBoundaryAtPi(spec.getSeasBoundaryAtPi())
                .setNfcasts(spec.getForecastCount())
                .setNbcasts(spec.getBackcastCount())
                .setApproximation(convert(spec.getApproximationMode()))
                .setAlgorithm(convert(spec.getMethod()))
                .setBiasCorrection(spec.getBiasCorrection() != DecompositionSpec.BiasCorrection.None);
    }

    public TramoSeatsProtos.DecompositionSpec convert(DecompositionSpec spec) {
        TramoSeatsProtos.DecompositionSpec.Builder builder = TramoSeatsProtos.DecompositionSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public DecompositionSpec convert(TramoSeatsProtos.DecompositionSpec spec) {
        return DecompositionSpec.builder()
                .xlBoundary(spec.getXlBoundary())
                .trendBoundary(spec.getTrendBoundary())
                .seasTolerance(spec.getSeastolerance())
                .seasBoundary(spec.getSeasBoundary())
                .seasBoundaryAtPi(spec.getSeasBoundaryAtPi())
                .forecastCount(spec.getNfcasts())
                .backcastCount(spec.getNbcasts())
                .biasCorrection(spec.getBiasCorrection() ? DecompositionSpec.BiasCorrection.Legacy : DecompositionSpec.BiasCorrection.None)
                .approximationMode(convert(spec.getApproximation()))
                .method(convert(spec.getAlgorithm()))
                .build();
    }

    TramoSeatsProtos.SeatsApproximation convert(DecompositionSpec.ModelApproximationMode app) {
        switch (app) {
            case Legacy:
                return TramoSeatsProtos.SeatsApproximation.SEATS_APP_LEGACY;
            case Noisy:
                return TramoSeatsProtos.SeatsApproximation.SEATS_APP_NOISY;
            default:
                return TramoSeatsProtos.SeatsApproximation.SEATS_APP_NONE;
        }
    }

    TramoSeatsProtos.SeatsAlgorithm convert(DecompositionSpec.ComponentsEstimationMethod method) {
        switch (method) {
            case KalmanSmoother:
                return TramoSeatsProtos.SeatsAlgorithm.SEATS_ALG_KALMANSMOOTHER;
            default:
                return TramoSeatsProtos.SeatsAlgorithm.SEATS_ALG_BURMAN;
        }
    }

    DecompositionSpec.ModelApproximationMode convert(TramoSeatsProtos.SeatsApproximation app) {
        switch (app) {
            case SEATS_APP_LEGACY:
                return DecompositionSpec.ModelApproximationMode.Legacy;
            case SEATS_APP_NOISY:
                return DecompositionSpec.ModelApproximationMode.Noisy;
            default:
                return DecompositionSpec.ModelApproximationMode.None;
        }
    }

    DecompositionSpec.ComponentsEstimationMethod convert(TramoSeatsProtos.SeatsAlgorithm method) {
        switch (method) {
            case SEATS_ALG_KALMANSMOOTHER:
                return DecompositionSpec.ComponentsEstimationMethod.KalmanSmoother;
            default:
                return DecompositionSpec.ComponentsEstimationMethod.Burman;
        }
    }
}
