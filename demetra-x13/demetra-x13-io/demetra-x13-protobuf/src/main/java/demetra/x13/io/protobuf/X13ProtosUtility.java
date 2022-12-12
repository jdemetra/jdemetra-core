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
import demetra.sa.DecompositionMode;
import demetra.x11.BiasCorrection;
import demetra.x11.CalendarSigmaOption;
import demetra.x11.SeasonalFilterOption;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13ProtosUtility {

    public X13Protos.DecompositionMode convert(DecompositionMode mode) {
        switch (mode) {
            case Additive:
                return X13Protos.DecompositionMode.MODE_ADDITIVE;
            case Multiplicative:
                return X13Protos.DecompositionMode.MODE_MULTIPLICATIVE;
            case LogAdditive:
                return X13Protos.DecompositionMode.MODE_LOGADDITIVE;
            case PseudoAdditive:
                return X13Protos.DecompositionMode.MODE_PSEUDOADDITIVE;
            default:
                return X13Protos.DecompositionMode.MODE_UNKNOWN;
        }
    }

    public DecompositionMode convert(X13Protos.DecompositionMode mode) {
        switch (mode) {
            case MODE_ADDITIVE:
                return DecompositionMode.Additive;
            case MODE_MULTIPLICATIVE:
                return DecompositionMode.Multiplicative;
            case MODE_LOGADDITIVE:
                return DecompositionMode.LogAdditive;
            case MODE_PSEUDOADDITIVE:
                return DecompositionMode.PseudoAdditive;
            default:
                return DecompositionMode.Undefined;
        }
    }

    public X13Protos.SeasonalFilter convert(SeasonalFilterOption sf) {
        switch (sf) {
            case Stable:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_STABLE;
            case X11Default:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_X11DEFAULT;
            case S3X1:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_S3X1;
            case S3X3:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_S3X3;
            case S3X5:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_S3X5;
            case S3X9:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_S3X9;
            case S3X15:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_S3X15;
            default:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_MSR;
        }
    }

    public SeasonalFilterOption convert(X13Protos.SeasonalFilter sf) {
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

    public BiasCorrection convert(X13Protos.BiasCorrection bias) {
        switch (bias) {
            case BIAS_LEGACY:
                return BiasCorrection.Legacy;
            case BIAS_RATIO:
                return BiasCorrection.Ratio;
            case BIAS_SMOOTH:
                return BiasCorrection.Smooth;
            default:
                return BiasCorrection.None;
        }
    }

    public X13Protos.BiasCorrection convert(BiasCorrection bias) {
        switch (bias) {
            case Legacy:
                return X13Protos.BiasCorrection.BIAS_LEGACY;
            case Ratio:
                return X13Protos.BiasCorrection.BIAS_RATIO;
            case Smooth:
                return X13Protos.BiasCorrection.BIAS_SMOOTH;
            default:
                return X13Protos.BiasCorrection.BIAS_NONE;
        }
    }

    public X13Protos.CalendarSigma convert(CalendarSigmaOption sig) {
        switch (sig) {
            case All:
                return X13Protos.CalendarSigma.SIGMA_ALL;
            case Signif:
                return X13Protos.CalendarSigma.SIGMA_SIGNIF;
            case Select:
                return X13Protos.CalendarSigma.SIGMA_SELECT;
            default:
                return X13Protos.CalendarSigma.SIGMA_NONE;
        }
    }

    public CalendarSigmaOption convert(X13Protos.CalendarSigma sig) {
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

    public X13Protos.RegressionTest convert(RegressionTestSpec test) {
        switch (test) {
            case Add:
                return X13Protos.RegressionTest.TEST_ADD;
            case Remove:
                return X13Protos.RegressionTest.TEST_REMOVE;
            default:
                return X13Protos.RegressionTest.TEST_NO;
        }
    }

    public RegressionTestSpec convert(X13Protos.RegressionTest test) {
        switch (test) {
            case TEST_ADD:
                return RegressionTestSpec.Add;
            case TEST_REMOVE:
                return RegressionTestSpec.Remove;
            default:
                return RegressionTestSpec.None;
        }
    }

    public EasterSpec.Type convert(X13Protos.EasterType type) {
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

    public X13Protos.EasterType convert(EasterSpec.Type type) {
        switch (type) {
            case Easter:
                return X13Protos.EasterType.EASTER_STANDARD;
            case JulianEaster:
                return X13Protos.EasterType.EASTER_JULIAN;
            case SCEaster:
                return X13Protos.EasterType.EASTER_SC;
            default:
                return X13Protos.EasterType.EASTER_UNUSED;
        }
    }
    
        public X13Protos.AutomaticTradingDays convert(TradingDaysSpec.AutoMethod auto) {
        switch (auto) {
             case WALD:
                return X13Protos.AutomaticTradingDays.TD_AUTO_WALD;
            case BIC:
                return X13Protos.AutomaticTradingDays.TD_AUTO_BIC;
            case AIC:
                return X13Protos.AutomaticTradingDays.TD_AUTO_AIC;
            default:
                return X13Protos.AutomaticTradingDays.TD_AUTO_NO;
        }
    }

    public TradingDaysSpec.AutoMethod convert(X13Protos.AutomaticTradingDays  auto) {
        return switch (auto) {
             case TD_AUTO_WALD -> TradingDaysSpec.AutoMethod.WALD;
            case TD_AUTO_BIC -> TradingDaysSpec.AutoMethod.BIC;
            case TD_AUTO_AIC -> TradingDaysSpec.AutoMethod.AIC;
            default -> TradingDaysSpec.AutoMethod.UNUSED;
        };
    }

}
