/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.sa.DecompositionMode;
import demetra.x11.BiasCorrection;
import demetra.x11.CalendarSigmaOption;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.SigmaVecOption;
import demetra.x11.X11Spec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13ProtosUtility {

    public X13Protos.X11Spec.DecompositionMode convert(DecompositionMode mode) {
        switch (mode) {
            case Additive:
                return X13Protos.X11Spec.DecompositionMode.ADDITIVE;
            case Multiplicative:
                return X13Protos.X11Spec.DecompositionMode.MULTIPLICATIVE;
            case LogAdditive:
                return X13Protos.X11Spec.DecompositionMode.LOGADDITIVE;
            case PseudoAdditive:
                return X13Protos.X11Spec.DecompositionMode.PSEUDOADDITIVE;
            default:
                return X13Protos.X11Spec.DecompositionMode.UNKNOWN;
        }
    }

    public DecompositionMode convert(X13Protos.X11Spec.DecompositionMode mode) {
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

    public X13Protos.X11Spec.SeasonalFilter convert(SeasonalFilterOption sf) {
        switch (sf) {
            case Msr:
                return X13Protos.X11Spec.SeasonalFilter.MSR;
            case Stable:
                return X13Protos.X11Spec.SeasonalFilter.STABLE;
            case X11Default:
                return X13Protos.X11Spec.SeasonalFilter.X11DEFAULT;
            case S3X1:
                return X13Protos.X11Spec.SeasonalFilter.S3X1;
            case S3X3:
                return X13Protos.X11Spec.SeasonalFilter.S3X3;
            case S3X5:
                return X13Protos.X11Spec.SeasonalFilter.S3X5;
            case S3X9:
                return X13Protos.X11Spec.SeasonalFilter.S3X9;
            case S3X15:
                return X13Protos.X11Spec.SeasonalFilter.S3X15;
            default:
                return X13Protos.X11Spec.SeasonalFilter.UNRECOGNIZED;
        }
    }

    public SeasonalFilterOption convert(X13Protos.X11Spec.SeasonalFilter sf) {
        switch (sf) {
            case MSR:
                return SeasonalFilterOption.Msr;
            case STABLE:
                return SeasonalFilterOption.Stable;
            case S3X1:
                return SeasonalFilterOption.S3X1;
            case S3X3:
                return SeasonalFilterOption.S3X3;
            case S3X5:
                return SeasonalFilterOption.S3X5;
            case S3X9:
                return SeasonalFilterOption.S3X9;
            case S3X15:
                return SeasonalFilterOption.S3X15;
            default:
                return SeasonalFilterOption.X11Default;
        }
    }

    public BiasCorrection convert(X13Protos.X11Spec.BiasCorrection bias) {
        switch (bias) {
            case LEGACY:
                return BiasCorrection.Legacy;
            case RATIO:
                return BiasCorrection.Ratio;
            case SMOOTH:
                return BiasCorrection.Smooth;
            default:
                return BiasCorrection.None;
        }
    }

    public X13Protos.X11Spec.BiasCorrection convert(BiasCorrection bias) {
        switch (bias) {
            case Legacy:
                return X13Protos.X11Spec.BiasCorrection.LEGACY;
            case Ratio:
                return X13Protos.X11Spec.BiasCorrection.RATIO;
            case Smooth:
                return X13Protos.X11Spec.BiasCorrection.SMOOTH;
            default:
                return X13Protos.X11Spec.BiasCorrection.NOCORRECTION;
        }
    }

    public X13Protos.X11Spec.CalendarSigma convert(CalendarSigmaOption sig) {
        switch (sig) {
            case All:
                return X13Protos.X11Spec.CalendarSigma.ALL;
            case Signif:
                return X13Protos.X11Spec.CalendarSigma.SIGNIF;
            case Select:
                return X13Protos.X11Spec.CalendarSigma.SELECT;
            default:
                return X13Protos.X11Spec.CalendarSigma.NONE;
        }
    }

    public CalendarSigmaOption convert(X13Protos.X11Spec.CalendarSigma sig) {
        switch (sig) {
            case ALL:
                return CalendarSigmaOption.All;
            case SIGNIF:
                return CalendarSigmaOption.Signif;
            case SELECT:
                return CalendarSigmaOption.Select;
            default:
                return CalendarSigmaOption.None;
        }
    }

    public byte[] toBuffer(X11Spec spec) {
        X13Protos.X11Spec.Builder builder = X13Protos.X11Spec.newBuilder()
                .setMode(convert(spec.getMode()))
                .setSeasonal(spec.isSeasonal())
                .setLsig(spec.getLowerSigma())
                .setUsig(spec.getUpperSigma())
                .setHenderson(spec.getHendersonFilterLength())
                .setNfcasts(spec.getForecastHorizon())
                .setNbcasts(spec.getForecastHorizon())
                .setSigma(convert(spec.getCalendarSigma()))
                .setExcudefcasts(spec.isExcludeForecast())
                .setBias(convert(spec.getBias()));
        SeasonalFilterOption[] filters = spec.getFilters();
        for (int i = 0; i < filters.length; ++i) {
            builder.addSfilter(convert(filters[i]));
        }
        SigmaVecOption[] vs = spec.getSigmaVec();
        if (vs != null) {
             for (int i = 0; i < vs.length; ++i) {
                builder.addVsigma(vs[i] == SigmaVecOption.Group1 ? 1 : 2);
            }
        }
        return builder.build().toByteArray();
    }

    public X11Spec x11SpecOf(byte[] bytes) throws InvalidProtocolBufferException {
        X13Protos.X11Spec x11 = X13Protos.X11Spec.parseFrom(bytes);
        X11Spec.Builder builder = X11Spec.builder()
                .mode(convert(x11.getMode()))
                .seasonal(x11.getSeasonal())
                .lowerSigma(x11.getLsig())
                .upperSigma(x11.getUsig())
                .hendersonFilterLength(x11.getHenderson())
                .forecastHorizon(x11.getNfcasts())
                .backcastHorizon(x11.getNbcasts())
                .calendarSigma(convert(x11.getSigma()))
                .excludeForecast(x11.getExcudefcasts())
                .bias(convert(x11.getBias()));
        int n = x11.getSfilterCount();
        if (n > 0) {
            SeasonalFilterOption[] sf = new SeasonalFilterOption[n];
            for (int i = 0; i < n; ++i) {
                sf[i] = convert(x11.getSfilter(i));
            }
            builder.filters(sf);
        }
        n = x11.getVsigmaCount();
        if (n > 0) {
            SigmaVecOption[] sv = new SigmaVecOption[n];
            for (int i = 0; i < n; ++i) {
                sv[i] = x11.getVsigma(i) == 1 ? SigmaVecOption.Group1 : SigmaVecOption.Group2;
            }
            builder.sigmaVec(sv);
        }

        return builder.build();
    }

}
