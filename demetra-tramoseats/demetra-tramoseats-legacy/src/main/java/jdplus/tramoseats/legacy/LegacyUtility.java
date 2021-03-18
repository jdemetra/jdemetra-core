/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.legacy;

import demetra.arima.SarimaModel;
import demetra.data.DoubleSeq;
import demetra.likelihood.ParametersEstimation;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.timeseries.regression.modelling.LightLinearModel;
import demetra.tramo.TramoSpec;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;


/**
 *
 * @author palatej
 */
        @lombok.experimental.UtilityClass
public class LegacyUtility {
    TramoSpecification toLegacy(TramoSpec spec){
        if (spec.equals(TramoSpec.TRfull))
            return TramoSpecification.TRfull;
        else if (spec.equals(TramoSpec.TR5))
            return TramoSpecification.TR5;
        else if (spec.equals(TramoSpec.TR4))
            return TramoSpecification.TR4;
        else if (spec.equals(TramoSpec.TR3))
            return TramoSpecification.TR3;
        else if (spec.equals(TramoSpec.TR2))
            return TramoSpecification.TR2;
        else if (spec.equals(TramoSpec.TR1))
            return TramoSpecification.TR1;
        else if (spec.equals(TramoSpec.TR0))
            return TramoSpecification.TR0;
        else
            throw new java.lang.UnsupportedOperationException();
    }
    
//    LinearModelEstimation<SarimaModel> toApi(PreprocessingModel model){
//        // likelihood statistics
//        LikelihoodStatistics stat = model.estimation.getStatistics();
//        demetra.likelihood.LikelihoodStatistics nstat = demetra.likelihood.LikelihoodStatistics.statistics(stat.logLikelihood, stat.observationsCount)
//                .differencingOrder(stat.observationsCount-stat.effectiveObservationsCount)
//                .llAdjustment(stat.transformationAdjustment)
//                .ssq(stat.SsqErr)
//                .parametersCount(stat.estimatedParametersCount)
//                .build();
//        
//        IReadDataBlock p = model.estimation.getArima().getParameters();
//        double[] params=new double[p.getLength()];
//        p.copyTo(params, 0);
//        return LinearModelEstimation.<SarimaModel>builder()
//                .statistics(nstat)
//                .parameters(new ParametersEstimation(DoubleSeq.of(params), null, null, null))
//                .build();
//    }
    
    public ec.tstoolkit.timeseries.simplets.TsData toLegacy(TsData series){
                int y=series.getStart().year(), p=series.getStart().annualPosition();
        ec.tstoolkit.timeseries.simplets.TsFrequency freq=ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(series.getAnnualFrequency());
        return new
                ec.tstoolkit.timeseries.simplets.TsData(freq, y, p,series.getValues().toArray(), false);

    }
    
    public MatrixType fromLegacy(ec.tstoolkit.maths.matrices.Matrix M){
        return MatrixType.of(M.internalStorage(), M.getRowsCount(), M.getColumnsCount());
    }

    public DoubleSeq fromLegacy(ec.tstoolkit.data.IReadDataBlock v){
        double[] p=new double[v.getLength()];
        v.copyTo(p, 0);
        return DoubleSeq.of(p);
    }

    public GeneralLinearModel<SarimaModel> toApi(PreprocessingModel model) {
        LikelihoodStatistics stat = model.estimation.getStatistics();
        demetra.likelihood.LikelihoodStatistics nstat = demetra.likelihood.LikelihoodStatistics.statistics(stat.logLikelihood, stat.observationsCount)
                .differencingOrder(stat.observationsCount - stat.effectiveObservationsCount)
                .llAdjustment(stat.transformationAdjustment)
                .ssq(stat.SsqErr)
                .parametersCount(stat.estimatedParametersCount)
                .build();
        LightLinearModel.Description.Builder<SarimaModel> dbuilder = LightLinearModel.Description.<SarimaModel>builder();
        LightLinearModel.Estimation.Builder ebuilder = LightLinearModel.Estimation.builder()
                .residuals(DoubleSeq.of(model.getFullResiduals().internalStorage()))
                .parameters(new ParametersEstimation(fromLegacy(model.estimation.getArima().getParameters()), fromLegacy(model.estimation.getParametersCovariance()), null, null))
                .statistics(nstat);
        
        return LightLinearModel.<SarimaModel>builder()
                .description(dbuilder.build())
                .estimation(ebuilder.build())
                .build();

    }
}
