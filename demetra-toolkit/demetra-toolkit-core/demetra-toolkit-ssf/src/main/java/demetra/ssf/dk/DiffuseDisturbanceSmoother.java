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
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.univariate.DisturbanceSmoother;
import demetra.ssf.univariate.IDisturbanceSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.data.DoubleSeqCursor;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.ISsfLoading;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
public class DiffuseDisturbanceSmoother {

    public static class Builder{
        private final ISsf ssf;
        private boolean rescaleVariance=false;
        private boolean calcVariance=true;
        
        public Builder(ISsf ssf){
            this.ssf=ssf;
        }
        
        public Builder rescaleVariance(boolean rescale){
            this.rescaleVariance=rescale;
            if (rescale)
                calcVariance=true;
            return this;
        }

        public Builder calcVariance(boolean calc){
            this.calcVariance=calc;
            if (! calc)
                rescaleVariance=false;
            return this;
        }
        
        public DiffuseDisturbanceSmoother build(){
            return new DiffuseDisturbanceSmoother(ssf, calcVariance, rescaleVariance);
        }
    }
    
    public static Builder builder(ISsf ssf){
        return new Builder(ssf);
    }

    private final ISsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final ISsfError error;
    private final boolean calcvar, rescalevar;
    private IDisturbanceSmoothingResults srslts;
    private BaseDiffuseFilteringResults frslts;

    private double e, f, esm, esmVariance, h, fi;
    private DataBlock C, Ci, R, Ri, U;
    private FastMatrix N, UVar, S;
    private boolean missing;
    private int pos;
    // temporary
    private DataBlock tmp;
    private double c, v;

    private DiffuseDisturbanceSmoother(ISsf ssf, boolean calcvar, boolean rescalevar){
        this.ssf=ssf;
        this.calcvar=calcvar;
        this.rescalevar=rescalevar;
        dynamics = ssf.dynamics();
        loading = ssf.measurement().loading();
        error = ssf.measurement().error();
    }

    /**
     *
     * @param ssf
     * @param data
     * @param sresults Smoothing results. The caller is responsible of preparing
     * them!
     * @return
     */
    public boolean process(final ISsfData data, @Nonnull IDisturbanceSmoothingResults sresults) {
        BaseDiffuseFilteringResults fresults = DkToolkit.sqrtFilter(ssf, data, false);
        // rescale the variances
        return process(data.length(), fresults, sresults);
    }

    /**
     *
     * @param endpos
     * @param results The filtering results should contain the necessary
     * information for the smoothing
     * @param sresults Smoothing results. The caller is responsible of preparing
     * them!
     * @return
     */
    public boolean process(final int endpos, BaseDiffuseFilteringResults results, @Nonnull IDisturbanceSmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initSmoother(ssf);
        ordinarySmoothing(ssf, endpos);
        pos = frslts.getEndDiffusePosition();
        while (--pos >= 0) {
            loadInfo();
            if (iterate()) {
                srslts.saveSmoothedTransitionDisturbances(pos, U, UVar);
                if (error != null) {
                    srslts.saveSmoothedMeasurementDisturbance(pos, esm, esmVariance);
                }
            }
        }
        if (rescalevar)
            srslts.rescaleVariances(frslts.var());
        return true;
    }

    private void initSmoother(ISsf ssf) {
        int dim = ssf.getStateDim();
        int resdim = dynamics.getInnovationsDim();

        R = DataBlock.make(dim);
        C = DataBlock.make(dim);
        Ri = DataBlock.make(dim);
        Ci = DataBlock.make(dim);
        U = DataBlock.make(resdim);
        S = FastMatrix.make(dim, resdim);
        if (calcvar) {
            N = FastMatrix.square(dim);
            tmp = DataBlock.make(dim);
            UVar = FastMatrix.square(resdim);
            if (error != null && error.isTimeInvariant()) {
                h = error.at(0);
            } else {
                h = 0;
            }
        }
        if (dynamics.isTimeInvariant()) {
            dynamics.S(0, S);
        }
    }

    private void loadInfo() {
        e = frslts.error(pos);
        f = frslts.errorVariance(pos);
        fi = frslts.diffuseNorm2(pos);
        C.copy(frslts.M(pos));
        if (fi != 0) {
            Ci.copy(frslts.Mi(pos));
            Ci.mul(1 / fi);
            C.addAY(-f, Ci);
            C.mul(1 / fi);
        } else {
            C.mul(1 / f);
            Ci.set(0);
        }
        missing = !Double.isFinite(e);
        if (pos > 0) {
            if (!dynamics.isTimeInvariant() && S != null) {
                S.set(0);
                dynamics.S(pos - 1, S);
            }
            if (error != null && !error.isTimeInvariant()) {
                h = error.at(pos - 1);
            }
        }
    }

