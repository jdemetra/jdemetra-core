/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import demetra.regarima.TransformSpec;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TransformProto {

    public void fill(TransformSpec spec, X13Protos.RegArimaSpec.TransformSpec.Builder builder) {
        builder.setTransformation(RegArimaProtosUtility.convert(spec.getFunction()))
                .setAdjust(RegArimaProtosUtility.convert(spec.getAdjust()))
                .setAicdiff(spec.getAicDiff());
    }

    public X13Protos.RegArimaSpec.TransformSpec convert(TransformSpec spec) {
        X13Protos.RegArimaSpec.TransformSpec.Builder builder = X13Protos.RegArimaSpec.TransformSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public TransformSpec convert(X13Protos.RegArimaSpec.TransformSpec spec) {
        return TransformSpec.builder()
                .function(RegArimaProtosUtility.convert(spec.getTransformation()))
                .adjust(RegArimaProtosUtility.convert(spec.getAdjust()))
                .aicDiff(spec.getAicdiff())
                .build();

    }
}
