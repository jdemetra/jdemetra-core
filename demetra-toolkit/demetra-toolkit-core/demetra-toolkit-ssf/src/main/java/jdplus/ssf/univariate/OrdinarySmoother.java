/*
 * Copyright 2015 National Bank copyOf Belgium
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
package jdplus.ssf.univariate;

import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.State;
import jdplus.ssf.StateInfo;

/**
 *
 * @author Jean Palate
 */
public class OrdinarySmoother {

    public static class Builder {

        private final ISsf ssf;
        private boolean rescaleVariance = false;
        private boolean calcVariance = true;

        public Builder(ISsf ssf) {
            this.ssf = ssf;
        }

        public Builder rescaleVariance(boolean rescale) {
            this.rescaleVariance = rescale;
            if (rescale) {
                calcVariance = true;
            }
            return this;
        }

        public Builder calcVariance(boolean calc) {
            this.calcVariance = calc;
            if (!calc) {
                rescaleVariance = false;
            }
            return this;
        }

        public OrdinarySmoother build() {
            return new OrdinarySmoother(ssf, calcVariance, rescaleVariance);
        }
    }

    public static Builder builder(ISsf ssf) {
        return new Builder(ssf);
    }

    private final ISsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final boolean calcvar, rescalevar;
    private State state;
    private ISmoothingResults srslts;
    private DefaultFilteringResults frslts;

    private double err, errVariance, u, uVariance;
    private DataBlock M, R;
    private FastMatrix N;
    private boolean missing;
    private int stop;

    public OrdinarySmoother(ISsf ssf, boolean calcvar, boolean rescalevar) {
        this.ssf = ssf;
        this.calcvar = calcvar;
        this.rescalevar = rescalevar;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
    }

    public boolean process(ISsfData data) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fresults = DefaultFilteringResults.full();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(0, data.length(), fresults);
    }

    public boolean process(DefaultFilteringResults results) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        ResultsRange range = results.getRange();
        return process(range.getStart(), range.getEnd(), results);
    }

    public boolean process(int start, int end, DefaultFilteringResults results) {
        ISmoothingResults sresults;
        if (calcvar) {
            sresults = DefaultSmoothingResults.full();
        } else {
            sresults = DefaultSmoothingResults.light();
        }

        return process(start, end, results, sresults);
    }

    public boolean process(final int start, final int end, DefaultFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        stop = start;
        initSmoother(ssf);
        int t = end;
        while (--t >= stop) {
            loadInfo(t);
            if (iterate(t)) {
                srslts.save(t, state, StateInfo.Smoothed);
            }
        }

        return true;
    }

    public ISmoothingResults getResults() {
        return srslts;
    }

    public DataBlock getFinalR() {
        return R;
    }

    public FastMatrix getFinalN() {
        return N;
    }

    public double getFinalSmoothation() {
        return u;
    }

    public double getFinalSmoothationVariance() {
        return uVariance;
    }

    private void initSmoother(ISsf ssf) {
        int dim = ssf.getStateDim();
        state = new State(dim);

        R = DataBlock.make(dim);
        M = DataBlock.make(dim);

        if (calcvar) {
            N = FastMatrix.square(dim);
        }
    }

    private void loadInfo(int pos) {
        err = frslts.error(pos);
        errVariance = frslts.errorVariance(pos);
        M.copy(frslts.M(pos));
        missing = !Double.isFinite(err);
    }

    private boolean iterate(int pos) {
        iterateSmoothation(pos);
        iterateR(pos);
        if (calcvar) {
            iterateN(pos);
        }
        srslts.saveSmoothation(pos, u, uVariance);
        srslts.saveR(pos, R, N);
        DataBlock fa = frslts.a(pos);
        FastMatrix fP = frslts.P(pos);
        if (fP == null) {
            return false;
        }
        // a = a + r*P
        DataBlock a = state.a();
        a.copy(fa);
        a.addProduct(R, fP.columnsIterator());
        if (calcvar) {
            // P = P-PNP
            FastMatrix P = state.P();
            P.copy(fP);
            FastMatrix V = SymmetricMatrix.XtSX(N, P);
            P.sub(V);
        }
        return true;
    }
    // 

    private void iterateSmoothation(int pos) {
        if (missing) {
            u = Double.NaN;
            uVariance = Double.NaN;
            return;
        }
        // u = v(t)/f(t)-K'(t)*R(t)
        DataBlock k = M.deepClone();
        dynamics.TX(pos, k);
        k.div(errVariance);
        if (errVariance != 0) {
            u = err / errVariance - R.dot(k);
            // apply the same to the colums of Rd
            if (calcvar) {
                // uvar = 1/f(t)+ K'NK
                // = 1/f + 1/f*M'T'*N*T*M/f
                uVariance = 1 / errVariance + QuadraticForm.apply(N, k);
                if (uVariance < Constants.getEpsilon()) {
                    uVariance = 0;
                }
//                if (uVariance == 0) {
//                    if (Math.abs(u) < State.ZERO) {
//                        u = 0;
//                    } else {
//                        throw new SsfException(SsfException.INCONSISTENT);
//                    }
//                }
            }
        } else {
            u = 0;
            uVariance = Double.NaN;
        }
    }

    /**
     *
     */
    private void iterateN(int pos) {
        if (!missing && errVariance != 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            // L = T-KZ
            // N(t-1) = Z'(t)*Z(t)/f(t) + (T'(t)-Z'K')*N(t)*(T(t)-KZ)
            // Z'(t)*Z(t)(1/f(t)+K'N(t)K) + T'NT - Z'K'N(t) - NK'Z'
            ssf.XL(pos, N, M, errVariance);
            ssf.XtL(pos, N, M, errVariance);
            loading.VpZdZ(pos, N, 1 / errVariance);
        } else {
            //T'*N(t)*T
            dynamics.MT(pos, N);
            dynamics.TtM(pos, N);
        }
        SymmetricMatrix.reenforceSymmetry(N);
        N.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

    /**
     *
     */
    private void iterateR(int pos) {
        // R(t-1)=u(t)Z(t)+R(t)T'(t)
        dynamics.XT(pos, R);
        if (!missing && u != 0) {
            // RT
            loading.XpZd(pos, R, u);
        }
        R.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

}
