/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.tramo.OutlierSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class OutlierProto {

    public void fill(OutlierSpec spec, TramoSpec.OutlierSpec.Builder builder) {
        builder.setSpan(ToolkitProtosUtility.convert(spec.getSpan()))
                .setEnabled(spec.isUsed())
                .setVa(spec.getCriticalValue())
                .setTcrate(spec.getDeltaTC())
                .setAo(spec.isAo())
                .setLs(spec.isLs())
                .setTc(spec.isTc())
                .setSo(spec.isSo())
                .setMl(spec.isMaximumLikelihood());
    }

    public TramoSpec.OutlierSpec convert(OutlierSpec spec) {
        TramoSpec.OutlierSpec.Builder builder = TramoSpec.OutlierSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public OutlierSpec convert(TramoSpec.OutlierSpec spec) {
        if (!spec.getEnabled()) {
            return OutlierSpec.DEFAULT_DISABLED;
        }
        return OutlierSpec.builder()
                .span(ToolkitProtosUtility.convert(spec.getSpan()))
                .criticalValue(spec.getVa())
                .ao(spec.getAo())
                .ls(spec.getLs())
                .tc(spec.getTc())
                .so(spec.getSo())
                .deltaTC(spec.getTcrate())
                .maximumLikelihood(spec.getMl())
                .build();
    }

}
