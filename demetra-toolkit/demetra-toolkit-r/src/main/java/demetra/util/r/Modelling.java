/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.util.r;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.timeseries.regression.ModellingContext;
import demetra.toolkit.io.protobuf.ModellingContextProto;
import demetra.toolkit.io.protobuf.ToolkitProtos;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Modelling {
        public byte[] toBuffer(ModellingContext cntx) {
        return ModellingContextProto.convert(cntx).toByteArray();
    }

    public ModellingContext of(byte[] buffer) {
       try {
            ToolkitProtos.ModellingContext spec = ToolkitProtos.ModellingContext.parseFrom(buffer);
            return ModellingContextProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

}
