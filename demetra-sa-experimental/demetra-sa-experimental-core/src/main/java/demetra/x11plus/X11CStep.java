/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11plus;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import static demetra.x11plus.X11Kernel.table;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11CStep {

    private DoubleSeq refSeries, c1, c2, c4, c5, c6, c7, c9, c10, c11, c12, c13, c17, c20;
    private int c2drop;

    public void process(DoubleSeq refSeries, DoubleSeq input, X11Context context) {
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
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(c1, out);
        c2 = DoubleSeq.of(x);
    }

    private void c5(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSeq c5a = filter.process(c4);
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
        DataBlock out = DataBlock.of(x, ndrop, x.length-ndrop);
        filter.apply(c6, out);
        
       // apply asymmetric filters
        double r=MusgraveFilterFactory.findR(filter.length(), context.getPeriod().intValue());
        IFiniteFilter[] lf = context.leftAsymmetricTrendFilters(filter, r); 
        IFiniteFilter[] rf = context.rightAsymmetricTrendFilters(filter, r); 
        AsymmetricEndPoints aep=new AsymmetricEndPoints(lf, -1);
        aep.process(c6, DataBlock.ofInternal(x));
        aep=new AsymmetricEndPoints(rf, 1);
        aep.process(c6, DataBlock.ofInternal(x));
        c7 = DoubleSeq.of(x);
        if (c7.anyMatch(z->z <=0))
            throw new X11Exception(X11Exception.ERR_NEG);
    }
    
    private void c9(X11Context context) {
        c9=context.remove(c1, c7);
    }

    private void cfinal(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSeq c10a = filter.process(c9);
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
    public DoubleSeq getC1() {
        return c1;
    }

    /**
     * @return the c2
     */
    public DoubleSeq getC2() {
        return c2;
    }

    /**
     * @return the c4
     */
    public DoubleSeq getC4() {
        return c4;
    }

    /**
     * @return the C5
     */
    public DoubleSeq getC5() {
        return c5;
    }

    /**
     * @return the c10
     */
    public DoubleSeq getC10() {
        return c10;
    }

    /**
     * @return the c11
     */
    public DoubleSeq getC11() {
        return c11;
    }

    /**
     * @return the c12
     */
    public DoubleSeq getC12() {
        return c12;
    }

    /**
     * @return the c13
     */
    public DoubleSeq getC13() {
        return c13;
    }

    /**
     * @return the c6
     */
    public DoubleSeq getC6() {
        return c6;
    }

    /**
     * @return the c7
     */
    public DoubleSeq getC7() {
        return c7;
    }

    /**
     * @return the c9
     */
    public DoubleSeq getC9() {
        return c9;
    }

    /**
     * @return the c17
     */
    public DoubleSeq getC17() {
        return c17;
    }

    /**
     * @return the c20
     */
    public DoubleSeq getC20() {
        return c20;
    }

}

