/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.regarima.io.protobuf.RegArimaEstimationProto;
import demetra.tramo.TramoSpec;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;
import demetra.tramo.TramoOutput;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoProto {

    public TramoSeatsProtos.TramoSpec convert(TramoSpec spec) {
        return TramoSeatsProtos.TramoSpec.newBuilder()
                .setBasic(BasicProto.convert(spec.getTransform()))
                .setTransform(TransformProto.convert(spec.getTransform()))
                .setOutlier(OutlierProto.convert(spec.getOutliers()))
                .setArima(RegArimaProtosUtility.convert(spec.getArima()))
                .setAutomodel(AutoModelProto.convert(spec.getAutoModel()))
                .setRegression(RegressionProto.convert(spec.getRegression()))
                .setEstimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }

    public TramoSpec convert(TramoSeatsProtos.TramoSpec spec) {
        return TramoSpec.builder()
                .transform(TransformProto.convert(spec.getBasic(), spec.getTransform()))
                .outliers(OutlierProto.convert(spec.getOutlier()))
                .arima(RegArimaProtosUtility.convert(spec.getArima()))
                .autoModel(AutoModelProto.convert(spec.getAutomodel()))
                .regression(RegressionProto.convert(spec.getRegression(), spec.getOutlier().getTcrate()))
                .estimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }
    
        public TramoSeatsProtos.TramoOutput convert(TramoOutput output){
        TramoSeatsProtos.TramoOutput.Builder builder = 
                TramoSeatsProtos.TramoOutput.newBuilder()
                .setEstimationSpec(TramoProto.convert(output.getEstimationSpec()));
        
        if (output.getResult() != null){
            builder.setResult(RegArimaEstimationProto.convert(output.getResult()))
                    .setResultSpec(TramoProto.convert(output.getResultSpec()));
        }
        // TODO detail and logs
        
        return builder.build();
    }

}
