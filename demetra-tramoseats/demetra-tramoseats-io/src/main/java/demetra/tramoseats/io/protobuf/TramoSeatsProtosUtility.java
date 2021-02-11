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

    public EasterSpec.Type convert(TramoSeatsProtos.EasterType type) {
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

    public TramoSeatsProtos.EasterType convert(EasterSpec.Type type) {
        switch (type) {
            case Standard:
                return TramoSeatsProtos.EasterType.EASTER_STANDARD;
            case IncludeEaster:
                return TramoSeatsProtos.EasterType.EASTER_INCLUDEEASTER;
            case IncludeEasterMonday:
                return TramoSeatsProtos.EasterType.EASTER_INCLUDEEASTERMONDAY;
            default:
                return TramoSeatsProtos.EasterType.EASTER_UNUSED;
        }
    }

    public TramoSeatsProtos.TradingDaysTest convert(RegressionTestType test) {
        switch (test) {
            case Joint_F:
                return TramoSeatsProtos.TradingDaysTest.TD_TEST_JOINT_F;
            case Separate_T:
                return TramoSeatsProtos.TradingDaysTest.TD_TEST_SEPARATE_T;
            default:
                return TramoSeatsProtos.TradingDaysTest.TD_TEST_NO;
        }
    }

    public RegressionTestType convert(TramoSeatsProtos.TradingDaysTest test) {
        switch (test) {
            case TD_TEST_JOINT_F:
                return RegressionTestType.Joint_F;
            case TD_TEST_SEPARATE_T:
                return RegressionTestType.Separate_T;
            default:
                return RegressionTestType.None;
        }
    }

    public TramoSeatsProtos.AutomaticTradingDays convert(TradingDaysSpec.AutoMethod auto) {
        switch (auto) {
            case FTest:
                return TramoSeatsProtos.AutomaticTradingDays.TD_AUTO_FTEST;
            case WaldTest:
                return TramoSeatsProtos.AutomaticTradingDays.TD_AUTO_WALDTEST;
            default:
                return TramoSeatsProtos.AutomaticTradingDays.TD_AUTO_NO;
        }
    }

    public TradingDaysSpec.AutoMethod convert(TramoSeatsProtos.AutomaticTradingDays  auto) {
        switch (auto) {
            case TD_AUTO_FTEST:
                return TradingDaysSpec.AutoMethod.FTest;
            case TD_AUTO_WALDTEST:
                return TradingDaysSpec.AutoMethod.WaldTest;
            default:
                return TradingDaysSpec.AutoMethod.Unused;
        }
    }
}
