/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.IFilterOutput;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import static demetra.x11.X11Kernel.table;
import demetra.x11.extremevaluecorrector.IExtremeValuesCorrector;
import demetra.x11.extremevaluecorrector.PeriodSpecificExtremeValuesCorrector;
import demetra.x11.filter.AutomaticHenderson;
import demetra.x11.filter.DefaultSeasonalNormalizer;
import demetra.x11.filter.IFiltering;
import demetra.x11.filter.MusgraveFilterFactory;
import demetra.x11.filter.X11FilterFactory;
import demetra.x11.filter.X11SeasonalFiltersFactory;
import demetra.x11.filter.endpoints.AsymmetricEndPoints;
import lombok.AccessLevel;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11DStep {

    private static double EPS = 1e-9;

    private DoubleSequence d1, d2, d4, d5, d6, d7, d8, d9, d9g, d9bis, d9_g_bis, d10, d10bis, d11, d11bis, d12, d13;
    private int d2drop, finalHendersonFilterLength;
    private double iCRatio;
    @lombok.Getter(AccessLevel.NONE)
    private DoubleSequence refSeries;

    public void process(DoubleSequence refSeries, DoubleSequence input, X11Context context) throws Exception {
        this.refSeries = refSeries;
        d1 = input;
        d2(context);
        d4(context);
        d5(context);
        d6(context);
        d7(context);
        d8(context);
        d9(context);
        dfinal(context);
    }

    private void d2(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        d2drop = filter.length() / 2;

        double[] x = table(d1.length() - 2 * d2drop, Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, 0, x.length);
        filter.apply(i -> d1.get(i), IFilterOutput.of(out, d2drop));
        d2 = DoubleSequence.ofInternal(x);
    }

    private void d4(X11Context context) {
        d4 = context.remove(d1.drop(d2drop, d2drop), d2);
    }

    private void d5(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSequence d5a = filter.process(d4);
        d5 = DefaultSeasonalNormalizer.normalize(d5a, d2drop, context);
    }

    private void d6(X11Context context) {
        d6 = context.remove(d1, d5);
    }

    private void d7(X11Context context) throws Exception {
        SymmetricFilter filter;
        if (context.isAutomaticHenderson()) {
            double icr = AutomaticHenderson.calcICR(context, d6);
            int filterLength = AutomaticHenderson.selectFilter(icr, context.getPeriod());
            filter = context.trendFilter(filterLength);
        } else {
            filter = context.trendFilter();
        }
        int ndrop = filter.length() / 2;

        double[] x = table(d6.length(), Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, ndrop, x.length - ndrop);
        filter.apply(i -> d6.get(i), IFilterOutput.of(out, ndrop));

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(filter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(filter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(d6, DataBlock.ofInternal(x));
        d7 = DoubleSequence.ofInternal(x);
        if (context.isMultiplicative()) {
            d7 = d7.makePositivity();
        }
    }

    private void d8(X11Context context) {
        d8 = context.remove(refSeries, d7);
    }

    private void d9(X11Context context) {
        IExtremeValuesCorrector ecorr = context.getExtremeValuesCorrector();
        if (ecorr instanceof PeriodSpecificExtremeValuesCorrector && context.getCalendarSigma() != CalendarSigmaOption.Signif) {
            d9 = ecorr.computeCorrections(d8.drop(0, context.getForecastHorizon()));
            DoubleSequence ds = d9.extend(0, context.getForecastHorizon());
            d9g = ecorr.applyCorrections(d8, ds);
            d9_g_bis = d9g;
        } else {
            d9bis = context.remove(d1, d7);
            DoubleSequence d9temp = DoubleSequence.onMapping(d9bis.length(), i -> Math.abs(d9bis.get(i) - d8.get(i)));
            d9 = DoubleSequence.onMapping(d9temp.length() - context.getForecastHorizon(), i -> d9temp.get(i) < EPS ? Double.NaN : d9bis.get(i));
            d9_g_bis = d9bis;
        }

    }

    private void dfinal(X11Context context) throws Exception {
        IFiltering filter = msr(context, d9_g_bis);
        d10bis = filter.process(d9_g_bis);
        d10 = DefaultSeasonalNormalizer.normalize(d10bis, 0, context);

        d11bis = context.remove(d1, d10);
        d11 = context.remove(refSeries, d10);

        SymmetricFilter hfilter;
        iCRatio = AutomaticHenderson.calcICR(context, d11bis);
        if (context.isAutomaticHenderson()) {
            int filterLength = AutomaticHenderson.selectFilter(iCRatio, context.getPeriod());
            hfilter = context.trendFilter(filterLength);
        } else {
            hfilter = context.trendFilter();
        }
        finalHendersonFilterLength = hfilter.length();
        int ndrop = hfilter.length() / 2;

        double[] x = table(d11bis.length(), Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, ndrop, x.length - ndrop);
        hfilter.apply(i -> d11bis.get(i), IFilterOutput.of(out, ndrop));

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(hfilter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(hfilter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(d11bis, DataBlock.ofInternal(x));
        d12 = DoubleSequence.ofInternal(x);
        if (context.isMultiplicative()) {
            d12 = d12.makePositivity();
        }

        d13 = context.remove(d11, d12);

    }

    private IFiltering msr(X11Context context, DoubleSequence input) {
        SeasonalFilterOption seasFilter;
        if (context.isMSR()) {
            seasFilter = X11FilterFactory.calcRatioIS(input, context);
            System.out.println("MSR: " + seasFilter.name());
        } else {
            seasFilter = context.getFinalSeasonalFilter();
        }
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), seasFilter);
        return filter;
    }
}
