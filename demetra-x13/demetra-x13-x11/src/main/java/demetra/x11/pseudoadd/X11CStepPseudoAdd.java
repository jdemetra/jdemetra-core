/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.pseudoadd;

import demetra.data.DoubleSequence;
import demetra.x11.X11CStep;
import demetra.x11.X11Context;

/**
 *
 * @author Nina Gonschorreck
 */
public class X11CStepPseudoAdd extends X11CStep {

    private final DoubleSequence b7, b13;

    public X11CStepPseudoAdd(DoubleSequence b7, DoubleSequence b13) {
        super();
        this.b7 = b7;
        this.b13 = b13;
    }

    @Override
    protected DoubleSequence c11(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjusted(this.getRefSeries(), this.getC10(), this.getC7());
    }

    @Override
    protected DoubleSequence c6(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjustedWithBorders(this.getC1(), this.getC5(), this.getC2(), this.getC2drop()); 
    }

    @Override
    protected DoubleSequence c1(X11Context context, DoubleSequence input) {
        return PseudoAddUtility.adjustRefSeries(this.getRefSeries(), input, b7, b13);
    }

}
