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
package demetra.ssf.dk.sqrt;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.StateInfo;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.BaseDiffuseSmoother;
import demetra.ssf.akf.AugmentedState;
import demetra.ssf.univariate.ISmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.OrdinarySmoother;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSquareRootSmoother extends BaseDiffuseSmoother{

    private AugmentedState state;
    private IDiffuseSquareRootFilteringResults frslts;

    public boolean process(final ISsf ssf, final ISsfData data, ISmoothingResults sresults) {
        IDiffuseSquareRootFilteringResults fresults = DkToolkit.sqrtFilter(ssf, data, true);
        return process(ssf, data.length(), fresults, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, IDiffuseSquareRootFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf, endpos);
        ordinarySmoothing(ssf, endpos);
        int t=frslts.getEndDiffusePosition();
        while (--t >= 0) {
            loadInfo(t);
            iterate(t);
            if (srslts != null)
            srslts.save(t, state, StateInfo.Smoothed);
        }
        return true;
    }

    private void initSmoother(ISsf ssf, int end) {
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
            N0 = Matrix.square(dim);
            N1 = Matrix.square(dim);
            N2 = Matrix.square(dim);
            Z = DataBlock.make(dim);
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
        state.a().copy(frslts.a(pos));
        if (calcvar) {
            loading.Z(pos, Z);
            state.P().copy(frslts.P(pos));
            Matrix B = frslts.B(pos);
            state.restoreB(B);
        }
    }

    // 

    @Override
    protected void updateA(int pos) {
        DataBlock a = state.a();
        if (calcvar) {
            a.addProduct(Rf, state.P().columnsIterator());
            // Pi=B*B'
            DataBlock tmp = DataBlock.make(state.getDiffuseDim());
            tmp.product(Ri, state.B().columnsIterator());
            a.addProduct(tmp, state.B().rowsIterator());
        } else { // to avoid unnecessary copies
            a.addProduct(Rf, frslts.P(pos).columnsIterator());
            Matrix B = frslts.B(pos);
            DataBlock tmp =  DataBlock.make(B.getColumnsCount());
            tmp.product(Ri, B.columnsIterator());
            a.addProduct(tmp, B.rowsIterator());
        }
    }

    @Override
    protected void updateP(int pos) {
        Matrix P = state.P();
        Matrix PN0P = SymmetricMatrix.XtSX(N0, P);
        Matrix BN2B = SymmetricMatrix.XtSX(N2,state.B());
        Matrix PN2P=SymmetricMatrix.XSXt(BN2B, state.B());
        Matrix N1B= Matrix.make(N1.getRowsCount(), state.B().getColumnsCount());
        N1B.product(N1, state.B());
        Matrix PN1B=P.times(N1B);
        Matrix PN1Pi=Matrix.square(P.getRowsCount());
        PN1Pi.product(PN1B, state.B().transpose());
//        Matrix PN2P = SymmetricMatrix.quadraticForm(N2, Pi);
//        Matrix PN1 = P.times(N1);
//        Matrix PN1Pi = PN1.times(Pi);
        P.sub(PN0P);
        P.sub(PN2P);
        P.sub(PN1Pi);
        P.sub(PN1Pi.transpose());
        SymmetricMatrix.reenforceSymmetry(P);
     }

    private void ordinarySmoothing(ISsf ssf, final int endpos) {
        OrdinarySmoother smoother = new OrdinarySmoother();
        smoother.setCalcVariances(calcvar);
        smoother.process(ssf, frslts.getEndDiffusePosition(), endpos, frslts, srslts);
        // updates R, N
        Rf.copy(smoother.getFinalR());
        if (calcvar) {
            N0.copy(smoother.getFinalN());
        }
    }

    public IDiffuseSquareRootFilteringResults getFilteringResults() {
        return frslts;
      }

}
