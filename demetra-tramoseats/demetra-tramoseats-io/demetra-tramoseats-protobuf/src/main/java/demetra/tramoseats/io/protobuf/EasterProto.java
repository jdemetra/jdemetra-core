/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.data.Parameter;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
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
        Parameter c = spec.getCoefficient();
        if (c != null)
            builder.setCoefficient(ToolkitProtosUtility.convert(c));
        return builder.build();
    }

    public EasterSpec convert(TramoSeatsProtos.TramoSpec.EasterSpec spec) {
        EasterSpec.Builder builder=EasterSpec.builder()
                .duration(spec.getDuration())
                .type(TramoSeatsProtosUtility.convert(spec.getType()))
                .test(spec.getTest())
                .julian(spec.getJulian());
        if (spec.hasCoefficient())
            builder.coefficient(ToolkitProtosUtility.convert(spec.getCoefficient()));
        return builder.build();
    }

}
