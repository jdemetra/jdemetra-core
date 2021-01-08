/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import demetra.sa.io.protobuf.SaProtosUtility;
import demetra.x13.X13Spec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SpecProto {

    public X13Protos.Spec convert(X13Spec spec) {
        return X13Protos.Spec.newBuilder()
                .setRegarima(RegArimaProto.convert(spec.getRegArima()))
                .setX11(X11Proto.convert(spec.getX11()))
                .setBenchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }

    public X13Spec convert(X13Protos.Spec spec) {
        return X13Spec.builder()
                .regArima(RegArimaProto.convert(spec.getRegarima()))
                .x11(X11Proto.convert(spec.getX11()))
                .benchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }
}
