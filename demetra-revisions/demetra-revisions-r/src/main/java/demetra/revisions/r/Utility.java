/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.revisions.r;

import demetra.data.DoubleSeqCursor;
import demetra.math.matrices.MatrixType;
import demetra.revisions.parametric.Bias;
import demetra.revisions.parametric.RegressionBasedAnalysis;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Utility {

    public MatrixType biasInformation(RegressionBasedAnalysis analysis) {
        int nrevs = analysis.getRevisionBiases().size();
        Matrix all = Matrix.make(nrevs + 1, 9);
        informationOf(analysis.getCurrentBias(), all.row(0));
        int row = 0;
        for (Bias bias : analysis.getRevisionBiases()) {
            informationOf(bias, all.row(++row));
        }
        return all.unmodifiable();
    }

    void informationOf(Bias bias, DataBlock buffer) {
        if (bias == null) {
            return;
        }
        DoubleSeqCursor.OnMutable cursor = buffer.cursor();
        cursor.setAndNext(bias.getN());
        cursor.setAndNext(bias.getMu());
        cursor.setAndNext(bias.getSigma());
        cursor.setAndNext(bias.getT());
        cursor.setAndNext(bias.getTPvalue());
        cursor.setAndNext(bias.getAr());
        cursor.setAndNext(bias.getAdjustedSigma());
        cursor.setAndNext(bias.getAdjustedT());
        cursor.setAndNext(bias.getAdjustedTPvalue());
    }
}
