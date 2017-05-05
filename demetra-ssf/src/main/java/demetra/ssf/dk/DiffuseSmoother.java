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
import demetra.maths.matrices.Matrix;
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
public class DiffuseSmoother extends BaseDiffuseSmoother{

    private DiffuseState state;
    private IDiffuseFilteringResults frslts;

    public boolean process(final ISsf ssf, final ISsfData data, ISmoothingResults sresults) {
        IDiffuseFilteringResults fresults = DkToolkit.filter(ssf, data, true);
        return process(ssf, data.length(), fresults, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, IDiffuseFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf, endpos);
        ordinarySmoothing(ssf, endpos);
        int t=frslts.getEndDiffusePosition();
        while (--t >= 0) {
            loadInfo(t);
            iterate(t);
            if (hasinfo) {
                srslts.save(t, state, StateInfo.Smoothed);
            }
        }
        return true;
    }

    private void initSmoother(ISsf ssf, int endpos) {
        int dim = ssf.getStateDim();
        state = new DiffuseState(dim);
 
        Rf = DataBlock.make(dim);
        C = DataBlock.make(dim);
        Ri = DataBlock.make(dim);
        Ci = DataBlock.make(dim);

        if (calcvar) {
            tmp0 = DataBlock.make(dim);
            tmp1 = DataBlock.make(dim);
            N0 = Matrix.square(dim);
            N1 = Matrix.square(dim);
            N2 = Matrix.square(dim);
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
        Matrix P = state.P();
        Matrix PN0P = SymmetricMatrix.XtSX(N0, P);
        Matrix Pi = state.Pi();
        Matrix PN2P = SymmetricMatrix.XtSX(N2, Pi);
        Matrix PN1 = P.times(N1);
        Matrix PN1Pi = PN1.times(Pi);
        P.sub(PN0P);
        P.sub(PN2P);
        P.sub(PN1Pi);
        P.sub(PN1Pi.transpose());
        SymmetricMatrix.reenforceSymmetry(P);

    }

    @Override
    protected void initFilter(ISsf ssf) {
        dynamics = ssf.getDynamics();
        measurement = ssf.getMeasurement();
    }

    private void ordinarySmoothing(ISsf ssf, final int end) {
        OrdinarySmoother smoother = new OrdinarySmoother();
        smoother.setCalcVariances(calcvar);
        int beg=frslts.getEndDiffusePosition();
        smoother.process(ssf, beg, end, frslts, srslts);
        // updates R, N
        Rf.copy(smoother.getFinalR());
        if (calcvar) {
            N0.copy(smoother.getFinalN());
        }
    }

        public IDiffuseFilteringResults getFilteringResults() {
        return frslts;
      }

}
