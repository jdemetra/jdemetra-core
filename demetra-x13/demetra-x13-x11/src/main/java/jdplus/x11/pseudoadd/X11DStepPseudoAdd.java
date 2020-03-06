/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11.pseudoadd;

import demetra.data.DoubleSeq;
import jdplus.x11.X11Context;
import jdplus.x11.X11DStep;
import jdplus.x11.filter.MsrFilterSelection;

/**
 *
 * @author Nina Gonschorreck
 */
public class X11DStepPseudoAdd extends X11DStep {

    private final DoubleSeq c7, c13, c20;

    public X11DStepPseudoAdd(DoubleSeq c7, DoubleSeq c13, DoubleSeq c20) {
        super();
        this.c7 = c7;
        this.c13 = c13;
        this.c20 = c20;
    }

    @Override
    protected DoubleSeq d11bis(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjusted(this.getD1(), this.getD10(), this.getD7());
    }

    @Override
    protected DoubleSeq d11(X11Context context) {
        DoubleSeq d10 = this.getD10(), d12 = this.getD12();
        DoubleSeq d10b = DoubleSeq.onMapping(d10.length(), i -> d12.get(i) * (d10.get(i) - 1));
        return PseudoAddUtility.removeSeasonalAdjustedFinal(this.getRefSeries(), d10b);
    }

    @Override
    protected DoubleSeq d8(X11Context context) {
        DoubleSeq tmp = context.remove(this.getD1(), this.getD7());
        return DoubleSeq.onMapping(tmp.length(), i -> tmp.get(i) * c20.get(i));
        // No solution available: theoretical, wrong for PseudoAdd
    }

    @Override
    protected MsrFilterSelection getMsrFilterSelection() {
        return new MsrFilterSelectionPseudoAdd();
    }

    @Override
    protected DoubleSeq d6(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjustedWithBorders(this.getD1(), this.getD5(), this.getD2(), this.getD2drop());
    }

    @Override
    protected DoubleSeq d1(X11Context context, DoubleSeq input) {
        return PseudoAddUtility.adjustRefSeries(this.getRefSeries(), input, c7, c13);
    }

}
