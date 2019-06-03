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

import demetra.ssf.likelihood.ProfileLikelihood;
import demetra.ssf.likelihood.MarginalLikelihood;
import jdplus.data.DataBlock;
import demetra.data.LogSign;
import demetra.likelihood.DeterminantalTerm;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.matrices.UpperTriangularMatrix;
import jdplus.maths.matrices.decomposition.Householder;
import demetra.ssf.ResultsRange;
import demetra.ssf.univariate.DefaultFilteringResults;
import demetra.ssf.univariate.FastFilter;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.OrdinaryFilter;
import demetra.ssf.likelihood.DiffuseLikelihood;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.CanonicalMatrix;

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
    private ISsfData o;
    private CanonicalMatrix R, X, Xl;
    private DataBlock yl, b, e;
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
        this.o = data;
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fr = DefaultFilteringResults.light();
        fr.prepare(ssf, 0, data.length());
        if (!filter.process(ssf, data, fr)) {
            return false;
        }
        DeterminantalTerm det = new DeterminantalTerm();
        DoubleSeq vars = fr.errorVariances();
        for (int i = 0; i < vars.length(); ++i) {
            double v = vars.get(i);
            if (v != 0) {
                det.add(v);
            }
        }
        ldet = det.getLogDeterminant();

        // apply the filter on the diffuse effects
        X = CanonicalMatrix.make(data.length(), ssf.getDiffuseDim());
        ssf.diffuseEffects(X);
        yl = DataBlock.of(fr.errors(true, true));
        FastFilter ffilter = new FastFilter(ssf, fr, new ResultsRange(0, data.length()));
        int n = ffilter.getOutputLength(X.getRowsCount());
        Xl = CanonicalMatrix.make(n, X.getColumnsCount());
        for (int i = 0; i < X.getColumnsCount(); ++i) {
            ffilter.apply(X.column(i), Xl.column(i));
        }
        return true;
    }

    public static MarginalLikelihood ml(final ISsf ssf, final ISsfData data, boolean concentrated) {
        AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(true);
        pe.prepare(ssf, data.length());
        AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(pe);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        if (!filter.process(ssf, data, pe))
            return null;
        int collapsing = pe.getCollapsingPosition();
        DiffuseLikelihood likelihood = pe.likelihood();
        CanonicalMatrix M = CanonicalMatrix.make(collapsing, ssf.getDiffuseDim());
        ssf.diffuseEffects(M);
        int j = 0;
        for (int i = 0; i < collapsing; ++i) {
            if (!data.isMissing(i)) {
                if (i > j) {
                    M.row(j).copy(M.row(i));
                }
                j++;
            }
        }
        Householder hous = new Householder();
        hous.decompose(M.extract(0, j, 0, M.getColumnsCount()));
        double mc = 2 * LogSign.of(hous.rdiagonal(true)).getValue();
        return MarginalLikelihood.builder(likelihood.dim(), likelihood.getD())
                .concentratedScalingFactor(concentrated)
                .diffuseCorrection(likelihood.getDiffuseCorrection())
                .legacy(false)
                .logDeterminant(likelihood.logDeterminant())
                .ssqErr(likelihood.ssq())
                .residuals(pe.errors(true, true))
                .marginalCorrection(mc)
                .build();
    }

    private void calcMLL() {
        if (dll == null) {
            calcDLL();
        }

        Householder housx = new Householder();
        CanonicalMatrix Q = X;
        if (X.getRowsCount() != Xl.getRowsCount()) {
            Q = CanonicalMatrix.make(Xl.getRowsCount(), X.getColumnsCount());
            for (int i = 0, j = 0; i < o.length(); ++i) {
                if (!o.isMissing(i)) {
                    Q.row(j++).copy(X.row(i));
                }
            }
        }
        housx.decompose(Q);
        mcorr = 2 * LogSign.of(housx.rdiagonal(true)).getValue();
        int nd = housx.rank(), n = Xl.getRowsCount();

        mll = MarginalLikelihood.builder(n, nd)
                .ssqErr(ssq)
                .logDeterminant(ldet)
                .diffuseCorrection(dcorr)
                .marginalCorrection(mcorr)
                .residuals(e)
                .build();
    }

    private void calcDLL() {
        Householder hous = new Householder(false);
        hous.decompose(Xl);
        b = DataBlock.make(hous.rank());
        int nd = b.length(), n = Xl.getRowsCount();
        e = DataBlock.make(n - nd);
        hous.leastSquares(yl, b, e);
        ssq = e.ssq();
        dcorr = 2 * LogSign.of(hous.rdiagonal(true)).getValue();
        R = hous.r(true);
        dll = DiffuseLikelihood.builder(n, nd)
                .ssqErr(ssq)
                .logDeterminant(ldet)
                .diffuseCorrection(dcorr)
                .build();
    }

    private void calcPLL() {
        if (dll == null) {
            calcDLL();
        }

        int n = Xl.getRowsCount();
        CanonicalMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        bvar.mul(ssq / n);
        pll = new ProfileLikelihood();
        pll.set(ssq, ldet, b, bvar, n);

    }

    private void clear() {
        o = null;
        ssq = 0;
        ldet = 0;
        dcorr = 0;
        pcorr = 0;
        mcorr = 0;
        mll = null;
        dll=null;
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
