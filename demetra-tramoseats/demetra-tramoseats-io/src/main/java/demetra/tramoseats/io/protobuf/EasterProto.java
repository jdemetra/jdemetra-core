/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.tramo.EasterSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EasterProto {

    public void fill(EasterSpec spec, TramoSeatsProtos.TramoSpec.EasterSpec.Builder builder) {
        builder.setType(TramoSeatsProtosUtility.convert(spec.getType()))
                .setDuration(spec.getDuration())
                .setJulian(spec.isJulian())
                .setTest(spec.isTest());
    }

    public TramoSeatsProtos.TramoSpec.EasterSpec convert(EasterSpec spec) {
        TramoSeatsProtos.TramoSpec.EasterSpec.Builder builder = TramoSeatsProtos.TramoSpec.EasterSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public EasterSpec convert(TramoSeatsProtos.TramoSpec.EasterSpec spec) {
        return EasterSpec.builder()
                .duration(spec.getDuration())
                .type(TramoSeatsProtosUtility.convert(spec.getType()))
                .test(spec.getTest())
                .julian(spec.getJulian())
                .build();

    }

}
