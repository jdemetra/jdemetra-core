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

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11BStep {

    /**
     * @return the b4a
     */
    public DoubleSequence getB4a() {
        return b4a;
    }

    /**
     * @return the b4anorm
     */
    public DoubleSequence getB4anorm() {
        return b4anorm;
    }

    /**
     * @return the b4d
     */
    public DoubleSequence getB4d() {
        return b4d;
    }

    /**
     * @return the b4g
     */
    public DoubleSequence getB4g() {
        return b4g;
    }

    /**
     * @return the b9g
     */
    public DoubleSequence getB9g() {
        return b9g;
    }

    private DoubleSequence b1, b2, b3, b4, b4a, b4anorm, b4d, b4g, b5, b6,
            b7, b8, b9, b9g, b10, b11, b13, b17, b20;
    private int b2drop;

    public X11BStep() {
    }

    public void process(DoubleSequence input, X11Context context) {
        b1 = input;
        b2(context);
        b3(context);
        b4(context);
        b5(context);
        b6(context);
        b7(context);
        b8(context);
        b9(context);
        bfinal(context);
    }

    private void b2(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        b2drop = filter.length() / 2;

        double[] x = table(b1.length() - 2 * b2drop, Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, 0, x.length);
        filter.apply(i -> b1.get(i), IFilterOutput.of(out, b2drop));
        b2 = DoubleSequence.ofInternal(x);
    }

    private void b3(X11Context context) {
        b3 = context.remove(b1.drop(b2drop, b2drop), b2);
    }

    private void b4(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        b4a = filter.process(b3);
        b4anorm=DefaultSeasonalNormalizer.normalize(b4a, 0, context);
        b4d=context.remove(b3, b4anorm);
        
        DefaultExtremeValuesCorrector ecorr=new DefaultExtremeValuesCorrector();
        ecorr.setStart(b2drop);
        ecorr.analyse(b4d, context);

        b4 = ecorr.computeCorrections(b3);
        b4g = ecorr.applyCorrections(b3, b4);
    }
    
    private void b5(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSequence b5a = filter.process(b4g);
        b5=DefaultSeasonalNormalizer.normalize(b5a, b2drop, context);
    }
    
    private void b6(X11Context context) {
        b6 = context.remove(b1, b5);
    }
    
    private void b7(X11Context context) {
        SymmetricFilter filter = context.trendFilter();
        int ndrop = filter.length() / 2;

        double[] x = table(b6.length(), Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, ndrop, x.length-ndrop);
        filter.apply(i -> b6.get(i), IFilterOutput.of(out, ndrop));
        
        // apply asymmetric filters
        double r=MusgraveFilterFactory.findR(filter.length(), context.getPeriod().intValue());
        IFiniteFilter[] lf = context.leftAsymmetricTrendFilters(filter, r); 
        IFiniteFilter[] rf = context.rightAsymmetricTrendFilters(filter, r); 
        AsymmetricEndPoints aep=new AsymmetricEndPoints(lf, -1);
        aep.process(b6, DataBlock.ofInternal(x));
        aep=new AsymmetricEndPoints(rf, 1);
        aep.process(b6, DataBlock.ofInternal(x));
        b7 = DoubleSequence.ofInternal(x);
        if (b7.anyMatch(z->z <=0))
            throw new X11Exception(X11Exception.ERR_NEG);
    }
    
    private void b8(X11Context context) {
        b8=context.remove(b1, b7);
    }
    
    private void b9(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSequence b9a = filter.process(b8);
        DoubleSequence b9c=DefaultSeasonalNormalizer.normalize(b9a, 0, context);
        DoubleSequence b9d=context.remove(b8, b9c);
        DefaultExtremeValuesCorrector ecorr=new DefaultExtremeValuesCorrector();
        ecorr.setStart(0);
        ecorr.analyse(b9d, context);

        b9 = ecorr.computeCorrections(b8);
        b9g = ecorr.applyCorrections(b8, b9);
    }

    private void bfinal(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSequence b10a = filter.process(b9g);
        b10=DefaultSeasonalNormalizer.normalize(b10a, 0, context);
        b11=context.remove(b1, b10);
        b13=context.remove(b11, b7);
        
        DefaultExtremeValuesCorrector ecorr=new DefaultExtremeValuesCorrector();
        ecorr.setStart(0);
        ecorr.analyse(b13, context);
        b17 = ecorr.getObservationWeights();
        b20 = ecorr.getCorrectionFactors();
   }


    /**
     * @return the b1
     */
    public DoubleSequence getB1() {
        return b1;
    }

    /**
     * @return the b2
     */
    public DoubleSequence getB2() {
        return b2;
    }

    /**
     * @return the b3
     */
    public DoubleSequence getB3() {
        return b3;
    }

    public DoubleSequence getB4() {
        return b4;
    }

    /**
     * @return the b5
     */
    public DoubleSequence getB5() {
        return b5;
    }

    /**
     * @return the b6
     */
    public DoubleSequence getB6() {
        return b6;
    }

    /**
     * @return the b7
     */
    public DoubleSequence getB7() {
        return b7;
    }

    /**
     * @return the b8
     */
    public DoubleSequence getB8() {
        return b8;
    }

    /**
     * @return the b9
     */
    public DoubleSequence getB9() {
        return b9;
    }

    /**
     * @return the b10
     */
    public DoubleSequence getB10() {
        return b10;
    }

    /**
     * @return the b11
     */
    public DoubleSequence getB11() {
        return b11;
    }

    /**
     * @return the b13
     */
    public DoubleSequence getB13() {
        return b13;
    }

    /**
     * @return the b19
     */
    public DoubleSequence getB17() {
        return b17;
    }

    /**
     * @return the b20
     */
    public DoubleSequence getB20() {
        return b20;
    }

    /**
     * @param b20 the b20 to set
     */
    public void setB20(DoubleSequence b20) {
        this.b20 = b20;
    }
}
