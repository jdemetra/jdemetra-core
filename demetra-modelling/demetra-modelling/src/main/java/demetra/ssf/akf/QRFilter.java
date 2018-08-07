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
import demetra.data.LogSign;
import demetra.likelihood.DeterminantalTerm;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ResultsRange;
import demetra.ssf.univariate.DefaultFilteringResults;
import demetra.ssf.univariate.FastFilter;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.OrdinaryFilter;
import demetra.data.DoubleSequence;

/**
 * QR variant copyOf the augmented Kalman filter. See for instance
 * Gomez-Maravall. This implementation doesn't use collapsing
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilter {

    private ProfileLikelihood pll;
    private MarginalLikelihood mll;
    private DiffuseLikelihood dll;
    private Matrix R, X, Xl;
    private DataBlock yl, b;
    private double ldet, ssq, dcorr, pcorr, mcorr;

    /**
     *
     */
    public QRFilter() {
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data) {
        clear();
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fr = DefaultFilteringResults.light();
        fr.prepare(ssf, 0, data.length());
        if (!filter.process(ssf, data, fr)) {
            return false;
        }
        DeterminantalTerm det = new DeterminantalTerm();
        DoubleSequence vars = fr.errorVariances();
        for (int i = 0; i < vars.length(); ++i) {
            double v = vars.get(i);
            if (v != 0) {
                det.add(v);
            }
        }
        ldet = det.getLogDeterminant();

        // apply the filter on the diffuse effects
        ISsfDynamics dynamics = ssf.dynamics();
        X = Matrix.make(data.length(), ssf.getDiffuseDim());
        ssf.diffuseEffects(X);
        yl = DataBlock.of(fr.errors(true, true));
        FastFilter ffilter = new FastFilter(ssf, fr, new ResultsRange(0, data.length()));
        int n = ffilter.getOutputLength(X.getRowsCount());
        Xl = Matrix.make(n, X.getColumnsCount());
        for (int i = 0; i < X.getColumnsCount(); ++i) {
            ffilter.transform(X.column(i), Xl.column(i));
        }
        return true;
    }

    private void calcMLL() {
        if (dll == null) {
            calcDLL();
        }

        Householder housx = new Householder();
        housx.decompose(X);
        mcorr = 2 * LogSign.of(housx.rdiagonal(true)).getValue();
        int nd = housx.rank(), n = Xl.getRowsCount();

        mll = new MarginalLikelihood();
        mll.set(ssq, ldet, dcorr, mcorr, n, nd);
    }

    private void calcDLL() {
        Householder hous = new Householder(false);
        hous.decompose(Xl);
        b = DataBlock.make(hous.rank());
        int nd = b.length(), n = Xl.getRowsCount();
        DataBlock e = DataBlock.make(n - nd);
        hous.leastSquares(yl, b, e);
        ssq = e.ssq();
        dcorr = 2 * LogSign.of(hous.rdiagonal(true)).getValue();
        R = hous.r(true);
        dll = new DiffuseLikelihood();
        dll.set(ssq, ldet, dcorr, n, nd);
    }

    private void calcPLL() {
        if (dll == null) {
            calcDLL();
        }

        int n = Xl.getRowsCount();
        Matrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        bvar.mul(ssq / n);
        pll = new ProfileLikelihood();
        pll.set(ssq, ldet, b, bvar, n);

    }

    private void clear() {
        ssq = 0;
        ldet = 0;
        dcorr = 0;
        pcorr = 0;
        mcorr = 0;
        mll = null;
        pll = null;
        X = null;
        Xl = null;
        yl = null;
        R = null;
    }

    public ProfileLikelihood getProfileLikelihood() {
        if (pll == null) {
            calcPLL();
        }
        return pll;
    }

    public MarginalLikelihood getMarginalLikelihood() {
        if (mll == null) {
            calcMLL();
        }
        return mll;
    }

    public DiffuseLikelihood getDiffuseLikelihood() {
        if (dll == null) {
            calcDLL();
        }
        return dll;
    }
}
