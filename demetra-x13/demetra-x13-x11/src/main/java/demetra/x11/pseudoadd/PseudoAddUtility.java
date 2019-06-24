/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.pseudoadd;

import demetra.data.DoubleSequence;

/**
 *
 * @author Nina Gonschorreck
 */
public class PseudoAddUtility {

    public static DoubleSequence removeIrregular(DoubleSequence l, DoubleSequence r) {
        return DoubleSequence.onMapping(l.length(), i -> l.get(i) - r.get(i) + 1);
    }

    public static DoubleSequence removeSeasonalAdjusted(DoubleSequence l, DoubleSequence r, DoubleSequence m) {
        return DoubleSequence.onMapping(l.length(), i -> l.get(i) - m.get(i) * (r.get(i) - 1));
    }

    public static DoubleSequence removeSeasonalAdjustedWithBorders(DoubleSequence l, DoubleSequence r, DoubleSequence m, int drop) {
        DoubleSequence adjusted = removeSeasonalAdjusted(l.drop(drop, drop), r.drop(drop, drop), m);
        double[] borders = DoubleSequence.onMapping(l.length(), i -> l.get(i) / r.get(i)).toArray();

        for (int i = drop; i < borders.length - drop; i++) {
            borders[i] = adjusted.get(i - drop);
        }

        return DoubleSequence.ofInternal(borders);
    }

    public static DoubleSequence removeSeasonalAdjustedFinal(DoubleSequence l, DoubleSequence r) {
        return DoubleSequence.onMapping(l.length(), i -> l.get(i) - r.get(i));
    }

    public static DoubleSequence adjustRefSeries(DoubleSequence l, DoubleSequence r, DoubleSequence m, DoubleSequence d) {
        return DoubleSequence.onMapping(l.length(), i -> l.get(i) + m.get(i) * (d.get(i) / r.get(i) - d.get(i)));
    }

    public static DoubleSequence calcIrregular(DoubleSequence l, DoubleSequence r) {
        return DoubleSequence.onMapping(l.length(), i -> l.get(i) / r.get(i));
    }
}
/*
d - divers - Irregular
m - middle - trend
l - left - refSeries
r - right - correction
*/
