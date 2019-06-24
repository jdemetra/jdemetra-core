/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.pseudoadd;

import demetra.data.DoubleSequence;
import demetra.x11.X11Context;
import demetra.x11.filter.MsrFilterSelection;

/**
 *
 * @author Nina Gonschorreck
 */
public class MsrFilterSelectionPseudoAdd extends MsrFilterSelection {

    @Override
    protected DoubleSequence calcIrregular(X11Context context, DoubleSequence series, DoubleSequence seas) {
        return PseudoAddUtility.removeIrregular(series, seas);
    }

}
