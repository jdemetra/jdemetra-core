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

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.AutoModelSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class AutoModelProto {
    public void fill(AutoModelSpec spec, RegArimaSpec.AutoModelSpec.Builder builder) {
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

    public RegArimaSpec.AutoModelSpec convert(AutoModelSpec spec) {
        RegArimaSpec.AutoModelSpec.Builder builder = RegArimaSpec.AutoModelSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public AutoModelSpec convert(RegArimaSpec.AutoModelSpec spec) {
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

}
