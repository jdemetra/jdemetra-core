/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
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
