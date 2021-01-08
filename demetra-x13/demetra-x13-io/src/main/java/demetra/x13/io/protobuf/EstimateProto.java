/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.EstimateSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EstimateProto {
    public void fill(EstimateSpec spec, X13Protos.RegArimaSpec.EstimateSpec.Builder builder) {
        builder.setSpan(ToolkitProtosUtility.convert(spec.getSpan()))
                .setTol(spec.getTol());
    }

    public X13Protos.RegArimaSpec.EstimateSpec convert(EstimateSpec spec) {
        X13Protos.RegArimaSpec.EstimateSpec.Builder builder = X13Protos.RegArimaSpec.EstimateSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public EstimateSpec convert(X13Protos.RegArimaSpec.EstimateSpec spec) {
        return EstimateSpec.builder()
                .span(ToolkitProtosUtility.convert(spec.getSpan()))
                .tol(spec.getTol())
                .build();
    }

}
