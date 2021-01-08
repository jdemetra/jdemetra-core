/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.EasterSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EasterProto {

    public void fill(EasterSpec spec, X13Protos.RegArimaSpec.EasterSpec.Builder builder) {
        builder.setType(X13ProtosUtility.convert(spec.getType()))
                .setDuration(spec.getDuration())
                .setTest(X13ProtosUtility.convert(spec.getTest()));
    }

    public X13Protos.RegArimaSpec.EasterSpec convert(EasterSpec spec) {
        X13Protos.RegArimaSpec.EasterSpec.Builder builder = X13Protos.RegArimaSpec.EasterSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public byte[] toBuffer(EasterSpec spec) {
        return convert(spec).toByteArray();
    }

    public EasterSpec convert(X13Protos.RegArimaSpec.EasterSpec spec) {
        return EasterSpec.builder()
                .automatic(spec.getDuration() == 0)
                .duration(spec.getDuration())
                .type(X13ProtosUtility.convert(spec.getType()))
                .test(X13ProtosUtility.convert(spec.getTest()))
                .build();

    }

    public EasterSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        X13Protos.RegArimaSpec.EasterSpec spec = X13Protos.RegArimaSpec.EasterSpec.parseFrom(bytes);
        return convert(spec);
    }

}
