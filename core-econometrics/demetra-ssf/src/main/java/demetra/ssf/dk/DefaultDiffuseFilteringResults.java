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

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.DefaultFilteringResults;
import demetra.ssf.DataBlockResults;
import demetra.ssf.DataResults;
import demetra.ssf.MatrixResults;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.data.DoubleSequence;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author Jean Palate
 */
public class DefaultDiffuseFilteringResults extends DefaultFilteringResults implements IDiffuseFilteringResults {

    private final DataBlockResults Ci;
    private final MatrixResults Pi;
    private final DataResults fi;
    private int enddiffuse;

    private DefaultDiffuseFilteringResults(boolean var) {
        super(var);
        Ci = new DataBlockResults();
        fi=new DataResults();
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
        ISsfInitialization initialization = ssf.getInitialization();
        int dim = initialization.getStateDim(), n = initialization.getDiffuseDim();
        fi.prepare(start, n);
        Ci.prepare(dim, start, n);
        if (Pi != null) {
            Pi.prepare(dim, start, n);
        }
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        super.save(t, pe);
        fi.save(t, pe.getDiffuseVariance());
        Ci.save(t, pe.Mi());
    }

    @Override
    public void close(int pos) {
        enddiffuse = pos;
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

    @Override
    public DoubleSequence errors(boolean normalized, boolean clean) {
        DataBlock r = DataBlock.of(errors());
        // set diffuse elements to Double.NaN
        r.range(0, enddiffuse).apply(fi.extract(0, enddiffuse), (x,y)->y!=0 ? Double.NaN : x);
        if (normalized) {
            DoubleSequence allf = errorVariances();
            r.apply(allf, (x, y) -> Double.isFinite(x) && Double.isFinite(y) ? x / Math.sqrt(y) : Double.NaN);
        }
        if (clean){
            r=DataBlock.select(r, (x)->Double.isFinite(x));
        }
        return r;
    }

    @Override
    public double diffuseNorm2(int pos) {
        return fi.get(pos);
    }

    @Override
    public DataBlock Mi(int pos) {
        return Ci.datablock(pos);
    }
 
    @Override
    public Matrix Pi(int pos) {
        return Pi.matrix(pos);
    }

    @Override
    public void clear() {
        super.clear();
        Ci.clear();
        fi.clear();
        Pi.clear();
        enddiffuse = 0;
    }

    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }
}
