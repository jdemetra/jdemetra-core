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

import demetra.sa.DecompositionMode;
import demetra.util.r.Buffer;
import demetra.x11.BiasCorrection;
import demetra.x11.CalendarSigmaOption;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.SigmaVecOption;
import demetra.x11.X11Spec;

/**
 *
 * @author PALATEJ
 */
public class X11Buffer extends Buffer<X11Spec>{

    private static final int MAXFREQ = 12, MODE = 0, SEASONAL = MODE + 1, FILTER = SEASONAL + 1,
            LSIG = FILTER + MAXFREQ, USIG = LSIG + 1, HEND = USIG + 1, NFCASTS = HEND + 1, NBCASTS = NFCASTS + 1,
            CSIG = NBCASTS + 1, SIGV = CSIG + 1, EXFCASTS = SIGV + MAXFREQ, BIAS = EXFCASTS + 1, SIZE = BIAS + 1;

    public static X11Buffer of(X11Spec spec) {
        double[] input = new double[SIZE];
        // fill the buffer
        input[MODE] = decompositionMode(spec.getMode());
        input[SEASONAL] = spec.isSeasonal() ? 1 : 0;
        int i = 0;
        for (SeasonalFilterOption filter : spec.getFilters()) {
            input[FILTER + (i++)] = filter(filter);

        }
        input[LSIG] = spec.getLowerSigma();
        input[USIG] = spec.getUpperSigma();
        input[HEND] = spec.getHendersonFilterLength();
        input[NFCASTS] = spec.getForecastHorizon();
        input[NBCASTS] = spec.getBackcastHorizon();
        input[CSIG] = calendarSigma(spec.getCalendarSigma());
        SigmaVecOption[] sv = spec.getSigmaVec();
        i = 0;
        if (sv != null) {
            for (SigmaVecOption s : sv) {
                input[SIGV + (i++)] = vsigma(s);
            }
        }
        input[EXFCASTS] = spec.isExcludeForecast() ? 1 : 0;
        input[BIAS] = bias(spec.getBias());
        
        return new X11Buffer(input);
    }

    public X11Buffer(double[] data) {
        super(data);
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
                return 1;
            case Stable:
                return 2;
            case X11Default:
                return 3;
            case S3X1:
                return 4;
            case S3X3:
                return 5;
            case S3X5:
                return 6;
            case S3X9:
                return 7;
            case S3X15:
                return 8;
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
            case 1:
                return SeasonalFilterOption.Msr;
            case 2:
                return SeasonalFilterOption.Stable;
            case 3:
                return SeasonalFilterOption.X11Default;
            case 4:
                return SeasonalFilterOption.S3X1;
            case 5:
                return SeasonalFilterOption.S3X3;
            case 6:
                return SeasonalFilterOption.S3X5;
            case 7:
                return SeasonalFilterOption.S3X9;
            case 8:
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

    @Override
    public X11Spec build() {
        int i = 0;
        while (i < MAXFREQ && buffer[FILTER + i] != 0) {
            i++;
        }
        SeasonalFilterOption[] sf = new SeasonalFilterOption[i];
        for (int j = 0; j < sf.length; ++j) {
            int f = (int) buffer[FILTER + j];
            sf[j] = filter(f);
        }

        i = 0;
        while (i < MAXFREQ && buffer[SIGV + i] != 0) {
            i++;
        }
        SigmaVecOption[] sv = null;
        if (i != 0) {
            sv = new SigmaVecOption[i];
            for (int j = 0; j < sf.length; ++j) {
                int v = (int) buffer[SIGV + j];
                sv[j] = vsigma(v);
            }
        }

        X11Spec.Builder builder = X11Spec.builder()
                .mode(decompositionMode())
                .filters(sf)
                .seasonal(seasonal())
                .lowerSigma(buffer[LSIG])
                .upperSigma(buffer[USIG])
                .hendersonFilterLength((int) buffer[HEND])
                .forecastHorizon((int) buffer[NFCASTS])
                .backcastHorizon((int) buffer[NBCASTS])
                .calendarSigma(calendarSigma())
                .sigmaVec(sv)
                .excludeForecast(excludeForecasts())
                .bias(bias());

        return builder.build();
    }
}
