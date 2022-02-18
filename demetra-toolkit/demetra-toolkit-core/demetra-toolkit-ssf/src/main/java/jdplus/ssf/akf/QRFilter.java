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
import jdplus.stats.likelihood.DeterminantalTerm;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.univariate.DefaultFilteringResults;
import jdplus.ssf.univariate.FastFilter;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import demetra.data.DoubleSeq;
import jdplus.math.linearsystem.QRLeastSquaresSolution;
import jdplus.math.linearsystem.QRLeastSquaresSolver;
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

    private ISsfData o;
    private FastMatrix X, Xl;
    private DataBlock yl;
    private double ldet;

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

    public MarginalLikelihood marginalLikelihood(boolean scalingFactor, boolean res) {
        DiffuseLikelihood dll = diffuseLikelihood(false, res);
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
        double mcorr = 2 * LogSign.of(qrx.rawRdiagonal()).getValue();
        int nd = UpperTriangularMatrix.rank(qrx.rawR(), EPS), n = Xl.getRowsCount();

        return MarginalLikelihood.builder(n, nd)
                .ssqErr(dll.ssq())
                .logDeterminant(ldet)
                .diffuseCorrection(dll.getDiffuseCorrection())
                .marginalCorrection(mcorr)
                .residuals(dll.e())
                .concentratedScalingFactor(scalingFactor)
                .build();

    }

    public DiffuseLikelihood diffuseLikelihood(boolean scalingFactor, boolean res) {
        QRLeastSquaresSolution ls = QRLeastSquaresSolver.robustLeastSquares(yl, Xl);
        DataBlock b = DataBlock.of(ls.getB());
        DataBlock e = DataBlock.of(ls.getE());
        int nd = b.length(), n = Xl.getRowsCount();
        double ssq = ls.getSsqErr();
        double dcorr = 2 * LogSign.of(ls.rawRDiagonal()).getValue();
        return DiffuseLikelihood.builder(n, nd)
                .ssqErr(ssq)
                .logDeterminant(ldet)
                .diffuseCorrection(dcorr)
                .concentratedScalingFactor(scalingFactor)
                .residuals(res ? e : null)
                .build();

    }

    public ProfileLikelihood profileLikelihood() {
        QRLeastSquaresSolution ls = QRLeastSquaresSolver.robustLeastSquares(yl, Xl);
        DataBlock b = DataBlock.of(ls.getB());
        DataBlock e = DataBlock.of(ls.getE());
        int nd = b.length(), n = Xl.getRowsCount();
        double ssq = ls.getSsqErr();
        double dcorr = 2 * LogSign.of(ls.rawRDiagonal()).getValue();
        FastMatrix R = ls.rawR();
        FastMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        bvar.mul(ssq / n);
        ProfileLikelihood pll = new ProfileLikelihood();
        pll.set(ssq, ldet, b, bvar, n);
        return pll;
    }

    private void clear() {
        o = null;
        ldet = 0;
        X = null;
        Xl = null;
        yl = null;
    }

}
