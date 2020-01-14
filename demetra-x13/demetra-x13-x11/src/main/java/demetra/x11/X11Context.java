/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DoubleSeq;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.x11.extremevaluecorrector.Cochran;
import demetra.x11.extremevaluecorrector.DefaultExtremeValuesCorrector;
import demetra.x11.extremevaluecorrector.GroupSpecificExtremeValuesCorrector;
import demetra.x11.extremevaluecorrector.IExtremeValuesCorrector;
import demetra.x11.extremevaluecorrector.PeriodSpecificExtremeValuesCorrector;
import java.util.function.IntToDoubleFunction;
import jdplus.data.DataBlock;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.math.linearfilters.AsymmetricFilters;
import jdplus.math.linearfilters.IFiniteFilter;
import jdplus.math.linearfilters.LocalPolynomialFilters;
import jdplus.math.linearfilters.SymmetricFilter;
import lombok.experimental.NonFinal;

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
    SeasonalFilterOption[] initialSeasonalFilter;
    @lombok.NonNull
    SeasonalFilterOption[] finalSeasonalFilter;
    double lowerSigma, upperSigma;
    CalendarSigmaOption calendarSigma;
    SigmavecOption[] sigmavecOptions;
    int forecastHorizon;
    int backcastHorizon;
    int firstPeriod;
    /**
     * Excludefcast is true if the forecast should be excluded for the
     * calculation of the standard deviation of the extreme values
     */
    boolean excludefcast;

    @NonFinal
    IExtremeValuesCorrector extremeValuesCorrector;

    public static X11ContextBuilder builder() {
        X11ContextBuilder builder = new X11ContextBuilder();
        builder.mode = DecompositionMode.Multiplicative;
        builder.trendFilterLength = 13;
        builder.localPolynomialDegree = 3;
        builder.period = 1;
        builder.initialSeasonalFilter = new SeasonalFilterOption[]{SeasonalFilterOption.S3X3};
        builder.finalSeasonalFilter = new SeasonalFilterOption[]{SeasonalFilterOption.S3X5};
        builder.calendarSigma = CalendarSigmaOption.None;
        builder.lowerSigma = 1.5;
        builder.upperSigma = 2.5;
        builder.firstPeriod = 0;
        return builder;
    }

    public static X11Context of(@lombok.NonNull X11Spec spec, @lombok.NonNull TsData data) {
        SeasonalFilterOption[] filters = new SeasonalFilterOption[data.getAnnualFrequency()];
        if (spec.getFilters().size() == 1) {
            filters = new SeasonalFilterOption[data.getAnnualFrequency()];
            SeasonalFilterOption filter = spec.getFilters().get(0);
            for (int i = 0; i < data.getAnnualFrequency(); i++) {
                filters[i] = filter;
            }
        } else {
            filters = spec.getFilters().toArray(new SeasonalFilterOption[0]);
        }

        return builder().mode(spec.getMode())
                .trendFilterLength(spec.getHendersonFilterLength())
                .period(data.getAnnualFrequency())
                .firstPeriod(data.getStart().annualPosition())
                .lowerSigma(spec.getLowerSigma())
                .upperSigma(spec.getUpperSigma())
                .calendarSigma(spec.getCalendarSigma())
                .sigmavecOptions(spec.getSigmavec() == null ? null : spec.getSigmavec().toArray(new SigmavecOption[0]))
                .excludefcast(spec.isExcludeForecast())
                .forecastHorizon(spec.getForecastHorizon())
                .backcastHorizon(spec.getBackcastHorizon())
                .initialSeasonalFilter(filters)
                .finalSeasonalFilter(filters)
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

    public boolean isPseudoAdd() {
        return mode == DecompositionMode.PseudoAdditive;
    }

    public DoubleSeq remove(DoubleSeq l, DoubleSeq r) {
        if (isMultiplicative()) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) / r.get(i));
        }
        return DoubleSeq.onMapping(l.length(), i -> l.get(i) - r.get(i));
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
        return LocalPolynomialFilters.of(horizon, localPolynomialDegree, weights);
    }

    private static final double SQRPI = Math.sqrt(Math.PI);

    public IFiniteFilter[] asymmetricTrendFilters(SymmetricFilter sfilter, double ic) {
        double d = 2 / (SQRPI * ic);
        int horizon = sfilter.getUpperBound();
        int u = 0;
        double[] c = new double[]{d};
        IFiniteFilter[] afilters = new IFiniteFilter[horizon];
        for (int i = 0; i < afilters.length; ++i) {
            afilters[horizon - i - 1] = AsymmetricFilters.mmsreFilter2(sfilter, i, u, c, null);
        }
        return afilters;
    }

    /**
     * Selects the extreme value corrector depending on the result of the
     * CochranTest if CalendarSimga is Signif, the other extreme value corrector
     * is used
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

    /**
     * MSR calculation is just for all periods. In case of mixed filters and
     * MSR, the MSR defaults will be used.
     */
    public boolean isMSR() {
        for (SeasonalFilterOption option : finalSeasonalFilter) {
            if (!SeasonalFilterOption.Msr.equals(option)) {
                return false;
            }
        }
        return true;
    }

    public SeasonalFilterOption[] getInitialSeasonalFilter() {

        SeasonalFilterOption[] result = new SeasonalFilterOption[period];
        for (int i = 0; i < period; i++) {
            result[i] = initialSeasonalFilter[i];
            if (SeasonalFilterOption.Msr.equals(initialSeasonalFilter[i]) || SeasonalFilterOption.X11Default.equals(initialSeasonalFilter[i])) {
                result[i] = SeasonalFilterOption.S3X3;
            }
        }
        return result;
    }

    public SeasonalFilterOption[] getFinalSeasonalFilter() {
        SeasonalFilterOption[] result = new SeasonalFilterOption[period];
        for (int i = 0; i < period; i++) {
            result[i] = finalSeasonalFilter[i];
            if (SeasonalFilterOption.Msr.equals(finalSeasonalFilter[i]) || SeasonalFilterOption.X11Default.equals(finalSeasonalFilter[i])) {
                result[i] = SeasonalFilterOption.S3X5;
            }
        }
        return result;
    }

    /**
     * Replace negative values of a Double Sequence with either the mean of the
     * two nearest positive replacements before and after the value, or the
     * nearest value if it is on the ends of the series.
     *
     * @param in
     *
     * @return new DoubleSeq
     */
    public static DoubleSeq makePositivity(DoubleSeq in) {
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
