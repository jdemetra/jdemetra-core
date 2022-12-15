/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.OutlierSpec;
import demetra.regarima.SingleOutlierSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class OutlierProto {
    public void fill(OutlierSpec spec, RegArimaSpec.OutlierSpec.Builder builder) {
        builder.setSpan(ToolkitProtosUtility.convert(spec.getSpan()))
                .setDefva(spec.getDefaultCriticalValue())
                .setMonthlyTcRate(spec.getMonthlyTCRate())
                .setMaxiter(spec.getMaxIter())
                .setMethod(spec.getMethod() == OutlierSpec.Method.AddAll ? OutlierMethod.OUTLIER_ADDALL : OutlierMethod.OUTLIER_ADDONE)
                .setLsrun(spec.getLsRun());
        
        List<SingleOutlierSpec> types = spec.getTypes();
        for (SingleOutlierSpec t : types){
            builder.addOutliers(RegArimaSpec.OutlierSpec.Type.newBuilder()
                    .setCode(t.getType())
                    .setVa(t.getCriticalValue())
                    .build());
        }
    }

    public RegArimaSpec.OutlierSpec convert(OutlierSpec spec) {
        RegArimaSpec.OutlierSpec.Builder builder = RegArimaSpec.OutlierSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public OutlierSpec convert(RegArimaSpec.OutlierSpec spec) {
        OutlierSpec.Builder builder = OutlierSpec.builder();
        
        int n = spec.getOutliersCount();
        for (int i=0; i<n; ++i){
            RegArimaSpec.OutlierSpec.Type cur = spec.getOutliers(i);
            builder.type(new SingleOutlierSpec(cur.getCode(), cur.getVa()));
        }
        
        return builder
                .span(ToolkitProtosUtility.convert(spec.getSpan()))
                .defaultCriticalValue(spec.getDefva())
                .monthlyTCRate(spec.getMonthlyTcRate())
                .maxIter(spec.getMaxiter())
                .method(spec.getMethod() == OutlierMethod.OUTLIER_ADDALL ? OutlierSpec.Method.AddAll : OutlierSpec.Method.AddOne)
                .lsRun(spec.getLsrun())
                .build();
    }
}
