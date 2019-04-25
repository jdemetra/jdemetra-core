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
package demetra.ssf.akf;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.IPredictionErrorDecomposition;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.IMultivariateSsfData;
import demetra.data.LogSign;
import demetra.maths.matrices.decomposition.ElementaryTransformations;
import demetra.ssf.likelihood.DiffuseLikelihood;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AugmentedPredictionErrorsDecomposition implements IPredictionErrorDecomposition, IMultivariateAugmentedFilteringResults {

    private double det;
    // Q is the cholesky factor copyOf the usual "Q matrix" copyOf De Jong.
    // Q(dj) = |S   -s|
    //         |-s'  q|
    // Q = |a 0|
    //     |b c|
    // so that we have:
    // q = b * b' + c * c
    // S = a * a' 
    // -s = a * b'
    // s' * S^-1 * s = b * a' * S^-1 * a * b' = b * b'
    // q - s' * S^-1 * s = c * c
    private FastMatrix Q, B;
    private int n, nd;
    
    /**
     *
     */
    public AugmentedPredictionErrorsDecomposition() {
    }

    /**
     *
     */
    @Override
    public void close() {
    }

    @Override
    public boolean canCollapse() {
        DataBlock diagonal = Q.diagonal();
        return diagonal.range(0, diagonal.length()-1).allMatch(x->x>=State.ZERO);
    }

    @Override
    public boolean collapse(AugmentedState state) {
        if (! canCollapse()) {
            return false;
        }

        // update the state vector
        B = state.B().deepClone();
        int d = B.getColumnsCount();
        FastMatrix S = a().deepClone();
        LowerTriangularMatrix.rsolve(S, B.transpose());
        DataBlock D = DataBlock.of(b());
        LowerTriangularMatrix.lsolve(S, D);
        for (int i = 0; i < d; ++i) {
            DataBlock col = B.column(i);
            state.a().addAY(-Q.get(d, i), col);
            state.P().addXaXt(1, col);
        }
        state.dropAllConstraints();
        return true;
    }

    public FastMatrix a() {
        return Q.extract(0, nd, 0, nd);
    }

    public DataBlock b() {
        return Q.row(nd).extract(0, nd);
    }

    public double c() {
        return Q.get(nd, nd);
    }
    
    /**
     * B*a^-1'
     * @return 
     */
    public FastMatrix B(){
        return B;
    }

    /**
     *
     * @param nd
     */
    private void prepare(final int nd, final int nvars) {
        this.det=0;
        this.n = 0;
        this.nd = nd;
        Q = FastMatrix.make(nd + 1, nd + 1+nvars);
    }

    @Override
    public void save(final int t, final MultivariateAugmentedUpdateInformation pe) {
        DataBlock U = pe.getTransformedPredictionErrors();
        FastMatrix L=pe.getCholeskyFactor();
        DataBlock D=L.diagonal();
        FastMatrix E = pe.E();
        int nvars=E.getColumnsCount();
        n+=nvars;
        LogSign sld = LogSign.of(D);
        det+=sld.getValue();
        Q.extract(0, nd, nd+1, nvars).copy(E);
        Q.row(nd).extract(nd+1, nvars).copy(U);
        ElementaryTransformations.fastGivensTriangularize(Q);
    }

    public FastMatrix getFinalQ() {
        return Q;
    }

    @Override
    public DiffuseLikelihood likelihood() {
        double cc = c();
        cc *= cc;
        LogSign dsl =LogSign.of(a().diagonal());
        double dcorr = 2 * dsl.getValue();
        return DiffuseLikelihood.builder(n, nd)
                .ssqErr(cc)
                .logDeterminant(2*det)
                .diffuseCorrection(dcorr)
                .build();
    }

    @Override
    public void open(IMultivariateSsf ssf, IMultivariateSsfData data) {
        prepare(ssf.getDiffuseDim(), ssf.measurements().getCount());
    }

    @Override
    public void save(int t, AugmentedState state, StateInfo info) {
        // nothing to do. We are just interested by the prediction error...
    }

}
