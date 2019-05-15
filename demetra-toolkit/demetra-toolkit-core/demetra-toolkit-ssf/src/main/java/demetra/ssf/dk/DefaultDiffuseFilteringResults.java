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
package demetra.ssf.dk;

import demetra.ssf.univariate.ISsf;
import demetra.ssf.MatrixResults;
import demetra.ssf.StateInfo;
import demetra.ssf.ISsfInitialization;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultDiffuseFilteringResults extends BaseDiffuseFilteringResults{

    private final MatrixResults Pi;

    private DefaultDiffuseFilteringResults(boolean var) {
        super(var);
        Pi = var ? new MatrixResults() : null;
    }

    public static DefaultDiffuseFilteringResults full() {
        return new DefaultDiffuseFilteringResults(true);
    }

    public static DefaultDiffuseFilteringResults light() {
        return new DefaultDiffuseFilteringResults(false);
    }
    
    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim(), n = initialization.getDiffuseDim();
        if (Pi != null) {
            Pi.prepare(dim, start, n);
        }
    }

    @Override
    public void save(final int t, final DiffuseState state, final StateInfo info) {
        if (info != StateInfo.Forecast) {
            return;
        }
        super.save(t, state, info);
        if (Pi != null) {
            Pi.save(t, state.Pi());
        }
    }

    public FastMatrix Pi(int pos) {
        return Pi.matrix(pos);
    }

    @Override
    public void clear() {
        super.clear();
        Pi.clear();
    }
}
