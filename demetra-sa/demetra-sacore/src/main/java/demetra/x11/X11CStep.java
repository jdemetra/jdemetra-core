/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.HendersonFilters;
import demetra.maths.linearfilters.IFilterOutput;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import static demetra.x11.X11Kernel.table;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11CStep {

    private DoubleSequence refSeries, c1, c2, c4, c5, c6, c7, c9, c10, c11, c12, c13, c17, c20;
    private int c2drop;

    public void process(DoubleSequence refSeries, DoubleSequence input, X11Context context) {
        this.refSeries=refSeries;
        c1 = input;
        c2(context);
        c4(context);
        c5(context);
        c6(context);
        c7(context);
        c9(context);
        cfinal(context);
    }

    private void c2(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        c2drop = filter.length() / 2;

        double[] x = table(c1.length() - 2 * c2drop, Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, 0, x.length);
        filter.apply(i -> c1.get(i), IFilterOutput.of(out, c2drop));
        c2 = DoubleSequence.ofInternal(x);
    }

    private void c5(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSequence c5a = filter.process(c4);
        c5 = DefaultSeasonalNormalizer.normalize(c5a, c2drop, context);
    }

    private void c4(X11Context context) {
        c4 = context.remove(c1.drop(c2drop, c2drop), c2);
    }

    private void c6(X11Context context) {
        c6 = context.remove(c1, c5);
    }

    private void c7(X11Context context) {
        SymmetricFilter filter = context.trendFilter();
        int ndrop = filter.length() / 2;

        double[] x = table(c6.length(), Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, ndrop, x.length-ndrop);
        filter.apply(i -> c6.get(i), IFilterOutput.of(out, ndrop));
        
       // apply asymmetric filters
        double r=MusgraveFilterFactory.findR(filter.length(), context.getPeriod().intValue());
        IFiniteFilter[] lf = context.leftAsymmetricTrendFilters(filter, r); 
        IFiniteFilter[] rf = context.rightAsymmetricTrendFilters(filter, r); 
        AsymmetricEndPoints aep=new AsymmetricEndPoints(lf, -1);
        aep.process(c6, DataBlock.ofInternal(x));
        aep=new AsymmetricEndPoints(rf, 1);
        aep.process(c6, DataBlock.ofInternal(x));
        c7 = DoubleSequence.ofInternal(x);
        if (c7.anyMatch(z->z <=0))
            throw new X11Exception(X11Exception.ERR_NEG);
    }
    
    private void c9(X11Context context) {
        c9=context.remove(c1, c7);
    }

    private void cfinal(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSequence c10a = filter.process(c9);
        c10=DefaultSeasonalNormalizer.normalize(c10a, 0, context);
        c11=context.remove(refSeries, c10);
        c13=context.remove(c11, c7);
        
        DefaultExtremeValuesCorrector ecorr=new DefaultExtremeValuesCorrector();
        ecorr.setStart(0);
        ecorr.analyse(c13, context);
        c17 = ecorr.getObservationWeights();
        c20 = ecorr.getCorrectionFactors();
    }

    /**
     * @return the c1
     */
    public DoubleSequence getC1() {
        return c1;
    }

    /**
     * @return the c2
     */
    public DoubleSequence getC2() {
        return c2;
    }

    /**
     * @return the c4
     */
    public DoubleSequence getC4() {
        return c4;
    }

    /**
     * @return the C5
     */
    public DoubleSequence getC5() {
        return c5;
    }

    /**
     * @return the c10
     */
    public DoubleSequence getC10() {
        return c10;
    }

    /**
     * @return the c11
     */
    public DoubleSequence getC11() {
        return c11;
    }

    /**
     * @return the c12
     */
    public DoubleSequence getC12() {
        return c12;
    }

    /**
     * @return the c13
     */
    public DoubleSequence getC13() {
        return c13;
    }

    /**
     * @return the c6
     */
    public DoubleSequence getC6() {
        return c6;
    }

    /**
     * @return the c7
     */
    public DoubleSequence getC7() {
        return c7;
    }

    /**
     * @return the c9
     */
    public DoubleSequence getC9() {
        return c9;
    }

    /**
     * @return the c17
     */
    public DoubleSequence getC17() {
        return c17;
    }

    /**
     * @return the c20
     */
    public DoubleSequence getC20() {
        return c20;
    }

}

