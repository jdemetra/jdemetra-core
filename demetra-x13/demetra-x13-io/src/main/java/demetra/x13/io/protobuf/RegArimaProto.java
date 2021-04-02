/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.x13.io.protobuf;

import demetra.regarima.RegArimaOutput;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.io.protobuf.RegArimaEstimationProto;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaProto {

    public X13Protos.RegArimaSpec convert(RegArimaSpec spec) {
        return X13Protos.RegArimaSpec.newBuilder()
                .setBasic(BasicProto.convert(spec.getBasic()))
                .setTransform(TransformProto.convert(spec.getTransform()))
                .setOutlier(OutlierProto.convert(spec.getOutliers()))
                .setArima(RegArimaProtosUtility.convert(spec.getArima()))
                .setAutomodel(AutoModelProto.convert(spec.getAutoModel()))
                .setRegression(RegressionProto.convert(spec.getRegression()))
                .setEstimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }

    public RegArimaSpec convert(X13Protos.RegArimaSpec spec) {
        return RegArimaSpec.builder()
                .basic(BasicProto.convert(spec.getBasic()))
                .transform(TransformProto.convert(spec.getTransform()))
                .outliers(OutlierProto.convert(spec.getOutlier()))
                .arima(RegArimaProtosUtility.convert(spec.getArima()))
                .autoModel(AutoModelProto.convert(spec.getAutomodel()))
                .regression(RegressionProto.convert(spec.getRegression(), spec.getOutlier().getMonthlyTcRate()))
                .estimate(EstimateProto.convert(spec.getEstimate()))
                .build();
    }
    
        public X13ResultsProtos.RegArimaOutput convert(RegArimaOutput output){
        X13ResultsProtos.RegArimaOutput.Builder builder = 
                X13ResultsProtos.RegArimaOutput.newBuilder()
                .setEstimationSpec(convert(output.getEstimationSpec()));
        
        if (output.getResult() != null){
            builder.setResult(RegArimaEstimationProto.convert(output.getResult()))
                    .setResultSpec(convert(output.getResultSpec()));
        }
        // TODO detail and logs
        
        return builder.build();
    }
    
}
