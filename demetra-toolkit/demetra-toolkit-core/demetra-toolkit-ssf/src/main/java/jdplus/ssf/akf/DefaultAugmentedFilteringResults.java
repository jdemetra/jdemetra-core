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
package jdplus.ssf.akf;

import jdplus.data.DataBlock;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.DefaultFilteringResults;
import jdplus.ssf.DataBlockResults;
import jdplus.ssf.MatrixResults;
import jdplus.ssf.StateInfo;
import jdplus.ssf.ISsfInitialization;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultAugmentedFilteringResults extends DefaultFilteringResults implements IAugmentedFilteringResults {

    private final MatrixResults B;
    private final DataBlockResults E;
    private int collapsed;
    private final QAugmentation Q = new QAugmentation();

    private DefaultAugmentedFilteringResults(boolean var) {
        super(var);
        B = new MatrixResults();
        E = new DataBlockResults();
    }

    public static DefaultAugmentedFilteringResults full() {
        return new DefaultAugmentedFilteringResults(true);
    }

    public static DefaultAugmentedFilteringResults light() {
        return new DefaultAugmentedFilteringResults(false);
    }

    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim(), n = initialization.getDiffuseDim();
        B.prepare(dim, n, 0, n);
        E.prepare(n, 0, n);
        Q.prepare(n, 1);
    }

    @Override
    public void save(int t, AugmentedUpdateInformation pe) {
        super.save(t, pe);
        E.save(t, pe.E());
        Q.update(pe);
    }

    @Override
    public void close(int pos) {
    }

    @Override
    public void save(final int t, final AugmentedState state, final StateInfo info) {
        if (info != StateInfo.Forecast) {
            return;
        }
        super.save(t, state, info);
        B.save(t, state.B());
    }

    @Override
    public FastMatrix B(int pos) {
        return B.matrix(pos);
    }

    @Override
    public DataBlock E(int pos) {
        return E.datablock(pos);
    }

    @Override
    public void clear() {
        super.clear();
        collapsed = 0;
        B.clear();
    }

    @Override
    public int getCollapsingPosition() {
        return collapsed;
    }

    @Override
    public QAugmentation getAugmentation() {
        return Q;
    }

    @Override
    public boolean canCollapse() {
        return Q.canCollapse();
    }

    @Override
    public boolean collapse(int pos, AugmentedState state) {
        if (Q.collapse(state)) {
            collapsed = pos;
            return true;
        } else {
            return false;
        }
    }

}
