/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DoubleSeq;
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
import jdplus.data.DataBlock;
import jdplus.maths.linearfilters.IFiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11DStep {

    private static final double EPS = 1e-9;

    private DoubleSeq d1, d2, d4, d5, d6, d7, d8, d9, d9g, d9bis, d9_g_bis, d10, d10bis, d11, d11bis, d12, d13;
    private int d2drop, finalHendersonFilterLength;
    private double iCRatio;
    private SeasonalFilterOption[] seasFilter;
    private DoubleSeq refSeries;

    public void process(DoubleSeq refSeries, DoubleSeq input, X11Context context) {
        this.refSeries = refSeries;
        d1Step(context, input);
        d2Step(context);
        d4Step(context);
        d5Step(context);
        d6Step(context);
        d7Step(context);
        d8Step(context);
        d9Step(context);
        dFinalStep(context);
    }

    private void d1Step(X11Context context, DoubleSeq input) {
        d1 = d1(context, input);
    }

    protected DoubleSeq d1(X11Context context, DoubleSeq input) {
        return context.remove(this.refSeries, input);
    }

    private void d2Step(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        d2drop = filter.length() / 2;

        double[] x = table(d1.length() - 2 * d2drop, Double.NaN);
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(d1, out);
        d2 = DoubleSeq.of(x);
    }

    private void d4Step(X11Context context) {
        d4 = context.remove(d1.drop(d2drop, d2drop), d2);
    }

    private void d5Step(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSeq d5a = processor.process(d4, (context.getFirstPeriod() + d2drop) % context.getPeriod());
        d5 = DefaultSeasonalNormalizer.normalize(d5a, d2drop, context);
    }

    private void d6Step(X11Context context) {
        d6 = d6(context);
    }

    protected DoubleSeq d6(X11Context context) {
        return context.remove(d1, d5);
    }

    private void d7Step(X11Context context) {
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
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        filter.apply(d6, out);

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(filter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(filter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(d6, DataBlock.of(x));
        d7 = DoubleSeq.of(x);
        if (context.isMultiplicative()) {
            d7 = X11Context.makePositivity(d7);
        }
    }

    private void d8Step(X11Context context) {
        d8 = d8(context);
    }

    protected DoubleSeq d8(X11Context context) {
        return context.remove(refSeries, d7);
    }

    private void d9Step(X11Context context) {
        IExtremeValuesCorrector ecorr = context.getExtremeValuesCorrector();
        if (ecorr instanceof PeriodSpecificExtremeValuesCorrector && context.getCalendarSigma() != CalendarSigmaOption.Signif) {
            //compute corrections without forecast but keep the length
            d9 = ecorr.computeCorrections(d8.drop(0, context.getForecastHorizon())).extend(0, context.getForecastHorizon());
            d9g = ecorr.applyCorrections(d8, d9);
            d9_g_bis = d9g;
        } else {
            d9bis = context.remove(d1, d7);
            DoubleSeq d9temp = DoubleSeq.onMapping(d9bis.length(), i -> Math.abs(d9bis.get(i) - d8.get(i)));
            d9 = DoubleSeq.onMapping(d9temp.length(), i -> d9temp.get(i) < EPS ? Double.NaN : d9bis.get(i));
            d9_g_bis = d9bis;
        }
    }

    private void dFinalStep(X11Context context) {
        seasFilter = context.getFinalSeasonalFilter();
        if (context.isMSR()) {
            MsrFilterSelection msr = getMsrFilterSelection();
            SeasonalFilterOption msrFilter = msr.doMSR(d9_g_bis, context);
            Arrays.fill(seasFilter, msrFilter);
        }
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), seasFilter);
        d10bis = processor.process(d9_g_bis, context.getFirstPeriod());
        d10 = DefaultSeasonalNormalizer.normalize(d10bis, 0, context);

        d11bis = d11bis(context);

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
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        hfilter.apply(d11bis, out);

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(hfilter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(hfilter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(d11bis, DataBlock.of(x));
        d12 = DoubleSeq.of(x);
        if (context.isMultiplicative()) {
            d12 = X11Context.makePositivity(d12);
        }

        d11 = d11(context);

        d13 = context.remove(d11, d12);

    }

    protected MsrFilterSelection getMsrFilterSelection() {
        return new MsrFilterSelection();
    }

    protected DoubleSeq d11(X11Context context) {
        return context.remove(refSeries, d10);
    }

    protected DoubleSeq d11bis(X11Context context) {
        return context.remove(d1, d10);
    }
}
