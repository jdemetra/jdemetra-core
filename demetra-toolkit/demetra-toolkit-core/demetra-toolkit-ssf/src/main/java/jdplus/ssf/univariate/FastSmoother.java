/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.univariate;

import jdplus.ssf.ISsfLoading;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfDynamics;
import demetra.data.DoubleSeqCursor;
import jdplus.data.DataBlockIterator;
import jdplus.ssf.DataBlockResults;

/**
 *
 * @author Jean Palate
 */
public class FastSmoother {

    public static interface VarianceFilterProvider {

        int size();

        boolean isMissing(int pos);

        double errorVariance(int pos);

        DataBlock M(int pos);

        FastMatrix P(int pos);

        public static VarianceFilterProvider of(final DefaultFilteringResults fr) {
            return new VarianceFilterProvider() {
                @Override
                public int size() {
                    return fr.size();
                }

                @Override
                public boolean isMissing(int pos) {
                    return fr.isMissing(pos);
                }

                @Override
                public double errorVariance(int pos) {
                    return fr.errorVariance(pos);
                }

                @Override
                public DataBlock M(int pos) {
                    return fr.M(pos);
                }

                @Override
                public FastMatrix P(int pos) {
                    return fr.P(pos);
                }
            };
        }
    }
    private final VarianceFilterProvider vf;
    private final ISsf ssf;
    private final ISsfLoading loading;
    private final ISsfDynamics dynamics;
    private final DataBlock state;
   private final DataBlock R, C;
    private final DataBlockResults A = new DataBlockResults();

    public FastSmoother(ISsf ssf, DefaultFilteringResults frslts) {
        this.ssf = ssf;
        this.vf = VarianceFilterProvider.of(frslts);
        loading = ssf.measurement().loading();
        dynamics = ssf.dynamics();
        int dim = ssf.getStateDim();
        state = DataBlock.make(dim);
        R = DataBlock.make(dim);
        C = DataBlock.make(dim);
       A.prepare(dim, 0, vf.size());
    }
    
    public void smooth(FastMatrix X){
        DataBlockIterator cols = X.columnsIterator();
        while (cols.hasNext())
            smooth(cols.next());
    }
    
    public void smooth(DataBlock x) {
        forwardFilter(x);
        backwardFilter(x);
    }

    void forwardFilter(DataBlock x) {
        int n = vf.size();
        int pos = 0;
        // initialize the state
        ssf.initialization().a0(state);
        DoubleSeqCursor.OnMutable cursor = x.cursor();
        while (pos < n) {
            int cur = pos++;
            cursor.applyAndNext(z -> iterateFilter(cur, z));
        }
    }
    
    private double iterateFilter(int i, double x) {
        // save the current state
        A.save(i, state);
        // retrieve the current information
        boolean missing = vf.isMissing(i);
        double fx = x - loading.ZX(i, state);
        // update the state, if not missing
        if (!missing) {
            double f = vf.errorVariance(i);
            if (f > 0) {
                // update the state
                DataBlock C = vf.M(i);
                state.addAY(fx / f, C);
            }
        }
        dynamics.TX(i, state);
        return fx;
    }
    
    void backwardFilter(DataBlock x){
         int t = x.length();
        while (--t >= 0) {
                x.set(t, iterateSmoother(t, x.get(t)));
         }
    }

    private double iterateSmoother(int pos, double fx) {
        double f = vf.errorVariance(pos);
        C.copy(vf.M(pos));
        C.mul(1 / f);
        boolean missing = vf.isMissing(pos);
        dynamics.XT(pos, R);
        if (!missing) {
            double u = fx / f - R.dot(C);
            loading.XpZd(pos, R, u);
        }
        state.copy(A.datablock(pos));
        state.addProduct(R, vf.P(pos).columnsIterator());
        return loading.ZX(pos, state);
    }
}
