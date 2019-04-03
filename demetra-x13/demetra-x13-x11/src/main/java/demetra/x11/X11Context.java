/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DataBlock;
import demetra.data.DiscreteKernel;
import demetra.maths.linearfilters.FiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.x11.extremevaluecorrector.Cochran;
import demetra.x11.extremevaluecorrector.DefaultExtremeValuesCorrector;
import demetra.x11.extremevaluecorrector.GroupSpecificExtremeValuesCorrector;
import demetra.x11.extremevaluecorrector.IExtremeValuesCorrector;
import demetra.x11.extremevaluecorrector.PeriodSpecificExtremeValuesCorrector;
import java.util.function.IntToDoubleFunction;
import lombok.experimental.NonFinal;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class X11Context {

    @lombok.NonNull
    DecompositionMode mode;
    int period;
    int trendFilterLength;
    int localPolynomialDegree;
    @lombok.NonNull
    SeasonalFilterOption initialSeasonalFilter;
    @lombok.NonNull
    SeasonalFilterOption finalSeasonalFilter;
    double lowerSigma, upperSigma;
    CalendarSigmaOption calendarSigma;
    SigmavecOption[] sigmavecOptions;
    int forecastHorizon;
    int firstPeriod;
    /**
     * Excludefcast is true if the forecast should be excluded for the calculation of the standard deviation of the extreme values
     */
    boolean excludefcast;

    @NonFinal
    IExtremeValuesCorrector extremeValuesCorrector;

    public static X11ContextBuilder builder() {
        X11ContextBuilder builder = new X11ContextBuilder();
        builder.mode = DecompositionMode.Multiplicative;
        builder.trendFilterLength = 13;
        builder.localPolynomialDegree = 3;
        builder.initialSeasonalFilter = SeasonalFilterOption.S3X3;
        builder.finalSeasonalFilter = SeasonalFilterOption.S3X5;
        builder.calendarSigma = CalendarSigmaOption.None;
        builder.lowerSigma = 1.5;
        builder.upperSigma = 2.5;
        builder.firstPeriod = 0;
        return builder;
    }

    public static X11Context of(X11Spec spec, TsData data) {
        return builder().mode(spec.getMode())
                .trendFilterLength(spec.getHendersonFilterLength())
                .period(data.getAnnualFrequency())
                .firstPeriod(data.getStart().annualPosition())
                .lowerSigma(spec.getLowerSigma())
                .upperSigma(spec.getUpperSigma())
                .calendarSigma(spec.getCalendarSigma())
                .sigmavecOptions(spec.getSigmavec().toArray(new SigmavecOption[0]))
                .excludefcast(spec.isExcludeForecast())
                .forecastHorizon(spec.getForecastHorizon())
                .initialSeasonalFilter(spec.getFilters().get(0))
                .finalSeasonalFilter(spec.getFilters().get(0))
                .build();
    }

    public boolean isAutomaticHenderson() {
        return trendFilterLength == 0;
    }

    public boolean isMultiplicative() {
        return mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive;
    }

    public boolean isLogAdd() {
        return mode == DecompositionMode.LogAdditive;
    }

    public DoubleSeq remove(DoubleSeq l, DoubleSeq r) {
        if (isMultiplicative()) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) / r.get(i));
        } else {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) - r.get(i));
        }
    }

    public DoubleSeq add(DoubleSeq l, DoubleSeq r) {
        if (isMultiplicative()) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) * r.get(i));
        } else {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) + r.get(i));
        }
    }

    public void remove(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (isMultiplicative()) {
            q.set(l, r, (x, y) -> x / y);
        } else {
            q.set(l, r, (x, y) -> x - y);
        }
    }

    public void add(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (isMultiplicative()) {
            q.set(l, r, (x, y) -> x * y);
        } else {
            q.set(l, r, (x, y) -> x + y);
        }
    }

    public SymmetricFilter trendFilter() {
        return trendFilter(trendFilterLength);
    }

    public SymmetricFilter trendFilter(int filterLength) {
        int horizon = filterLength / 2;
        IntToDoubleFunction weights = DiscreteKernel.henderson(horizon);
        return demetra.maths.linearfilters.LocalPolynomialFilters.of(horizon, localPolynomialDegree, weights);
    }

    private static final double SQRPI = Math.sqrt(Math.PI);

    public FiniteFilter[] asymmetricTrendFilters(SymmetricFilter sfilter, double ic) {
        double d = 2 / (SQRPI * ic);
        FiniteFilter[] afilters;
        int horizon = sfilter.getUpperBound();
        int u = 0;
        double[] c = new double[]{d};
        afilters = new FiniteFilter[horizon];
        for (int i = 0; i < afilters.length; ++i) {
            afilters[horizon - i - 1] = demetra.maths.linearfilters.LocalPolynomialFilters.asymmetricFilter(sfilter, i, u, c, null);
        }
        return afilters;
    }

    /**
     * Selects the extreme value corrector depending on the result of the CochranTest if CalendarSimga is Signif, the other extreme value corrector is used
     *
     * @param dsToTest
     *
     * @return
     */
    public IExtremeValuesCorrector selectExtremeValuesCorrector(DoubleSeq dsToTest) {
        if (calendarSigma == CalendarSigmaOption.Signif) {
            Cochran cochranTest = new Cochran(dsToTest, this);
            boolean testResult = cochranTest.getTestResult();
            if (!testResult) {
                extremeValuesCorrector = new PeriodSpecificExtremeValuesCorrector();
            } else {
                extremeValuesCorrector = new DefaultExtremeValuesCorrector();
            }
        }
        return getExtremeValuesCorrector();

    }

    public IExtremeValuesCorrector getExtremeValuesCorrector() {

        if (extremeValuesCorrector == null) {

            switch (calendarSigma) {
                case All:
                    extremeValuesCorrector = new PeriodSpecificExtremeValuesCorrector();
                    break;
                case Signif:
                    break;
                case Select:
                    extremeValuesCorrector = new GroupSpecificExtremeValuesCorrector(sigmavecOptions);
                    break;
                default:
                    extremeValuesCorrector = new DefaultExtremeValuesCorrector();
                    break;
            }
        }

        return extremeValuesCorrector;

    }

    public boolean isMSR() {
        return SeasonalFilterOption.Msr.equals(finalSeasonalFilter);
    }

    public SeasonalFilterOption getInitialSeasonalFilter() {
        if (SeasonalFilterOption.Msr.equals(initialSeasonalFilter) || SeasonalFilterOption.X11Default.equals(initialSeasonalFilter)) {
            return SeasonalFilterOption.S3X3;
        }
        return initialSeasonalFilter;
    }

    public SeasonalFilterOption getFinalSeasonalFilter() {
        if (SeasonalFilterOption.Msr.equals(finalSeasonalFilter) || SeasonalFilterOption.X11Default.equals(finalSeasonalFilter)) {
            return SeasonalFilterOption.S3X5;
        }
        return finalSeasonalFilter;
    }

    /**
     * Replace negative values of a Double Sequence with either the mean of the two
     * nearest positive replacements before and after the value, or the nearest value
     * if it is on the ends of the series.
     *
     * @param in
     *
     * @return new DoubleSeq
     */
    public DoubleSeq makePositivity(DoubleSeq in) {
        double[] stc = in.toArray();
        int n = in.length();
        for (int i = 0; i < n; ++i) {
            if (stc[i] <= 0) {
                int before = i - 1;
                while (before >= 0 && stc[before] <= 0) {
                    --before;
                }
                int after = i + 1;
                while (after < n && stc[after] <= 0) {
                    ++after;
                }
                double m;
                if (before < 0 && after >= n) {
                    throw new X11Exception("Negative series");
                }
                if (before >= 0 && after < n) {
                    m = (stc[before] + stc[after]) / 2;
                } else if (after >= n) {
                    m = stc[before];
                } else {
                    m = stc[after];
                }
                stc[i] = m;
            }
        }
        return DoubleSeq.copyOf(stc);
    }
}
