/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.tramo.AutoModelSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class AutoModelProto {
    public void fill(AutoModelSpec spec, TramoSpec.AutoModelSpec.Builder builder) {
        builder.setEnabled(spec.isEnabled())
                .setEnabled(spec.isEnabled())
                .setCancel(spec.getCancel())
                .setUb1(spec.getUb1())
                .setUb2(spec.getUb2())
                .setPcr(spec.getPcr())
                .setPc(spec.getPc())
                .setTsig(spec.getTsig())
                .setAcceptDef(spec.isAcceptDefault())
                .setAmiCompare(spec.isAmiCompare());
        
    }

    public TramoSpec.AutoModelSpec convert(AutoModelSpec spec) {
        TramoSpec.AutoModelSpec.Builder builder = TramoSpec.AutoModelSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public AutoModelSpec convert(TramoSpec.AutoModelSpec spec) {
        return AutoModelSpec.builder()
                .enabled(spec.getEnabled())
                .cancel(spec.getCancel())
                .ub1(spec.getUb1())
                .ub2(spec.getUb2())
                .pcr(spec.getPcr())
                .pc(spec.getPc())
                .tsig(spec.getTsig())
                .acceptDefault(spec.getAcceptDef())
                .amiCompare(spec.getAmiCompare())
                .build();

    }

}
