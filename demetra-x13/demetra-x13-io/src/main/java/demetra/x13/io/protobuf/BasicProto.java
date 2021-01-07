/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.BasicSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class BasicProto {

    public void fill(BasicSpec spec, X13Protos.RegArimaSpec.BasicSpec.Builder builder) {
        builder.setSpan(ToolkitProtosUtility.convert(spec.getSpan()))
                .setPreliminaryCheck(spec.isPreliminaryCheck())
                .setPreprocessing(spec.isPreprocessing());
    }

    public X13Protos.RegArimaSpec.BasicSpec convert(BasicSpec spec) {
        X13Protos.RegArimaSpec.BasicSpec.Builder builder = X13Protos.RegArimaSpec.BasicSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public byte[] toBuffer(BasicSpec spec) {
        return convert(spec).toByteArray();
    }

    public BasicSpec convert(X13Protos.RegArimaSpec.BasicSpec spec) {
        return BasicSpec.builder()
                .span(ToolkitProtosUtility.convert(spec.getSpan()))
                .preprocessing(spec.getPreprocessing())
                .preliminaryCheck(spec.getPreliminaryCheck())
                .build();

    }

    public BasicSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        X13Protos.RegArimaSpec.BasicSpec spec = X13Protos.RegArimaSpec.BasicSpec.parseFrom(bytes);
        return convert(spec);
    }

}
