/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11.pseudoadd;

import demetra.data.DoubleSeq;

/**
 *
 * @author Nina Gonschorreck
 */
public class PseudoAddUtility {

    public static DoubleSeq removeIrregular(DoubleSeq l, DoubleSeq r) {
        return DoubleSeq.onMapping(l.length(), i -> l.get(i) - r.get(i) + 1);
    }

    public static DoubleSeq removeSeasonalAdjusted(DoubleSeq l, DoubleSeq r, DoubleSeq m) {
        return DoubleSeq.onMapping(l.length(), i -> l.get(i) - m.get(i) * (r.get(i) - 1));
    }

    public static DoubleSeq removeSeasonalAdjustedWithBorders(DoubleSeq l, DoubleSeq r, DoubleSeq m, int drop) {
        DoubleSeq adjusted = removeSeasonalAdjusted(l.drop(drop, drop), r.drop(drop, drop), m);
        double[] borders = DoubleSeq.onMapping(l.length(), i -> l.get(i) / r.get(i)).toArray();

        for (int i = drop; i < borders.length - drop; i++) {
            borders[i] = adjusted.get(i - drop);
        }

        return DoubleSeq.of(borders);
    }

    public static DoubleSeq removeSeasonalAdjustedFinal(DoubleSeq l, DoubleSeq r) {
        return DoubleSeq.onMapping(l.length(), i -> l.get(i) - r.get(i));
    }

    public static DoubleSeq adjustRefSeries(DoubleSeq l, DoubleSeq r, DoubleSeq m, DoubleSeq d) {
        return DoubleSeq.onMapping(l.length(), i -> l.get(i) + m.get(i) * (d.get(i) / r.get(i) - d.get(i)));
    }

    public static DoubleSeq calcIrregular(DoubleSeq l, DoubleSeq r) {
        return DoubleSeq.onMapping(l.length(), i -> l.get(i) / r.get(i));
    }
}
/*
 * d - divers - Irregular
 * m - middle - trend
 * l - left - refSeries
 * r - right - correction
 */
