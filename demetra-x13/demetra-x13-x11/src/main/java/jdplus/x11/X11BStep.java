/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11;

import demetra.data.DoubleSeq;
import static jdplus.x11.X11Kernel.table;
import jdplus.x11.extremevaluecorrector.IExtremeValuesCorrector;
import jdplus.x11.filter.AutomaticHenderson;
import jdplus.x11.filter.DefaultSeasonalNormalizer;
import jdplus.x11.filter.MusgraveFilterFactory;
import jdplus.x11.filter.X11FilterFactory;
import jdplus.x11.filter.X11SeasonalFilterProcessor;
import jdplus.x11.filter.X11SeasonalFiltersFactory;
import jdplus.x11.filter.endpoints.AsymmetricEndPoints;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.IFiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11BStep {

    private DoubleSeq b1, b2, b3, b4, b4a, b4anorm, b4d, b4g, b5, b6,
            b7, b8, b9, b9g, b10, b11, b13, b17, b20;
    private int b2drop;

    public X11BStep() {
    }

    public void process(DoubleSeq input, X11Context context) {

        b1 = input;
        b2Step(context);
        b3Step(context);
        b4Step(context);
        b5Step(context);
        b6Step(context);
        b7Step(context);
        b8Step(context);
        b9Step(context);
        bFinalStep(context);
    }

    private void b2Step(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        b2drop = filter.length() / 2;

        double[] x = table(b1.length() - 2 * b2drop, Double.NaN);
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(b1, out);
        b2 = DoubleSeq.of(x);
    }

    private void b3Step(X11Context context) {
        b3 = context.remove(b1.drop(b2drop, b2drop), b2);
    }

    private void b4Step(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        b4a = processor.process(b3, (context.getFirstPeriod() + b2drop) % context.getPeriod());
        b4anorm = DefaultSeasonalNormalizer.normalize(b4a, 0, context, b2drop);
        b4d = b4d(context);
        IExtremeValuesCorrector ecorr = context.selectExtremeValuesCorrector(b4d);
        int j = (b2drop + context.getFirstPeriod()) % context.getPeriod();
        ecorr.setStart(j);
        ecorr.analyse(b4d, context);

        b4 = ecorr.computeCorrections(b3);
        b4g = ecorr.applyCorrections(b3, b4);
    }

    protected DoubleSeq b4d(X11Context context) {
        return context.remove(b3, b4anorm);
    }

    private void b5Step(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSeq b5a = processor.process(b4g, (context.getFirstPeriod() + b2drop) % context.getPeriod());
        b5 = DefaultSeasonalNormalizer.normalize(b5a, b2drop, context);
    }

    private void b6Step(X11Context context) {
        b6 = b6(context);
    }

    protected DoubleSeq b6(X11Context context) {
        return context.remove(b1, b5);
    }

    private void b7Step(X11Context context) {
        SymmetricFilter filter;
        if (context.isAutomaticHenderson()) {
            double icr = AutomaticHenderson.calcICR(context, b6);
            int filterLength;
            if (icr >= 1.0 && context.getPeriod() != 2) {
                filterLength = context.getPeriod() + 1;
            } else {
                filterLength = AutomaticHenderson.selectFilter(icr, context.getPeriod());
            }
            filter = context.trendFilter(filterLength);
        } else {
            filter = context.trendFilter();
        }
        int ndrop = filter.length() / 2;

        double[] x = table(b6.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        filter.apply(b6, out);

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(filter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(filter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(b6, DataBlock.of(x));
        b7 = DoubleSeq.of(x);
        if (context.isMultiplicative()) {
            b7 = X11Context.makePositivity(b7);
        }
    }

    private void b8Step(X11Context context) {
        b8 = context.remove(b1, b7);
    }

    private void b9Step(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSeq b9a = processor.process(b8, context.getFirstPeriod());
        DoubleSeq b9c = DefaultSeasonalNormalizer.normalize(b9a, 0, context);
        DoubleSeq b9d = b9d(context, b9c);
        IExtremeValuesCorrector ecorr = context.getExtremeValuesCorrector();
        ecorr.setStart(context.getFirstPeriod());
        ecorr.analyse(b9d, context);

        b9 = ecorr.computeCorrections(b8);
        b9g = ecorr.applyCorrections(b8, b9);
    }

    protected DoubleSeq b9d(X11Context context, DoubleSeq b9c) {
        return context.remove(b8, b9c);
    }

    private void bFinalStep(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSeq b10a = processor.process(b9g, context.getFirstPeriod());
        b10 = DefaultSeasonalNormalizer.normalize(b10a, 0, context);
        b11 = b11(context);
        b13 = context.remove(b11, b7);

        IExtremeValuesCorrector ecorr = context.selectExtremeValuesCorrector(b13);
        ecorr.setStart(context.getFirstPeriod());
        ecorr.analyse(b13, context);
        b17 = ecorr.getObservationWeights();
        b20 = ecorr.getCorrectionFactors();
    }

    protected DoubleSeq b11(X11Context context) {
        return context.remove(b1, b10);
    }
}
