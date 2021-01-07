/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import demetra.modelling.RegressionTestSpec;
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
                return X13Protos.DecompositionMode.ADDITIVE;
            case Multiplicative:
                return X13Protos.DecompositionMode.MULTIPLICATIVE;
            case LogAdditive:
                return X13Protos.DecompositionMode.LOGADDITIVE;
            case PseudoAdditive:
                return X13Protos.DecompositionMode.PSEUDOADDITIVE;
            default:
                return X13Protos.DecompositionMode.UNKNOWN;
        }
    }

    public DecompositionMode convert(X13Protos.DecompositionMode mode) {
        switch (mode) {
            case ADDITIVE:
                return DecompositionMode.Additive;
            case MULTIPLICATIVE:
                return DecompositionMode.Multiplicative;
            case LOGADDITIVE:
                return DecompositionMode.LogAdditive;
            case PSEUDOADDITIVE:
                return DecompositionMode.PseudoAdditive;
            default:
                return DecompositionMode.Undefined;
        }
    }

    public X13Protos.SeasonalFilter convert(SeasonalFilterOption sf) {
        switch (sf) {
            case Msr:
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_MSR;
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
                return X13Protos.SeasonalFilter.SEASONAL_FILTER_UNSPECIFIED;
        }
    }

    public SeasonalFilterOption convert(X13Protos.SeasonalFilter sf) {
        switch (sf) {
            case SEASONAL_FILTER_MSR:
                return SeasonalFilterOption.Msr;
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
            default:
                return SeasonalFilterOption.X11Default;
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
        switch (test){
            case Add: return X13Protos.RegressionTest.TEST_ADD;
            case Remove: return X13Protos.RegressionTest.TEST_REMOVE;
            default: return X13Protos.RegressionTest.TEST_NO;
        }
    }
    
    public  RegressionTestSpec convert(X13Protos.RegressionTest test) {
        switch (test){
            case TEST_ADD: return RegressionTestSpec.Add;
            case TEST_REMOVE: return RegressionTestSpec.Remove;
            default: return RegressionTestSpec.None;
        }
    }
    
}
