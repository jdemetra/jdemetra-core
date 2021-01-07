/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import demetra.regarima.RegArimaSpec;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaProto {

    public X13Protos.RegArimaSpec convert(RegArimaSpec spec) {
        return X13Protos.RegArimaSpec.newBuilder()
                .setBasic(BasicProto.convert(spec.getBasic()))
                .setTransform(TransformProto.convert(spec.getTransform()))
                .setOutlier(OutlierProto.convert(spec.getOutliers()))
                .setArima(RegArimaProtosUtility.convert(spec.getArima()))
                .setAutomodel(AutoModelProto.convert(spec.getAutoModel()))
                .setRegression(RegressionProto.convert(spec.getRegression()))
                .setEstimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }

    public RegArimaSpec convert(X13Protos.RegArimaSpec spec) {
        return RegArimaSpec.builder()
                .basic(BasicProto.convert(spec.getBasic()))
                .transform(TransformProto.convert(spec.getTransform()))
                .outliers(OutlierProto.convert(spec.getOutlier()))
                .arima(RegArimaProtosUtility.convert(spec.getArima()))
                .autoModel(AutoModelProto.convert(spec.getAutomodel()))
                .regression(RegressionProto.convert(spec.getRegression(), spec.getOutlier().getMonthlytcrate()))
                .estimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }
}
