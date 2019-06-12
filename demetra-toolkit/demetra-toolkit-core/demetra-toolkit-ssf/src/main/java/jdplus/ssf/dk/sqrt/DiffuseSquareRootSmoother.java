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
package jdplus.ssf.dk.sqrt;

import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SubMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.StateInfo;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.BaseDiffuseSmoother;
import jdplus.ssf.akf.AugmentedState;
import jdplus.ssf.univariate.ISmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinarySmoother;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSquareRootSmoother extends BaseDiffuseSmoother {

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

        public DiffuseSquareRootSmoother build() {
            return new DiffuseSquareRootSmoother(ssf, calcVariance, rescaleVariance);
        }
    }

    public static Builder builder(ISsf ssf) {
        return new Builder(ssf);
    }

    private AugmentedState state;
    private DefaultDiffuseSquareRootFilteringResults frslts;

    private DiffuseSquareRootSmoother(ISsf ssf, boolean calcvar, boolean rescalevar) {
        super(ssf, calcvar, rescalevar);
    }

    public boolean process(final ISsfData data, ISmoothingResults sresults) {
        DefaultDiffuseSquareRootFilteringResults fresults = DkToolkit.sqrtFilter(ssf, data, true);
        return process(data.length(), fresults, sresults);
    }

    public boolean process(final int endpos, DefaultDiffuseSquareRootFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initSmoother();
        ordinarySmoothing(ssf, endpos);
        int t = frslts.getEndDiffusePosition();
        while (--t >= 0) {
            loadInfo(t);
            iterate(t);
            if (srslts != null) {
                srslts.save(t, state, StateInfo.Smoothed);
            }
        }
        if (rescalevar) {
            srslts.rescaleVariances(frslts.var());
        }
        return true;
    }

    private void initSmoother() {
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim();
        state = new AugmentedState(dim, initialization.getDiffuseDim());

        Rf = DataBlock.make(dim);
        C = DataBlock.make(dim);
        Ri = DataBlock.make(dim);
        Ci = DataBlock.make(dim);

        if (calcvar) {
            tmp0 = DataBlock.make(dim);
            tmp1 = DataBlock.make(dim);
            N0 = CanonicalMatrix.square(dim);
            N1 = CanonicalMatrix.square(dim);
            N2 = CanonicalMatrix.square(dim);
            Z = DataBlock.make(dim);
            if (loading.isTimeInvariant()) {
                Z.set(0);
                loading.Z(0, Z);
            }
        }
    }

    private void loadInfo(int pos) {
        e = frslts.error(pos);
        f = frslts.errorVariance(pos);
        fi = frslts.diffuseNorm2(pos);
        missing = !Double.isFinite(e);
        state.a().copy(frslts.a(pos));
        if (!missing) {
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
        } else {
            e = 0;
        }
        if (calcvar) {
            if (!loading.isTimeInvariant()) {
                Z.set(0);
                loading.Z(pos, Z);
            }
            state.P().copy(frslts.P(pos));
            state.restoreB(frslts.B(pos));
        }
    }

    // 
    @Override
    protected void updateA(int pos) {
        DataBlock a = state.a();
        a.addProduct(Rf, frslts.P(pos).columnsIterator());
        SubMatrix B = frslts.B(pos);
        DataBlock tmp = DataBlock.make(B.getColumnsCount());
        tmp.product(Ri, B.columnsIterator());
        a.addProduct(tmp, B.rowsIterator());
    }

    @Override
    protected void updateP(int pos) {
        // V = Pf - Pf * N0 * Pf - < Pi * N1 * Pf > - Pi * N2 * Pi
        // Pi = B*B'
        // ! N1 is not a symmetric matrix
        CanonicalMatrix P = state.P();
        CanonicalMatrix PN0P = SymmetricMatrix.XtSX(N0, P);
        CanonicalMatrix BN2B = SymmetricMatrix.XtSX(N2, state.B());
        CanonicalMatrix PN2P = SymmetricMatrix.XSXt(BN2B, state.B());
        CanonicalMatrix N1B = CanonicalMatrix.make(N1.getRowsCount(), state.B().getColumnsCount());
        N1B.product(N1, state.B());
        CanonicalMatrix PN1B = P.times(N1B);
        CanonicalMatrix PN1Pi = CanonicalMatrix.square(P.getRowsCount());
        PN1Pi.product(PN1B, state.B().transpose());
//        Matrix PN2P = SymmetricMatrix.quadraticForm(N2, Pi);
//        Matrix PN1 = P.times(N1);
//        Matrix PN1Pi = PN1.times(Pi);
        PN0P.add(PN2P);
        PN0P.add(PN1Pi);
        PN0P.add(PN1Pi.transpose());
        SymmetricMatrix.reenforceSymmetry(PN0P);
        P.sub(PN0P);
    }

    private void ordinarySmoothing(ISsf ssf, final int endpos) {
        OrdinarySmoother smoother = OrdinarySmoother
                .builder(ssf)
                .calcVariance(calcvar)
                .rescaleVariance(false)
                .build();
        smoother.process(frslts.getEndDiffusePosition(), endpos, frslts, srslts);
        // updates R, N
        Rf.copy(smoother.getFinalR());
        if (calcvar) {
            N0.copy(smoother.getFinalN());
        }
    }

    public DefaultDiffuseSquareRootFilteringResults getFilteringResults() {
        return frslts;
    }

}
