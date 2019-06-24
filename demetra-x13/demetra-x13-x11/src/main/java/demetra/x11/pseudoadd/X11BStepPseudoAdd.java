/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.pseudoadd;

import demetra.data.DoubleSequence;
import demetra.x11.X11BStep;
import demetra.x11.X11Context;

/**
 *
 * @author Nina Gonschorreck
 */
public class X11BStepPseudoAdd extends X11BStep {

    @Override
    protected DoubleSequence b11(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjusted(this.getB1(), this.getB10(), this.getB7());
    }

    @Override
    protected DoubleSequence b9d(X11Context context, DoubleSequence b9c) {
        return PseudoAddUtility.removeIrregular(this.getB8(), b9c);
    }

    @Override
    protected DoubleSequence b6(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjustedWithBorders(this.getB1(), this.getB5(), this.getB2(), this.getB2drop()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected DoubleSequence b4d(X11Context context) {
        return PseudoAddUtility.removeIrregular(this.getB3(), this.getB4anorm());
    }
}