    private boolean iterate() {
        iterateR();
        if (calcvar) {
            iterateN();
        }
        // updates the smoothed disturbances
        if (pos > 0) {
            // updates the smoothed disturbances
            esm = c * h;
            if (dynamics.hasInnovations(pos - 1)) {
                dynamics.XS(pos - 1, R, U);
                if (calcvar) {
                    esmVariance = h - h * h * v;
                    // v(U) = I-S'NS
                    SymmetricMatrix.XtSX(N, S, UVar);
                    UVar.chs();
                    UVar.diagonal().add(1);
                }
            } else {
                U.set(0);
                if (calcvar) {
                    UVar.set(0);
                }
            }
        }
        return true;
    }

    /**
     *
     */
    private void iterateN() {
        if (missing || (f == 0 && fi == 0)) {
            iterateMissingN();
        } else if (fi == 0) {
            iterateRegularN();
        } else {
            iterateDiffuseN();
        }
        SymmetricMatrix.reenforceSymmetry(N);
    }

    private void iterateMissingN() {
        tvt(N);
    }

    private void iterateRegularN() {
        // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
        tvt(N);
        tmp.product(C, N.columnsIterator());
        // 2. v
        v = 1 / f + tmp.dot(C);
        loading.VpZdZ(pos, N, v);
        subZ(N.rowsIterator(), tmp);
        subZ(N.columnsIterator(), tmp);
    }

    private void iterateDiffuseN() {
        tvt(N);
        tmp.product(Ci, N.columnsIterator());
        // 2. v
        v = tmp.dot(Ci);
        loading.VpZdZ(pos, N, v);
        subZ(N.rowsIterator(), tmp);
        subZ(N.columnsIterator(), tmp);
    }

    /**
     *
     */
    private void iterateR() {
        if (fi == 0) {
            iterateRegularR();
        } else {
            iterateDiffuseR();
        }
    }

    private void iterateRegularR() {
        // R(t-1)=v(t)/f(t)Z(t)+R(t)L(t)
        //   = v/f*Z + R*(T-TC/f*Z)
        //  = (v - RT*C)/f*Z + RT
        dynamics.XT(pos, R);
        dynamics.XT(pos, Ri);
        if (!missing && f != 0) {
            // RT
            c = e / f - R.dot(C);
            loading.XpZd(pos, R, c);
        }
    }

    private void iterateDiffuseR() {
        dynamics.XT(pos, R);
        dynamics.XT(pos, Ri);
        if (!missing && fi != 0) {
            c = -Ri.dot(Ci);
            // Ri(t-1)=c*Z(t) +Ri(t)*T(t)
            // c = e/fi-(Ri(t)*T(t)*Ci(t))/fi-(Rf(t)*T(t)*Cf(t))/f
            double ci = e / fi + c - R.dot(C);
            loading.XpZd(pos, Ri, ci);
            // Rf(t-1)=c*Z(t)+Rf(t)*T(t)
            // c =  - Rf(t)T(t)*Ci/fi
            double cf = -R.dot(Ci);
            loading.XpZd(pos, R, cf);
        }
    }

    private void tvt(FastMatrix N) {
        DataBlockIterator columns = N.columnsIterator();
        while (columns.hasNext()) {
            dynamics.XT(pos, columns.next());
        }
        DataBlockIterator rows = N.rowsIterator();
        while (rows.hasNext()) {
            dynamics.XT(pos, rows.next());
        }
    }

    private void subZ(DataBlockIterator rows, DataBlock b) {
        DoubleSeqCursor cell = b.cursor();
        while (rows.hasNext()) {
            loading.XpZd(pos, rows.next(), -cell.getAndNext());
        }
    }

    private void ordinarySmoothing(ISsf ssf, final int endpos) {
        // Remark: rescaling is processed on the final results
        DisturbanceSmoother smoother = DisturbanceSmoother.builder(ssf)
                .calcVariance(calcvar)
                .rescaleVariance(false)
                .build();
         smoother.process(frslts.getEndDiffusePosition(), endpos, frslts, srslts);
        // updates R, N
        R.copy(smoother.getFinalR());
        if (calcvar) {
            N.copy(smoother.getFinalN());
        }
    }

    public IBaseDiffuseFilteringResults getFilteringResults() {
        return frslts;
    }

    public DataBlock getFinalR() {
        return R;
    }

    public DataBlock getFinalRi() {
        return Ri;
    }

    public FastMatrix getFinalN() {
        return N;
    }

    public DataBlock firstSmoothedState() {

        ISsfInitialization initialization = ssf.initialization();
        int n = initialization.getStateDim();
        // initial state
        DataBlock a = DataBlock.make(n);
        FastMatrix Pf0 = FastMatrix.square(n);
        initialization.a0(a);
        initialization.Pf0(Pf0);
        // stationary initialization
        a.addProduct(R, Pf0.columnsIterator());

        // non stationary initialisation
        FastMatrix Pi0 = FastMatrix.square(n);
        initialization.Pi0(Pi0);
        a.addProduct(Ri, Pi0.columnsIterator());
        return a;
    }
}
