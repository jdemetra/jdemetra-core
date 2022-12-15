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
package demetra.tramoseats.io.protobuf;

import demetra.tramo.EasterSpec;
import demetra.tramo.RegressionTestType;
import demetra.tramo.TradingDaysSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsProtosUtility {

    public EasterSpec.Type convert(EasterType type) {
        switch (type) {
            case EASTER_STANDARD:
                return EasterSpec.Type.Standard;
            case EASTER_INCLUDEEASTER:
                return EasterSpec.Type.IncludeEaster;
            case EASTER_INCLUDEEASTERMONDAY:
                return EasterSpec.Type.IncludeEasterMonday;
            default:
                return EasterSpec.Type.Unused;
        }
    }

    public EasterType convert(EasterSpec.Type type) {
        switch (type) {
            case Standard:
                return EasterType.EASTER_STANDARD;
            case IncludeEaster:
                return EasterType.EASTER_INCLUDEEASTER;
            case IncludeEasterMonday:
                return EasterType.EASTER_INCLUDEEASTERMONDAY;
            default:
                return EasterType.EASTER_UNUSED;
        }
    }

    public TradingDaysTest convert(RegressionTestType test) {
        switch (test) {
            case Joint_F:
                return TradingDaysTest.TD_TEST_JOINT_F;
            case Separate_T:
                return TradingDaysTest.TD_TEST_SEPARATE_T;
            default:
                return TradingDaysTest.TD_TEST_NO;
        }
    }

    public RegressionTestType convert(TradingDaysTest test) {
        switch (test) {
            case TD_TEST_JOINT_F:
                return RegressionTestType.Joint_F;
            case TD_TEST_SEPARATE_T:
                return RegressionTestType.Separate_T;
            default:
                return RegressionTestType.None;
        }
    }

    public AutomaticTradingDays convert(TradingDaysSpec.AutoMethod auto) {
        switch (auto) {
            case FTEST:
                return AutomaticTradingDays.TD_AUTO_FTEST;
            case WALD:
                return AutomaticTradingDays.TD_AUTO_WALD;
            case BIC:
                return AutomaticTradingDays.TD_AUTO_BIC;
            case AIC:
                return AutomaticTradingDays.TD_AUTO_AIC;
            default:
                return AutomaticTradingDays.TD_AUTO_NO;
        }
    }

    public TradingDaysSpec.AutoMethod convert(AutomaticTradingDays  auto) {
        return switch (auto) {
            case TD_AUTO_FTEST -> TradingDaysSpec.AutoMethod.FTEST;
            case TD_AUTO_WALD -> TradingDaysSpec.AutoMethod.WALD;
            case TD_AUTO_BIC -> TradingDaysSpec.AutoMethod.BIC;
            case TD_AUTO_AIC -> TradingDaysSpec.AutoMethod.AIC;
            default -> TradingDaysSpec.AutoMethod.UNUSED;
        };
    }
    
        public TramoSeatsOutput convert(jdplus.tramoseats.TramoSeatsOutput output) {
        TramoSeatsOutput.Builder builder
                = TramoSeatsOutput.newBuilder()
                        .setEstimationSpec(SpecProto.convert(output.getEstimationSpec()));
        jdplus.tramoseats.TramoSeatsResults result = output.getResult();
        if (result != null) {
            builder.setResult(TramoSeatsResultsProto.convert(result))
                    .setResultSpec(SpecProto.convert(output.getResultSpec()));
        }
        return builder.build();
    }

}
