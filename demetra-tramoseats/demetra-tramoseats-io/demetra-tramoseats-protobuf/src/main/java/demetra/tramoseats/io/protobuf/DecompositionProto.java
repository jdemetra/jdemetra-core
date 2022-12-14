/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;


/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class DecompositionProto {

    public void fill(demetra.seats.DecompositionSpec spec, DecompositionSpec.Builder builder) {
        builder.setXlBoundary(spec.getXlBoundary())
                .setTrendBoundary((spec.getTrendBoundary()))
                .setSeastolerance(spec.getSeasTolerance())
                .setSeasBoundary(spec.getSeasBoundary())
                .setSeasBoundaryAtPi(spec.getSeasBoundaryAtPi())
                .setNfcasts(spec.getForecastCount())
                .setNbcasts(spec.getBackcastCount())
                .setApproximation(convert(spec.getApproximationMode()))
                .setAlgorithm(convert(spec.getMethod()))
                .setBiasCorrection(spec.getBiasCorrection() != demetra.seats.DecompositionSpec.BiasCorrection.None);
    }

    public DecompositionSpec convert(demetra.seats.DecompositionSpec spec) {
        DecompositionSpec.Builder builder = DecompositionSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public demetra.seats.DecompositionSpec convert(DecompositionSpec spec) {
        return demetra.seats.DecompositionSpec.builder()
                .xlBoundary(spec.getXlBoundary())
                .trendBoundary(spec.getTrendBoundary())
                .seasTolerance(spec.getSeastolerance())
                .seasBoundary(spec.getSeasBoundary())
                .seasBoundaryAtPi(spec.getSeasBoundaryAtPi())
                .forecastCount(spec.getNfcasts())
                .backcastCount(spec.getNbcasts())
                .biasCorrection(spec.getBiasCorrection() ? demetra.seats.DecompositionSpec.BiasCorrection.Legacy : demetra.seats.DecompositionSpec.BiasCorrection.None)
                .approximationMode(convert(spec.getApproximation()))
                .method(convert(spec.getAlgorithm()))
                .build();
    }

    SeatsApproximation convert(demetra.seats.DecompositionSpec.ModelApproximationMode app) {
        switch (app) {
            case Legacy:
                return SeatsApproximation.SEATS_APP_LEGACY;
            case Noisy:
                return SeatsApproximation.SEATS_APP_NOISY;
            default:
                return SeatsApproximation.SEATS_APP_NONE;
        }
    }

    SeatsAlgorithm convert(demetra.seats.DecompositionSpec.ComponentsEstimationMethod method) {
        switch (method) {
            case KalmanSmoother:
                return SeatsAlgorithm.SEATS_ALG_KALMANSMOOTHER;
            default:
                return SeatsAlgorithm.SEATS_ALG_BURMAN;
        }
    }

    demetra.seats.DecompositionSpec.ModelApproximationMode convert(SeatsApproximation app) {
        switch (app) {
            case SEATS_APP_LEGACY:
                return demetra.seats.DecompositionSpec.ModelApproximationMode.Legacy;
            case SEATS_APP_NOISY:
                return demetra.seats.DecompositionSpec.ModelApproximationMode.Noisy;
            default:
                return demetra.seats.DecompositionSpec.ModelApproximationMode.None;
        }
    }

    demetra.seats.DecompositionSpec.ComponentsEstimationMethod convert(SeatsAlgorithm method) {
        switch (method) {
            case SEATS_ALG_KALMANSMOOTHER:
                return demetra.seats.DecompositionSpec.ComponentsEstimationMethod.KalmanSmoother;
            default:
                return demetra.seats.DecompositionSpec.ComponentsEstimationMethod.Burman;
        }
    }
}
