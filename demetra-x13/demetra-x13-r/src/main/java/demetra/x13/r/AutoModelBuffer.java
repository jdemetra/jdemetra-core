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
package demetra.x13.r;

import demetra.regarima.AutoModelSpec;
import demetra.util.r.Buffer;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class AutoModelBuffer extends Buffer<AutoModelSpec> {

    public static final int ENABLED = 0, LJUNGBOX = ENABLED + 1, TSIG = LJUNGBOX + 1, PREDCV = TSIG + 1,
            UBFINAL = PREDCV + 1, UB1 = UBFINAL + 1, UB2 = UB1 + 1,
            CANCEL = UB2 + 1, FCT = CANCEL + 1, ACCEPTDEF = FCT + 1,
            CHECKMU = ACCEPTDEF + 1, MIXED = CHECKMU + 1,
            BALANCED = MIXED + 1, HR = BALANCED + 1, SIZE = HR;

    public AutoModelBuffer(double[] data) {
        super(data);
    }

    public static AutoModelBuffer of(AutoModelSpec spec) {
        double[] input = new double[SIZE];
        input[ENABLED] = spec.isEnabled() ? 1 : 0;
        input[LJUNGBOX] = spec.getLjungBoxLimit();
        input[TSIG] = spec.getArmaSignificance();
        input[PREDCV] = spec.getPredcv();
        input[UBFINAL] = spec.getUbfinal();
        input[UB1] = spec.getUb1();
        input[UB2] = spec.getUb2();
        input[CANCEL] = spec.getCancel();
        input[FCT] = spec.getPercentRSE();
        input[ACCEPTDEF] = spec.isAcceptDefault() ? 1 : 0;
        input[CHECKMU] = spec.isCheckMu() ? 1 : 0;
        input[MIXED] = spec.isMixed() ? 1 : 0;
        input[BALANCED] = spec.isBalanced() ? 1 : 0;

        return new AutoModelBuffer(input);
    }

    @Override
    public AutoModelSpec build() {
        return AutoModelSpec.builder()
                .enabled(buffer[ENABLED] != 0)
                .ljungBoxLimit(buffer[LJUNGBOX])
                .armaSignificance(buffer[TSIG])
                .predcv(buffer[PREDCV])
                .ubfinal(buffer[UBFINAL])
                .ub1(buffer[UB1])
                .ub2(buffer[UB2])
                .cancel(buffer[CANCEL])
                .percentRSE(buffer[FCT])
                .acceptDefault(buffer[ACCEPTDEF] != 0)
                .checkMu(buffer[CHECKMU] != 0)
                .mixed(buffer[MIXED] != 0)
                .balanced(buffer[BALANCED] != 0)
                .build();
    }

}
