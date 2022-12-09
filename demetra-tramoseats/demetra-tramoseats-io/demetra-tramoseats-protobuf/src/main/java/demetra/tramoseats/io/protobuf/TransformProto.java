/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.modelling.io.protobuf.ModellingProtosUtility;
import demetra.tramo.TransformSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TransformProto {
    
    public void fill(TransformSpec spec, TramoSeatsProtos.TramoSpec.TransformSpec.Builder builder) {
        builder.setTransformation(ModellingProtosUtility.convert(spec.getFunction()))
                .setFct(spec.getFct())
                .setOutliersCorrection(spec.isOutliersCorrection());
        
    }
    // TODO outliers
    public TramoSeatsProtos.TramoSpec.TransformSpec convert(TransformSpec spec) {
        TramoSeatsProtos.TramoSpec.TransformSpec.Builder builder = TramoSeatsProtos.TramoSpec.TransformSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }
    
    public TransformSpec convert(TramoSeatsProtos.TramoSpec.BasicSpec bspec, TramoSeatsProtos.TramoSpec.TransformSpec tspec) {
        return TransformSpec.builder()
                .span(ToolkitProtosUtility.convert(bspec.getSpan()))
                .preliminaryCheck(bspec.getPreliminaryCheck())
                .function(ModellingProtosUtility.convert(tspec.getTransformation()))
                .fct(tspec.getFct())
                .outliersCorrection(tspec.getOutliersCorrection())
                .build();
    }
    
}
