/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus;

import jdplus.data.DataBlock;
import jdplus.maths.linearfilters.IFiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import static jdplus.x11plus.X11Kernel.table;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11DStep {

    private DoubleSeq refSeries, d1, d2, d4, d5, d6, d7, d8, d9, d9bis, d10, d10bis, d11, d11bis, d12, d13;
    private int d2drop;

    public void process(DoubleSeq refSeries, DoubleSeq input, X11Context context) {
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
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(d1, out);
        d2 = DoubleSeq.of(x);
    }

    private void d4(X11Context context) {
        d4 = context.remove(d1.drop(d2drop, d2drop), d2);
    }

    private void d5(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSeq d5a = filter.process(d4);
        d5 = DefaultSeasonalNormalizer.normalize(d5a, d2drop, context);
    }

    private void d6(X11Context context) {
        d6 = context.remove(d1, d5);
    }

    private void d7(X11Context context) {
        SymmetricFilter filter = context.trendFilter();
        int ndrop = filter.length() / 2;

        double[] x = table(d6.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        filter.apply(d6, out);

       // apply asymmetric filters
        double r=MusgraveFilterFactory.findR(filter.length(), context.getPeriod().intValue());
        IFiniteFilter[] lf = context.leftAsymmetricTrendFilters(filter, r); 
        IFiniteFilter[] rf = context.rightAsymmetricTrendFilters(filter, r); 
        AsymmetricEndPoints aep=new AsymmetricEndPoints(lf, -1);
        aep.process(d6, DataBlock.of(x));
        aep=new AsymmetricEndPoints(rf, 1);
        aep.process(d6, DataBlock.of(x));
        d7 = DoubleSeq.of(x);
        if (d7.anyMatch(z -> z <= 0)) {
            throw new X11Exception(X11Exception.ERR_NEG);
        }
    }

    private void d8(X11Context context) {
        d8 = context.remove(refSeries, d7);
    }

    private void d9(X11Context context) {
        d9bis = context.remove(d1, d7);
    }

    private void dfinal(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        d10bis = filter.process(d9bis);
        d10 = DefaultSeasonalNormalizer.normalize(d10bis, 0, context);
        d11bis = context.remove(d1, d10);
        d11 = context.remove(refSeries, d10);

        SymmetricFilter hfilter = context.trendFilter();
        int ndrop = hfilter.length() / 2;

        double[] x = table(d11bis.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        hfilter.apply(d11bis, out);

       // apply asymmetric filters
        double r=MusgraveFilterFactory.findR(hfilter.length(), context.getPeriod().intValue());
        IFiniteFilter[] lf = context.leftAsymmetricTrendFilters(hfilter, r); 
        IFiniteFilter[] rf = context.rightAsymmetricTrendFilters(hfilter, r); 
        AsymmetricEndPoints aep=new AsymmetricEndPoints(lf, -1);
        aep.process(d11bis, DataBlock.of(x));
        aep=new AsymmetricEndPoints(rf, 1);
        aep.process(d11bis, DataBlock.of(x));
        d12 = DoubleSeq.of(x);
        if (d12.anyMatch(z -> z <= 0)) {
            throw new X11Exception(X11Exception.ERR_NEG);
        }

        d13 = context.remove(d11, d12);

    }

    /**
     * @return the d1
     */
    public DoubleSeq getD1() {
        return d1;
    }

    /**
     * @return the d2
     */
    public DoubleSeq getD2() {
        return d2;
    }

    /**
     * @return the d4
     */
    public DoubleSeq getD4() {
        return d4;
    }

    /**
     * @return the d5
     */
    public DoubleSeq getD5() {
        return d5;
    }

    /**
     * @return the d6
     */
    public DoubleSeq getD6() {
        return d6;
    }

    /**
     * @return the d7
     */
    public DoubleSeq getD7() {
        return d7;
    }

    /**
     * @return the d8
     */
    public DoubleSeq getD8() {
        return d8;
    }

    /**
     * @return the d9
     */
    public DoubleSeq getD9() {
        return d9;
    }

    /**
     * @return the d9bis
     */
    public DoubleSeq getD9bis() {
        return d9bis;
    }

    /**
     * @return the d10
     */
    public DoubleSeq getD10() {
        return d10;
    }

    /**
     * @return the d10bis
     */
    public DoubleSeq getD10bis() {
        return d10bis;
    }

    /**
     * @return the d11
     */
    public DoubleSeq getD11() {
        return d11;
    }

    /**
     * @return the d11bis
     */
    public DoubleSeq getD11bis() {
        return d11bis;
    }

    /**
     * @return the d12
     */
    public DoubleSeq getD12() {
        return d12;
    }

    /**
     * @return the d13
     */
    public DoubleSeq getD13() {
        return d13;
    }

    /**
     * @return the d2drop
     */
    public int getD2drop() {
        return d2drop;
    }

}

