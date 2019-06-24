/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.pseudoadd;

import demetra.data.DoubleSequence;
import demetra.x11.X11Context;
import demetra.x11.X11DStep;
import demetra.x11.filter.MsrFilterSelection;

/**
 *
 * @author Nina Gonschorreck
 */
public class X11DStepPseudoAdd extends X11DStep {

    private final DoubleSequence c7, c13, c20;

    public X11DStepPseudoAdd(DoubleSequence c7, DoubleSequence c13, DoubleSequence c20) {
        super();
        this.c7 = c7;
        this.c13 = c13;
        this.c20 = c20;
    }

    @Override
    protected DoubleSequence d11bis(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjusted(this.getD1(), this.getD10(), this.getD7());
    }

    @Override
    protected DoubleSequence d11(X11Context context) {
        DoubleSequence d10 = this.getD10(), d12 = this.getD12();
        DoubleSequence d10b = DoubleSequence.onMapping(d10.length(), i -> d12.get(i) * (d10.get(i) - 1));
        return PseudoAddUtility.removeSeasonalAdjustedFinal(this.getRefSeries(), d10b);
    }

    @Override
    protected DoubleSequence d8(X11Context context) {
        DoubleSequence tmp = context.remove(this.getD1(), this.getD7());
        return DoubleSequence.onMapping(tmp.length(), i -> tmp.get(i) * c20.get(i));
        // No solution available: theoretical, wrong for PseudoAdd
    }

    @Override
    protected MsrFilterSelection getMsrFilterSelection() {
        return new MsrFilterSelectionPseudoAdd();
    }

    @Override
    protected DoubleSequence d6(X11Context context) {
        return PseudoAddUtility.removeSeasonalAdjustedWithBorders(this.getD1(), this.getD5(), this.getD2(), this.getD2drop());
    }

    @Override
    protected DoubleSequence d1(X11Context context, DoubleSequence input) {
        return PseudoAddUtility.adjustRefSeries(this.getRefSeries(), input, c7, c13);
    }

}
