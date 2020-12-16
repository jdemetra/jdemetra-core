/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.sa.DecompositionMode;
import demetra.x11.BiasCorrection;
import demetra.x11.CalendarSigmaOption;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.SigmaVecOption;
import demetra.x11.X11Spec;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class X11Buffer {

    private static final int MAXFREQ = 12, MODE = 0, SEASONAL = MODE + 1, FILTER = SEASONAL + 1,
            LSIG = FILTER + MAXFREQ, USIG = LSIG + 1, HEND = USIG + 1, NFCASTS = HEND + 1, NBCASTS = NFCASTS + 1,
            CSIG = NBCASTS + 1, SIGV = CSIG + 1, EXFCASTS = SIGV + MAXFREQ, BIAS = EXFCASTS + 1, SIZE = BIAS + 1;

    private final double[] buffer;

    public X11Buffer(X11Spec spec) {
        buffer = new double[SIZE];
        // fill the buffer
        buffer[MODE] = decompositionMode(spec.getMode());
        buffer[SEASONAL] = spec.isSeasonal() ? 1 : 0;
        int i = 0;
        for (SeasonalFilterOption filter : spec.getFilters()) {
            buffer[FILTER + (i++)] = filter(filter);

        }
        buffer[LSIG] = spec.getLowerSigma();
        buffer[USIG] = spec.getUpperSigma();
        buffer[HEND] = spec.getHendersonFilterLength();
        buffer[NFCASTS] = spec.getForecastHorizon();
        buffer[NBCASTS] = spec.getBackcastHorizon();
        buffer[CSIG] = calendarSigma(spec.getCalendarSigma());
        List<SigmaVecOption> sv = spec.getSigmaVec();
        i = 0;
        for (SigmaVecOption s : sv) {
            buffer[SIGV + (i++)] = vsigma(s);
        }
        buffer[EXFCASTS] = spec.isExcludeForecast() ? 1 : 0;
        buffer[BIAS] = bias(spec.getBias());
    }

    public X11Buffer(double[] data) {
        buffer = new double[SIZE];
        System.arraycopy(data, 0, buffer, 0, data.length);
    }

    public double[] data() {
        return buffer;
    }

    public DecompositionMode decompositionMode() { // ordinal
        int mod = (int) buffer[MODE];
        switch (mod) {
            case 1:
                return DecompositionMode.Additive;
            case 2:
                return DecompositionMode.Multiplicative;
            case 3:
                return DecompositionMode.LogAdditive;
            case 4:
                return DecompositionMode.PseudoAdditive;
            default:
                return DecompositionMode.Undefined;
        }
    }

    public static int decompositionMode(DecompositionMode mode) {
        switch (mode) {
            case Additive:
                return 1;
            case Multiplicative:
                return 2;
            case LogAdditive:
                return 3;
            case PseudoAdditive:
                return 4;
            default:
                return 0;
        }
    }

    public static int filter(SeasonalFilterOption option) {
        switch (option) {
            case Msr:
                return -1;
            case Stable:
                return -2;
            case X11Default:
                return -3;
            case S3X1:
                return 1;
            case S3X3:
                return 2;
            case S3X5:
                return 3;
            case S3X9:
                return 4;
            case S3X15:
                return 5;
            default:
                return 0;
        }
    }

    public static int calendarSigma(CalendarSigmaOption option) {
        switch (option) {
            case Signif:
                return 1;
            case All:
                return 2;
            case Select:
                return 3;
            default:
                return 0;
        }
    }

    public CalendarSigmaOption calendarSigma() {
        int option = (int) buffer[CSIG];
        switch (option) {
            case 1:
                return CalendarSigmaOption.Signif;
            case 2:
                return CalendarSigmaOption.All;
            case 3:
                return CalendarSigmaOption.Select;
            default:
                return CalendarSigmaOption.None;
        }
    }

    public static SeasonalFilterOption filter(int option) {
        switch (option) {
            case -1:
                return SeasonalFilterOption.Msr;
            case -2:
                return SeasonalFilterOption.Stable;
            case -3:
                return SeasonalFilterOption.X11Default;
            case 1:
                return SeasonalFilterOption.S3X1;
            case 2:
                return SeasonalFilterOption.S3X3;
            case 3:
                return SeasonalFilterOption.S3X5;
            case 4:
                return SeasonalFilterOption.S3X9;
            case 5:
                return SeasonalFilterOption.S3X15;
            default:
                return null;
        }
    }

    public static int vsigma(SigmaVecOption option) {
        switch (option) {
            case Group1:
                return 1;
            default:
                return 2;
        }
    }

    public static SigmaVecOption vsigma(int option) {
        switch (option) {
            case 1:
                return SigmaVecOption.Group1;
            default:
                return SigmaVecOption.Group2;
        }
    }

    public static int bias(BiasCorrection bias) {
        switch (bias) {
            case Legacy:
                return 1;
            case Smooth:
                return 2;
            case Ratio:
                return 3;
            default:
                return 0;
        }
    }

    public BiasCorrection bias() {
        int bias = (int) buffer[BIAS];
        switch (bias) {
            case 1:
                return BiasCorrection.Legacy;
            case 2:
                return BiasCorrection.Smooth;
            case 3:
                return BiasCorrection.Ratio;
            default:
                return BiasCorrection.None;
        }
    }

    public boolean seasonal() {
        return buffer[SEASONAL] != 0;
    }

    public boolean excludeForecasts() {
        return buffer[EXFCASTS] != 0;
    }

    public X11Spec build() {
        X11Spec.Builder builder = X11Spec.builder()
                .mode(decompositionMode())
                .seasonal(seasonal())
                .clearFilters()
                .clearSigmaVec()
                .lowerSigma(buffer[LSIG])
                .upperSigma(buffer[USIG])
                .hendersonFilterLength((int) buffer[HEND])
                .forecastHorizon((int) buffer[NFCASTS])
                .backcastHorizon((int) buffer[NBCASTS])
                .calendarSigma(calendarSigma())
                .excludeForecast(excludeForecasts())
                .bias(bias());

        for (int i = 0; i < MAXFREQ; ++i) {
            int f = (int) buffer[FILTER + i];
            if (f == 0) {
                break;
            }
            builder.filter(filter(f));
        }

        for (int i = 0; i < MAXFREQ; ++i) {
            int v = (int) buffer[SIGV + i];
            if (v == 0) {
                break;
            }
            builder.calendarSigma(vsigma(v));
        }

        return builder.build();
    }
}
