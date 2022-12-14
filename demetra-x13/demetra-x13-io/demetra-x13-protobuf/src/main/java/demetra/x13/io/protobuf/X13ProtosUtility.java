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

import demetra.regarima.RegressionTestSpec;
import demetra.regarima.EasterSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.x11.CalendarSigmaOption;
import demetra.x11.SeasonalFilterOption;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13ProtosUtility {

    public DecompositionMode convert(demetra.sa.DecompositionMode mode) {
        switch (mode) {
            case Additive:
                return DecompositionMode.MODE_ADDITIVE;
            case Multiplicative:
                return DecompositionMode.MODE_MULTIPLICATIVE;
            case LogAdditive:
                return DecompositionMode.MODE_LOGADDITIVE;
            case PseudoAdditive:
                return DecompositionMode.MODE_PSEUDOADDITIVE;
            default:
                return DecompositionMode.MODE_UNKNOWN;
        }
    }

    public demetra.sa.DecompositionMode convert(DecompositionMode mode) {
        switch (mode) {
            case MODE_ADDITIVE:
                return demetra.sa.DecompositionMode.Additive;
            case MODE_MULTIPLICATIVE:
                return demetra.sa.DecompositionMode.Multiplicative;
            case MODE_LOGADDITIVE:
                return demetra.sa.DecompositionMode.LogAdditive;
            case MODE_PSEUDOADDITIVE:
                return demetra.sa.DecompositionMode.PseudoAdditive;
            default:
                return demetra.sa.DecompositionMode.Undefined;
        }
    }

    public SeasonalFilter convert(SeasonalFilterOption sf) {
        switch (sf) {
            case Stable:
                return SeasonalFilter.SEASONAL_FILTER_STABLE;
            case X11Default:
                return SeasonalFilter.SEASONAL_FILTER_X11DEFAULT;
            case S3X1:
                return SeasonalFilter.SEASONAL_FILTER_S3X1;
            case S3X3:
                return SeasonalFilter.SEASONAL_FILTER_S3X3;
            case S3X5:
                return SeasonalFilter.SEASONAL_FILTER_S3X5;
            case S3X9:
                return SeasonalFilter.SEASONAL_FILTER_S3X9;
            case S3X15:
                return SeasonalFilter.SEASONAL_FILTER_S3X15;
            default:
                return SeasonalFilter.SEASONAL_FILTER_MSR;
        }
    }

    public SeasonalFilterOption convert(SeasonalFilter sf) {
        switch (sf) {
            case SEASONAL_FILTER_STABLE:
                return SeasonalFilterOption.Stable;
            case SEASONAL_FILTER_S3X1:
                return SeasonalFilterOption.S3X1;
            case SEASONAL_FILTER_S3X3:
                return SeasonalFilterOption.S3X3;
            case SEASONAL_FILTER_S3X5:
                return SeasonalFilterOption.S3X5;
            case SEASONAL_FILTER_S3X9:
                return SeasonalFilterOption.S3X9;
            case SEASONAL_FILTER_S3X15:
                return SeasonalFilterOption.S3X15;
            case SEASONAL_FILTER_X11DEFAULT:
                return SeasonalFilterOption.X11Default;
            default:
                return SeasonalFilterOption.Msr;
        }
    }

    public demetra.x11.BiasCorrection convert(BiasCorrection bias) {
        switch (bias) {
            case BIAS_LEGACY:
                return demetra.x11.BiasCorrection.Legacy;
            case BIAS_RATIO:
                return demetra.x11.BiasCorrection.Ratio;
            case BIAS_SMOOTH:
                return demetra.x11.BiasCorrection.Smooth;
            default:
                return demetra.x11.BiasCorrection.None;
        }
    }

    public BiasCorrection convert(demetra.x11.BiasCorrection bias) {
        switch (bias) {
            case Legacy:
                return BiasCorrection.BIAS_LEGACY;
            case Ratio:
                return BiasCorrection.BIAS_RATIO;
            case Smooth:
                return BiasCorrection.BIAS_SMOOTH;
            default:
                return BiasCorrection.BIAS_NONE;
        }
    }

    public CalendarSigma convert(CalendarSigmaOption sig) {
        switch (sig) {
            case All:
                return CalendarSigma.SIGMA_ALL;
            case Signif:
                return CalendarSigma.SIGMA_SIGNIF;
            case Select:
                return CalendarSigma.SIGMA_SELECT;
            default:
                return CalendarSigma.SIGMA_NONE;
        }
    }

    public CalendarSigmaOption convert(CalendarSigma sig) {
        switch (sig) {
            case SIGMA_ALL:
                return CalendarSigmaOption.All;
            case SIGMA_SIGNIF:
                return CalendarSigmaOption.Signif;
            case SIGMA_SELECT:
                return CalendarSigmaOption.Select;
            default:
                return CalendarSigmaOption.None;
        }
    }

    public RegressionTest convert(RegressionTestSpec test) {
        switch (test) {
            case Add:
                return RegressionTest.TEST_ADD;
            case Remove:
                return RegressionTest.TEST_REMOVE;
            default:
                return RegressionTest.TEST_NO;
        }
    }

    public RegressionTestSpec convert(RegressionTest test) {
        switch (test) {
            case TEST_ADD:
                return RegressionTestSpec.Add;
            case TEST_REMOVE:
                return RegressionTestSpec.Remove;
            default:
                return RegressionTestSpec.None;
        }
    }

    public EasterSpec.Type convert(EasterType type) {
        switch (type) {
            case EASTER_STANDARD:
                return EasterSpec.Type.Easter;
            case EASTER_JULIAN:
                return EasterSpec.Type.JulianEaster;
            case EASTER_SC:
                return EasterSpec.Type.SCEaster;
            default:
                return EasterSpec.Type.Unused;
        }
    }

    public EasterType convert(EasterSpec.Type type) {
        switch (type) {
            case Easter:
                return EasterType.EASTER_STANDARD;
            case JulianEaster:
                return EasterType.EASTER_JULIAN;
            case SCEaster:
                return EasterType.EASTER_SC;
            default:
                return EasterType.EASTER_UNUSED;
        }
    }

    public AutomaticTradingDays convert(TradingDaysSpec.AutoMethod auto) {
        switch (auto) {
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

    public TradingDaysSpec.AutoMethod convert(AutomaticTradingDays auto) {
        return switch (auto) {
            case TD_AUTO_WALD ->
                TradingDaysSpec.AutoMethod.WALD;
            case TD_AUTO_BIC ->
                TradingDaysSpec.AutoMethod.BIC;
            case TD_AUTO_AIC ->
                TradingDaysSpec.AutoMethod.AIC;
            default ->
                TradingDaysSpec.AutoMethod.UNUSED;
        };
    }

    public X13Output convert(jdplus.x13.X13Output output) {
        X13Output.Builder builder
                = X13Output.newBuilder()
                        .setEstimationSpec(SpecProto.convert(output.getEstimationSpec()));
        jdplus.x13.X13Results result = output.getResult();
        if (result != null) {
            builder.setResult(X13ResultsProto.convert(result))
                    .setResultSpec(SpecProto.convert(output.getResultSpec()));
        }
        return builder.build();
    }

}
