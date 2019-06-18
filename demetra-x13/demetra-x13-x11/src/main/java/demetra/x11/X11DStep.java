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
import demetra.x11.filter.MsrFilterSelection;
import demetra.x11.filter.MusgraveFilterFactory;
import demetra.x11.filter.X11FilterFactory;
import demetra.x11.filter.X11SeasonalFilterProcessor;
import demetra.x11.filter.X11SeasonalFiltersFactory;
import demetra.x11.filter.endpoints.AsymmetricEndPoints;
import java.util.Arrays;
import lombok.AccessLevel;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11DStep {

    private static final double EPS = 1e-9;

    private DoubleSequence d1, d2, d4, d5, d6, d7, d8, d9, d9g, d9bis, d9_g_bis, d10, d10bis, d11, d11bis, d12, d13;
    private int d2drop, finalHendersonFilterLength;
    private double iCRatio;
    private SeasonalFilterOption[] seasFilter;
    @lombok.Getter(AccessLevel.NONE)
    private DoubleSequence refSeries;

    public void process(DoubleSequence refSeries, DoubleSequence input, X11Context context) {
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
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSequence d5a = processor.process(d4, (context.getFirstPeriod() + d2drop) % context.getPeriod());
        d5 = DefaultSeasonalNormalizer.normalize(d5a, d2drop, context);
    }

    private void d6(X11Context context) {
        d6 = context.remove(d1, d5);
    }

    private void d7(X11Context context) {
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
            d7 = X11Context.makePositivity(d7);
        }
    }

    private void d8(X11Context context) {
        d8 = context.remove(refSeries, d7);
    }

    private void d9(X11Context context) {
        IExtremeValuesCorrector ecorr = context.getExtremeValuesCorrector();
        if (ecorr instanceof PeriodSpecificExtremeValuesCorrector && context.getCalendarSigma() != CalendarSigmaOption.Signif) {
            //compute corrections without forecast but keep the length
            d9 = ecorr.computeCorrections(d8.drop(0, context.getForecastHorizon())).extend(0, context.getForecastHorizon());
            d9g = ecorr.applyCorrections(d8, d9);
            d9_g_bis = d9g;
        } else {
            d9bis = context.remove(d1, d7);
            DoubleSequence d9temp = DoubleSequence.onMapping(d9bis.length(), i -> Math.abs(d9bis.get(i) - d8.get(i)));
            d9 = DoubleSequence.onMapping(d9temp.length(), i -> d9temp.get(i) < EPS ? Double.NaN : d9bis.get(i));
            d9_g_bis = d9bis;
        }
    }

    private void dfinal(X11Context context) {
        seasFilter = context.getFinalSeasonalFilter();
        if (context.isMSR()) {
            MsrFilterSelection msr = new MsrFilterSelection();
            SeasonalFilterOption msrFilter = msr.doMSR(d9_g_bis, context);
            Arrays.fill(seasFilter, msrFilter);
        }
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), seasFilter);
        d10bis = processor.process(d9_g_bis, context.getFirstPeriod());
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
            d12 = X11Context.makePositivity(d12);
        }

        d13 = context.remove(d11, d12);

    }
}
