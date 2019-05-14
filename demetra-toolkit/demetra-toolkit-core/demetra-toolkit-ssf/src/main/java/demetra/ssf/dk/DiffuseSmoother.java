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
package demetra.ssf.dk;

import demetra.data.DataBlock;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.StateInfo;
import demetra.ssf.univariate.ISmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.OrdinarySmoother;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSmoother extends BaseDiffuseSmoother {

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

        public DiffuseSmoother build() {
            return new DiffuseSmoother(ssf, calcVariance, rescaleVariance);
        }
    }

    public static Builder builder(ISsf ssf) {
        return new Builder(ssf);
    }

    private DiffuseState state;
    private DefaultDiffuseFilteringResults frslts;

    private DiffuseSmoother(ISsf ssf, boolean calcvar, boolean rescalevar) {
        super(ssf, calcvar, rescalevar);
    }

    public boolean process(final ISsfData data, ISmoothingResults sresults) {
        DefaultDiffuseFilteringResults fresults = DkToolkit.filter(ssf, data, true);
        return process(data.length(), fresults, sresults);
    }

    public boolean process(final int endpos, DefaultDiffuseFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initSmoother();
        ordinarySmoothing(ssf, endpos);
        int t = frslts.getEndDiffusePosition();
        while (--t >= 0) {
            loadInfo(t);
            iterate(t);
            if (hasinfo) {
                srslts.save(t, state, StateInfo.Smoothed);
            }
        }
        if (rescalevar)
            srslts.rescaleVariances(frslts.var());
        return true;
    }

    private void initSmoother() {
        int dim = ssf.getStateDim();
        state = new DiffuseState(dim);

        Rf = DataBlock.make(dim);
        C = DataBlock.make(dim);
        Ri = DataBlock.make(dim);
        Ci = DataBlock.make(dim);

        if (calcvar) {
            tmp0 = DataBlock.make(dim);
            tmp1 = DataBlock.make(dim);
            Z = DataBlock.make(dim);
            N0 = CanonicalMatrix.square(dim);
            N1 = CanonicalMatrix.square(dim);
            N2 = CanonicalMatrix.square(dim);
        }
    }

    private void loadInfo(int pos) {
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
        DataBlock fa = frslts.a(pos);
        hasinfo = fa != null;
        if (!hasinfo) {
            return;
        }
        state.a().copy(fa);
        if (calcvar) {
            state.P().copy(frslts.P(pos));
            state.Pi().copy(frslts.Pi(pos));
            Z.set(0);
            loading.Z(pos, Z);
        }
    }

    @Override
    protected void updateA(int pos) {
        DataBlock a = state.a();
        if (calcvar) {
            a.addProduct(Rf, state.P().columnsIterator());
            a.addProduct(Ri, state.Pi().columnsIterator());
        } else { // to avoid unnecessary copies
            a.addProduct(Rf, frslts.P(pos).columnsIterator());
            a.addProduct(Ri, frslts.Pi(pos).columnsIterator());
        }
    }

    @Override
    protected void updateP(int pos) {
        CanonicalMatrix P = state.P();
        CanonicalMatrix PN0P = SymmetricMatrix.XtSX(N0, P);
        CanonicalMatrix Pi = state.Pi();
        CanonicalMatrix PN2P = SymmetricMatrix.XtSX(N2, Pi);
        CanonicalMatrix PN1 = P.times(N1);
        CanonicalMatrix PN1Pi = PN1.times(Pi);
        P.sub(PN0P);
        P.sub(PN2P);
        P.sub(PN1Pi);
        P.sub(PN1Pi.transpose());
        SymmetricMatrix.reenforceSymmetry(P);

    }

    private void ordinarySmoothing(ISsf ssf, final int end) {
        OrdinarySmoother smoother = OrdinarySmoother
                .builder(ssf)
                .calcVariance(calcvar)
                .rescaleVariance(false)
                .build();
        int beg = frslts.getEndDiffusePosition();
        smoother.process(beg, end, frslts, srslts);
        // updates R, N
        Rf.copy(smoother.getFinalR());
        if (calcvar) {
            N0.copy(smoother.getFinalN());
        }
    }

    public DefaultDiffuseFilteringResults getFilteringResults() {
        return frslts;
    }

}
