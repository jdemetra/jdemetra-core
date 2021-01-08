/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.regarima.io.protobuf;

import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaResultsProto {
    
    public RegArimaResultsProtos.RegArimaEstimation convert(ModelEstimation model){
        RegArimaResultsProtos.RegArimaEstimation.Builder builder = RegArimaResultsProtos.RegArimaEstimation.newBuilder();
        builder.setTransformation(model.isLogTransformation() ? RegArimaProtos.Transformation.FN_LOG : RegArimaProtos.Transformation.FN_LEVEL)
                .setCovariance(ToolkitProtosUtility.convert(model.getArimaCovariance()))
                .setLikelihood(ToolkitProtosUtility.convert(model.getStatistics()));
        
        return builder.build();
    }
    
}
