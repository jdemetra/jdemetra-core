/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats;

import demetra.stats.StatException;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class StatUtility {

    /**
     * Theil's inequality coefficient, also known as Theil's U, provides a
     * measure of how well
     * a sequence compares to another sequence.
     *
     * @param a The first sequence
     * @param b The second sequence
     * @return Higher value means less comparable sequences
     */
    public double theilInequalityCoefficient(DoubleSeq a, DoubleSeq b) {
        int n = a.length();
        if (b.length() != n) {
            throw new StatException("Non compatible data");
        }
        double dssq = 0, assq = 0, bssq = 0;
        DoubleSeqCursor acur = a.cursor();
        DoubleSeqCursor bcur = b.cursor();
        for (int i = 0; i < n; ++i) {
            double ca = acur.getAndNext(), cb = bcur.getAndNext();
            assq += ca * ca;
            bssq += cb * cb;
            double del = ca - cb;
            dssq += del * del;
        }
        if (dssq == 0) {
            return 0;
        }
        assq /= n;
        bssq /= n;
        dssq /= n;
        return Math.sqrt(dssq) / (Math.sqrt(assq) + Math.sqrt(bssq));
    }
}
