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
package jdplus.ssf.akf;

import jdplus.ssf.likelihood.ProfileLikelihood;
import jdplus.ssf.likelihood.MarginalLikelihood;
import jdplus.data.DataBlock;
import jdplus.data.LogSign;
import jdplus.likelihood.DeterminantalTerm;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.math.matrices.decomposition.Householder;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.univariate.DefaultFilteringResults;
import jdplus.ssf.univariate.FastFilter;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import demetra.data.DoubleSeq;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.decomposition.Householder2;
import jdplus.math.matrices.decomposition.QRDecomposition;

/**
 * QR variant of the augmented Kalman filter. See for instance Gomez-Maravall.
 * This implementation doesn't use collapsing
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilter {

    private ProfileLikelihood pll;
    private MarginalLikelihood mll;
    private DiffuseLikelihood dll;
    private ISsfData o;
    private FastMatrix R, X, Xl;
    private DataBlock yl, b, e;
    private double ldet, ssq, dcorr, pcorr, mcorr;

    private static final double EPS = 1e-12;

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
        X = FastMatrix.make(data.length(), ssf.getDiffuseDim());
        ssf.diffuseEffects(X);
        yl = DataBlock.of(fr.errors(true, true));
        FastFilter ffilter = new FastFilter(ssf, fr, new ResultsRange(0, data.length()));
        int n = ffilter.getOutputLength(X.getRowsCount());
        Xl = FastMatrix.make(n, X.getColumnsCount());
        for (int i = 0; i < X.getColumnsCount(); ++i) {
            ffilter.apply(X.column(i), Xl.column(i));
        }
        return true;
    }

    public static MarginalLikelihood ml(final ISsf ssf, final ISsfData data, boolean scalingfactor) {
        AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(true);
        pe.prepare(ssf, data.length());
        AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(pe);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        if (!filter.process(ssf, data, pe)) {
            return null;
        }
        int collapsing = pe.getCollapsingPosition();
        DiffuseLikelihood likelihood = pe.likelihood(scalingfactor);
        FastMatrix M = FastMatrix.make(collapsing, ssf.getDiffuseDim());
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
        QRDecomposition qr = new Householder2().decompose(M.extract(0, j, 0, M.getColumnsCount()));
        double mc = 2 * LogSign.of(qr.rawRdiagonal()).getValue();
        return MarginalLikelihood.builder(likelihood.dim(), likelihood.getD())
                .concentratedScalingFactor(scalingfactor)
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

        FastMatrix Q = X;
        if (X.getRowsCount() != Xl.getRowsCount()) {
            Q = FastMatrix.make(Xl.getRowsCount(), X.getColumnsCount());
            for (int i = 0, j = 0; i < o.length(); ++i) {
                if (!o.isMissing(i)) {
                    Q.row(j++).copy(X.row(i));
                }
            }
        }
        QRDecomposition qrx = new Householder2().decompose(Q);
        mcorr = 2 * LogSign.of(qrx.rawRdiagonal()).getValue();
        int nd = UpperTriangularMatrix.rank(qrx.rawR(), EPS), n = Xl.getRowsCount();

        mll = MarginalLikelihood.builder(n, nd)
                .ssqErr(ssq)
                .logDeterminant(ldet)
                .diffuseCorrection(dcorr)
                .marginalCorrection(mcorr)
                .residuals(e)
                .build();
    }

    private void calcDLL() {
        QRSolution ls = QRSolver.robustLeastSquares(yl, Xl);
        b = DataBlock.of(ls.getB());
        e = DataBlock.of(ls.getE());
        int nd = b.length(), n = Xl.getRowsCount();
        ssq = ls.getSsqErr();
        dcorr = 2 * LogSign.of(ls.rawRDiagonal()).getValue();
        R = ls.rawR();
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
        FastMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
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
        dll = null;
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
