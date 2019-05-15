/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
 /*
 */
package demetra.ssf.univariate;

import demetra.ssf.UpdateInformation;
import jd.data.DataBlock;
import demetra.ssf.DataBlockResults;
import demetra.ssf.DataResults;
import demetra.ssf.IStateResults;
import demetra.ssf.MatrixResults;
import demetra.ssf.ResultsRange;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.likelihood.DeterminantalTerm;
import demetra.data.DoubleSeq;
import jd.maths.matrices.FastMatrix;

/**
 * Will contain the following items at position t: a(t|t-1)
 * e(t)=y(t)-Z(t)a(t|t-1), f(t) C(t)=Z(t)P(t|t-1) [P(t|t-1)], optional
 *
 * @author Jean Palate
 */
public class DefaultFilteringResults implements IFilteringResults, IStateResults {

    private final DataBlockResults A; // state vector
    private final MatrixResults P;  // P
    private final DataBlockResults C; // C = P*Z'
    private final DataResults e, f; // errors, variances copyOf the errors
    private final ResultsRange range = new ResultsRange();

    protected DefaultFilteringResults(boolean cov) {
        A = new DataBlockResults();
        C = new DataBlockResults();
        P = cov ? new MatrixResults() : null;
        e = new DataResults();
        f = new DataResults();
    }

    public boolean isInitialized() {
        return A.isInitialized();
    }

    public ResultsRange getRange() {
        return range;
    }

    public static DefaultFilteringResults full() {
        return new DefaultFilteringResults(true);
    }

    public static DefaultFilteringResults light() {
        return new DefaultFilteringResults(false);
    }

    public void prepare(ISsf ssf, final int start, final int end) {
        int dim = ssf.getStateDim();

        A.prepare(dim, start, end);
        C.prepare(dim, start, end);
        e.prepare(start, end);
        f.prepare(start, end);
        if (P != null) {
            P.prepare(dim, start, end);
        }
    }

    @Override
    public void save(int t, UpdateInformation pe) {
        e.save(t, pe.get());
        f.save(t, pe.getVariance());
        C.save(t, pe.M());
    }

    @Override
    public void save(final int t, final State state, final StateInfo info) {
        if (info !=StateInfo.Forecast){
            return;
        }
        A.save(t, state.a());
        if (P != null) {
            P.save(t, state.P());
        }
        range.add(t);
    }

    public double error(int pos) {
        return e.get(pos);
    }

    public double errorVariance(int pos) {
        return f.get(pos);
    }

    public DoubleSeq errors(boolean normalized, boolean clean) {
        DataBlock r = e.all();
        if (normalized) {
            r = DataBlock.of(r);
            DataBlock allf = f.all();
            r.apply(allf, (x, y) -> Double.isFinite(x) && Double.isFinite(y) ? x / Math.sqrt(y) : Double.NaN);
        }
        if (clean){
            r=DataBlock.select(r, (x)->Double.isFinite(x));
        }
        return r;
    }

    public DataBlock getComponent(int pos) {
        return A.item(pos);
    }

    public DataBlock getComponentVariance(int pos) {
        return P.item(pos, pos);
    }

    public DoubleSeq errors() {
        return e.asDoublesReader(true);
    }

    public DoubleSeq errorVariances() {
        return f.asDoublesReader(true);
    }

    public DataBlock a(int pos) {
        return A.datablock(pos);
    }

    public DataBlock M(int pos) {
        return C.datablock(pos);
    }

    public FastMatrix P(int pos) {
        return P.matrix(pos);
    }

    @Override
    public void clear() {
        e.clear();
        f.clear();
        A.clear();
        C.clear();
        if (P != null) {
            P.clear();
        }
        range.clear();
    }
    
    public int size(){
        return e.getLength();
    }

    public double var() {
        int m = 0;
        double ssq = 0;
        int n=e.getLength();
        for (int i = 0; i < n; ++i) {
            double e = error(i);
            if (Double.isFinite(e)) {
                ++m;
                ssq += e * e / errorVariance(i);
            }
        }
        return ssq / m;
    }

    public double logDeterminant() {
        DeterminantalTerm det = new DeterminantalTerm();
        int n=e.getLength();
        for (int i = 0; i < n; ++i) {
            if (Double.isFinite(error(i))) {
                double e = errorVariance(i);
                if (e > State.ZERO) {
                    det.add(e);
                }
            }
        }
        return det.getLogDeterminant();

    }
    
}
