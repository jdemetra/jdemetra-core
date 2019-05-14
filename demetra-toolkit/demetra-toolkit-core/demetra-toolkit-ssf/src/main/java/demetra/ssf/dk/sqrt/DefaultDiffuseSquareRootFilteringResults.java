/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.dk.sqrt;

import demetra.data.DataBlock;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.DefaultFilteringResults;
import demetra.ssf.dk.DiffuseUpdateInformation;
import demetra.ssf.DataBlockResults;
import demetra.ssf.DataBlocksResults;
import demetra.ssf.DataResults;
import demetra.ssf.StateInfo;
import demetra.ssf.akf.AugmentedState;
import demetra.likelihood.DeterminantalTerm;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.dk.BaseDiffuseFilteringResults;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.SubMatrix;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultDiffuseSquareRootFilteringResults extends BaseDiffuseFilteringResults implements IDiffuseSquareRootFilteringResults {

    private final DataBlocksResults B;

    private DefaultDiffuseSquareRootFilteringResults(boolean var) {
        super(var);
        B = var ? new DataBlocksResults() : null;
    }

    public static DefaultDiffuseSquareRootFilteringResults full() {
        return new DefaultDiffuseSquareRootFilteringResults(true);
    }

    public static DefaultDiffuseSquareRootFilteringResults light() {
        return new DefaultDiffuseSquareRootFilteringResults(false);
    }

    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim(), n = initialization.getDiffuseDim();
        if (B != null) {
            B.prepare(dim, n, n);
        }
    }

    @Override
    public void save(final int t, final AugmentedState state, final StateInfo info) {
        if (info != StateInfo.Forecast) {
            return;
        }
        super.save(t, state, info);
        if (B != null) {
            B.save(t, state.B());
        }

    }

    public SubMatrix B(int pos) {
        return B.matrix(pos);
    }

    @Override
    public void clear() {
        super.clear();
        B.clear();
    }

}
