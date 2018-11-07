/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
 */
package ec.tstoolkit.modelling.arima.tramo;

/**
 *
 * @author gianluca
 */
public class SpectralPeaks {

    static public enum AR {

        A, a, none, undef;

        public boolean isPresent() {
            return this == A || this == a;
        }

        public static AR fromInt(int v) {
            switch (v) {
                case 0:
                    return none;
                case 1:
                    return a;
                case 2:
                    return A;
                default:
                    return undef;
            }
        }
    }

    static public enum Tukey {

        T, t, none, undef;

        public boolean isPresent() {
            return this == T || this == t;
        }

        public static Tukey fromInt(int v) {
            switch (v) {
                case 0:
                    return none;
                case 1:
                    return t;
                case 2:
                    return T;
                default:
                    return undef;
            }
        }
    }
    public final AR ar;
    public final Tukey tu;
    public static final SpectralPeaks NONE = new SpectralPeaks(AR.none, Tukey.none);
    public static final SpectralPeaks UNDEF = new SpectralPeaks(AR.undef, Tukey.undef);
    public static final SpectralPeaks ALL = new SpectralPeaks(AR.A, Tukey.T);

    public SpectralPeaks(AR ar, Tukey tu) {
        this.ar = ar;
        this.tu = tu;
    }

    public boolean hasHighPeak() {
        return ar == AR.A || tu == Tukey.T;
    }

    public boolean hasPeak() {
        return ar.isPresent() || tu.isPresent();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.ar != null ? this.ar.hashCode() : 0);
        hash = 73 * hash + (this.tu != null ? this.tu.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SpectralPeaks && equals((SpectralPeaks) obj));
    }

    public boolean equals(SpectralPeaks peaks) {
        return this.ar == peaks.ar && this.tu == peaks.tu;
    }

    private char ar() {
        if (this.ar == AR.A) {
            return 'A';
        } else if (this.ar == AR.a) {
            return 'a';
        } else if (this.ar == AR.none) {
            return '-';
        } else {
            return 'n';
        }
    }

    public char tu() {
        if (this.tu == Tukey.T) {
            return 'T';
        } else if (this.tu == Tukey.t) {
            return 't';
        } else if (this.tu == Tukey.none) {
            return '-';
        } else {
            return 'n';
        }
    }

    @Override
    public String toString() {
        char[] s = new char[]{ar(), tu()};
        return new String(s);
    }

    /**
     * Corresponds to the fortran function SeasSpectCrit
     *
     * @param peaks
     *
     * @return
     */
    public static boolean hasSeasonalPeaks(SpectralPeaks[] peaks) {
        if (peaks == null) {
            return false;
        }
        if (peaks.length == 6) {
            return hasMonthlyPeaks(peaks);
        } else if (peaks.length == 2) {
            return hasQuarterlyPeaks(peaks);
        } else if (peaks.length == 1) {
            return hasHalfYearlyPreaks(peaks);

        } else {
            return false;
        }
    }

    /**
     * Corresponds to the fortran function SeasSpectCrit2
     *
     * @param peaks
     *
     * @return
     */
    public static boolean hasHighSeasonalPeaks(SpectralPeaks[] peaks) {
        if (peaks.length == 6) {
            return hasHighMonthlyPeaks(peaks);
        } else if (peaks.length == 2) {
            return hasHighQuarterlyPeaks(peaks);
        } else if (peaks.length == 1) {
            return hasHighHalfYearlyPreaks(peaks);
        } else {
            return false;
        }
    }

    private static boolean hasMonthlyPeaks(SpectralPeaks[] peaks) {
        int n = 0, nd = 0;
        for (int i = 0; i < peaks.length; ++i) {
            if (peaks[i].hasPeak()) {
                ++n;
                if (peaks[i].equals(ALL)) {
                    ++nd;
                }
            }
        }

        switch (n) {
            case 4:
            case 5:
            case 6:
                return true;
            case 3:
                if (nd >= 1 || (!peaks[5].hasPeak())) {
                    return true;
                } else {
                    return false;
                }
            case 2:
                if (peaks[5].equals(ALL) && nd == 2) {
                    return true;
                } else if (peaks[5].equals(NONE)) {
                    return nd >= 1;
                } else {
                    return false;
                }

            default:
                return false;
        }
    }

    private static boolean hasHighMonthlyPeaks(SpectralPeaks[] peaks) {
        int n = 0, nd = 0;
        for (int i = 0; i < peaks.length; ++i) {
            if (peaks[i].hasHighPeak()) {
                ++n;
                if (peaks[i].equals(ALL)) {
                    ++nd;
                }
            }
        }

        switch (n) {
            case 4:
            case 5:
            case 6:
                return true;
            case 3:
                if (nd >= 1 || (!peaks[5].hasHighPeak())) {
                    return true;
                } else {
                    return false;
                }
            case 2:
                if (peaks[5].equals(ALL)) {
                    return nd == 2;
                } else //if (a[5] != 2 || t[5] != 2)
                {
                    return nd >= 1;
                }

            default:
                return false;
        }
    }

    private static boolean hasHighQuarterlyPeaks(SpectralPeaks[] peaks) {
        if (peaks[0].equals(ALL)) {
            return true;
        }
        int n = 0;
        for (int i = 0; i < peaks.length; ++i) {
            if (peaks[i].hasHighPeak()) {
                ++n;
            }
        }
        return n == 2;
    }

    private static boolean hasQuarterlyPeaks(SpectralPeaks[] peaks) {
        if (peaks[0].equals(ALL)) {
            return true;
        }
        int n = 0;
        for (int i = 0; i < peaks.length; ++i) {
            if (peaks[i].hasPeak()) {
                ++n;
            }
        }
        return n == 2;
    }

    private static boolean hasHalfYearlyPreaks(SpectralPeaks[] peaks) {
        return peaks[0].hasPeak();
    }

    private static boolean hasHighHalfYearlyPreaks(SpectralPeaks[] peaks) {
        return peaks[0].hasHighPeak();
    }

    public static String format(SpectralPeaks[] peaks) {
        StringBuilder builder = new StringBuilder();
        if (peaks != null) {
            for (int i = 0; i < peaks.length; ++i) {
                if (i != 0) {
                    builder.append('.');
                }
                builder.append(peaks[i].toString());
            }
        }
        return builder.toString();
    }
}
