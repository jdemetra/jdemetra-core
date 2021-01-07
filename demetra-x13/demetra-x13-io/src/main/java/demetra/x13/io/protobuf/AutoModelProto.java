/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.AutoModelSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class AutoModelProto {
    public void fill(AutoModelSpec spec, X13Protos.RegArimaSpec.AutoModelSpec.Builder builder) {
        builder.setEnabled(spec.isEnabled())
                .setBalanced(spec.isBalanced())
                .setCheckmu(spec.isCheckMu())
                .setAcceptdef(spec.isAcceptDefault())
                .setMixed(spec.isMixed())
                .setCancel(spec.getCancel())
                .setUb1(spec.getUb1())
                .setUb2(spec.getUb2())
                .setUbfinal(spec.getUbfinal())
                .setTsig(spec.getArmaSignificance())
                .setLjungbox(spec.getLjungBoxLimit())
                .setPredcv(spec.getPredcv())
                .setFct(spec.getPercentRSE());
        
    }

    public X13Protos.RegArimaSpec.AutoModelSpec convert(AutoModelSpec spec) {
        X13Protos.RegArimaSpec.AutoModelSpec.Builder builder = X13Protos.RegArimaSpec.AutoModelSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public byte[] toBuffer(AutoModelSpec spec) {
        return convert(spec).toByteArray();
    }

    public AutoModelSpec convert(X13Protos.RegArimaSpec.AutoModelSpec spec) {
        return AutoModelSpec.builder()
                .enabled(spec.getEnabled())
                .checkMu(spec.getCheckmu())
                .acceptDefault(spec.getAcceptdef())
                .balanced(spec.getBalanced())
                .mixed(spec.getMixed())
                .cancel(spec.getCancel())
                .ub1(spec.getUb1())
                .ub2(spec.getUb2())
                .ubfinal(spec.getUbfinal())
                .ljungBoxLimit(spec.getLjungbox())
                .armaSignificance(spec.getTsig())
                .predcv(spec.getPredcv())
                .percentRSE(spec.getFct())
                .build();

    }

    public AutoModelSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        X13Protos.RegArimaSpec.AutoModelSpec spec = X13Protos.RegArimaSpec.AutoModelSpec.parseFrom(bytes);
        return convert(spec);
    }
    
}
