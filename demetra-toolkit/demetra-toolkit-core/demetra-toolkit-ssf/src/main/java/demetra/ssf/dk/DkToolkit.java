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

import demetra.ssf.likelihood.DiffuseLikelihood;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DataBlockStorage;
import demetra.likelihood.DeterminantalTerm;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.linearfilters.ILinearProcess;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;
import demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import demetra.ssf.ResultsRange;
import demetra.ssf.State;
import demetra.ssf.ckms.CkmsDiffuseInitializer;
import demetra.ssf.ckms.CkmsFilter;
import demetra.ssf.dk.sqrt.CompositeDiffuseSquareRootFilteringResults;
import demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import demetra.ssf.dk.sqrt.DiffuseSquareRootSmoother;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.IConcentratedLikelihoodComputer;
import demetra.ssf.univariate.ILikelihoodComputer;
import demetra.ssf.univariate.ISmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfBuilder;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.OrdinaryFilter;
import demetra.ssf.univariate.SsfRegressionModel;
import demetra.data.DoubleSequence;
import demetra.data.LogSign;
import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;
import demetra.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;
import demetra.ssf.likelihood.MarginalLikelihood;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.IMultivariateSsfData;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.IFilteringResults;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class DkToolkit {

    public DiffuseLikelihood likelihood(ISsf ssf, ISsfData data) {
        return likelihoodComputer().compute(ssf, data);
    }

    public MarginalLikelihood marginalLikelihood(ISsf ssf, ISsfData data, boolean concentrated) {
        return new MLLComputer(concentrated, concentrated).compute(ssf, data);
    }

    public MarginalLikelihood marginalLikelihood(ISsf ssf, ISsfData data, boolean res, boolean concentrated) {
        return new MLLComputer(res, concentrated).compute(ssf, data);
    }

    public DiffuseLikelihood likelihood(IMultivariateSsf ssf, IMultivariateSsfData data) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        return likelihoodComputer().compute(ussf, udata);
    }

    public ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer() {
        return likelihoodComputer(true, false);
    }

    public ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean res) {
        return likelihoodComputer(true, res);
    }

    public ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean sqr, boolean res) {
        return sqr ? new LLComputer2(res) : new LLComputer1(res);
    }

    public IConcentratedLikelihoodComputer<DkConcentratedLikelihood> concentratedLikelihoodComputer() {
        return concentratedLikelihoodComputer(true, false, true);
    }

    public IConcentratedLikelihoodComputer<DkConcentratedLikelihood> concentratedLikelihoodComputer(boolean sqr, boolean fast, boolean scalingFactor) {
        return new CLLComputer(sqr, fast, scalingFactor);
    }

    public <S, F extends ISsf> SsfFunction<S, F> likelihoodFunction(ISsfData data, IParametricMapping<S> mapping, ISsfBuilder<S, F> builder) {
        return SsfFunction.builder(data, mapping, builder).build();
    }

    public <F extends ISsf> SsfFunction<F, F> likelihoodFunction(ISsfData data, IParametricMapping<F> mapping) {
        return SsfFunction.builder(data, mapping, (F f) -> f).build();
    }

    public DefaultDiffuseFilteringResults filter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseFilteringResults frslts = all
                ? DefaultDiffuseFilteringResults.full() : DefaultDiffuseFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public DefaultDiffuseSquareRootFilteringResults sqrtFilter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseSquareRootFilteringResults frslts = all
                ? DefaultDiffuseSquareRootFilteringResults.full() : DefaultDiffuseSquareRootFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public void sqrtFilter(ISsf ssf, ISsfData data, IFilteringResults frslts, boolean all) {
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(null);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
    }

    public void sqrtFilter(ISsf ssf, ISsfData data, IDiffuseSquareRootFilteringResults frslts, boolean all) {
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
    }

    public DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all) {
        DiffuseSmoother smoother = new DiffuseSmoother();
        smoother.setCalcVariances(all);
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        if (smoother.process(ssf, data, sresults)) {
            return sresults;
        } else {
            return null;
        }
    }

    public StateStorage smooth(IMultivariateSsf ssf, IMultivariateSsfData data, boolean all) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        DefaultSmoothingResults sr = sqrtSmooth(ussf, udata, all);
        StateStorage ss = all ? StateStorage.full(StateInfo.Smoothed) : StateStorage.light(StateInfo.Smoothed);
        int m = data.getVarsCount(), n = data.getObsCount();
        ss.prepare(ussf.getStateDim(), 0, n);
        if (all) {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), sr.P(i * m));
            }
        } else {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), null);
            }
        }
        return ss;
    }

    public static boolean smooth(ISsf ssf, ISsfData data, ISmoothingResults sresults) {
        boolean all = sresults.hasVariances();
        DiffuseSmoother smoother = new DiffuseSmoother();
        smoother.setCalcVariances(all);
        return smoother.process(ssf, data, sresults);
    }

    public static DataBlockStorage fastSmooth(ISsf ssf, ISsfData data) {
        return fastSmooth(ssf, data, null);
    }

    public static DataBlockStorage fastSmooth(ISsf ssf, ISsfData data, FastStateSmoother.Corrector corrector) {
        FastStateSmoother smoother = new FastStateSmoother();
        smoother.setCorrector(corrector);
        return smoother.process(ssf, data);
    }

    public static DefaultSmoothingResults sqrtSmooth(ISsf ssf, ISsfData data, boolean all) {
        DiffuseSquareRootSmoother smoother = new DiffuseSquareRootSmoother();
        smoother.setCalcVariances(all);
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        if (smoother.process(ssf, data, sresults)) {
            return sresults;
        } else {
            return null;
        }
    }

    public static boolean sqrtSmooth(ISsf ssf, ISsfData data, ISmoothingResults sresults) {
        boolean all = sresults.hasVariances();
        DiffuseSquareRootSmoother smoother = new DiffuseSquareRootSmoother();
        smoother.setCalcVariances(all);
        return smoother.process(ssf, data, sresults);
    }

    private static class LLComputer1 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean res;

        LLComputer1(boolean res) {
            this.res = res;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood();
        }

    }

    private static class LLComputer2 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean res;

        LLComputer2(boolean res) {
            this.res = res;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood();
        }

        public MarginalLikelihood mcompute(ISsf ssf, ISsfData data, boolean concentrated) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            DiffuseLikelihood likelihood = pe.likelihood();
            int collapsing = pe.getEndDiffusePosition();
            Matrix M = Matrix.make(collapsing, ssf.getDiffuseDim());
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
    }

    private static class MLLComputer implements ILikelihoodComputer<MarginalLikelihood> {

        private final boolean res, concentrated;

        MLLComputer(boolean res, boolean concentrated) {
            this.res = res;
            this.concentrated = concentrated;
        }

        @Override
        public MarginalLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            DiffuseLikelihood likelihood = pe.likelihood();
            int collapsing = pe.getEndDiffusePosition();
            Matrix M = Matrix.make(collapsing, ssf.getDiffuseDim());
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
    }

    private static class CLLComputer implements IConcentratedLikelihoodComputer<DkConcentratedLikelihood> {

        private final boolean sqr, fast, scaling;

        private CLLComputer(boolean sqr, boolean fast, boolean scaling) {
            this.sqr = sqr;
            this.fast = fast;
            this.scaling = scaling;
        }

        @Override
        public DkConcentratedLikelihood compute(SsfRegressionModel model) {
            ISsfData y = model.getY();
            int n = y.length();
            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(true);
            pe.prepare(model.getSsf(), n);
            ILinearProcess lp = filteringResults(model.getSsf(), y, pe);
            DiffuseLikelihood ll = pe.likelihood();
            DoubleSequence yl = pe.errors(true, true);
            int nl = yl.length();
            Matrix xl = xl(model, lp, nl);
            if (xl == null) {
                return DkConcentratedLikelihood.builder(ll.dim(), ll.getD())
                        .ssqErr(ll.ssq())
                        .logDeterminant(ll.logDeterminant())
                        .logDiffuseDeterminant(ll.getDiffuseCorrection())
                        .residuals(yl)
                        .scalingFactor(scaling)
                        .build();
            } else {
                Householder qr = new Householder();
                qr.decompose(xl);
                if (qr.rank() == 0) {
                    return DkConcentratedLikelihood.builder(ll.dim(), ll.getD())
                            .ssqErr(ll.ssq())
                            .logDeterminant(ll.logDeterminant())
                            .logDiffuseDeterminant(ll.getDiffuseCorrection())
                            .residuals(yl)
                            .scalingFactor(scaling)
                            .build();
                } else {
                    int rank = qr.rank();
                    DataBlock b = DataBlock.make(rank);
                    DataBlock res = DataBlock.make(nl - rank);
                    qr.leastSquares(yl, b, res);
                    double ssqerr = res.ssq();
                    Matrix u = UpperTriangularMatrix.inverse(qr.r(true));
                    int[] unused = qr.unused();
                    // expand the results, if need be
                    b = expand(b, unused);
                    u = expand(u, unused);
                    // initializing the results...
                    int nobs = ll.dim();
                    int d = ll.getD();
                    int[] idiffuse = model.getDiffuseElements();
                    double ldet = ll.logDeterminant(), dcorr = ll.getDiffuseCorrection();
                    if (idiffuse != null) {
                        DoubleSequence rdiag = qr.rdiagonal(true);
                        double lregdet = 0;
                        int ndc = 0;
                        for (int i = 0; i < idiffuse.length; ++i) {
                            if (isUsed(idiffuse[i], unused)) {
                                lregdet += Math.log(Math.abs(rdiag
                                        .get(idiffuse[i])));
                                ++ndc;
                            }
                        }
                        lregdet *= 2;
                        dcorr += lregdet;
                        d += ndc;
                    }
                    double sig = ssqerr / (nobs - d);
                    Matrix bvar = SymmetricMatrix.UUt(u);
                    bvar.mul(sig);
                    return DkConcentratedLikelihood.builder(ll.dim(), ll.getD())
                            .ssqErr(ll.ssq())
                            .logDeterminant(ll.logDeterminant())
                            .logDiffuseDeterminant(ll.getDiffuseCorrection())
                            .residuals(yl)
                            .coefficients(b)
                            .unscaledCovariance(bvar)
                            .scalingFactor(scaling)
                            .build();
                }
            }
        }

        private DataBlock expand(DataBlock x, int[] unused) {
            if (unused == null) {
                return x;
            }
            double[] bc = new double[x.length() + unused.length];
            for (int i = 0, j = 0, k = 0; i < bc.length; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    bc[i] = x.get(j);
                    ++j;
                }
            }
            return DataBlock.ofInternal(bc);
        }

        private Matrix expand(Matrix v, int[] unused) {
            if (unused == null) {
                return v;
            }
            int nx = v.getColumnsCount() + unused.length;
            Matrix bvar = Matrix.square(nx);
            for (int i = 0, j = 0, k = 0; i < nx; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    for (int ci = 0, cj = 0, ck = 0; ci <= i; ++ci) {
                        if (ck < unused.length && ci == unused[ck]) {
                            ++ck;
                        } else {
                            double d = v.get(j, cj);
                            bvar.set(i, ci, d);
                            bvar.set(ci, i, d);
                            ++cj;
                        }
                    }
                    ++j;
                }
            }
            return bvar;
        }

        private ILinearProcess filteringResults(ISsf ssf, ISsfData data, DiffusePredictionErrorDecomposition pe) {
            if (sqr) {
                DefaultDiffuseSquareRootFilteringResults fr = DefaultDiffuseSquareRootFilteringResults.light();
                fr.prepare(ssf, 0, data.length());
                CompositeDiffuseSquareRootFilteringResults dr = new CompositeDiffuseSquareRootFilteringResults(fr, pe);
                DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                }
            } else {
                DefaultDiffuseFilteringResults fr = DefaultDiffuseFilteringResults.light();
                fr.prepare(ssf, 0, data.length());
                CompositeDiffuseFilteringResults dr = new CompositeDiffuseFilteringResults(fr, pe);
                DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                }
            }
        }

        private Matrix xl(SsfRegressionModel model, ILinearProcess lp, int nl) {
            Matrix x = model.getX();
            if (x == null) {
                return null;
            }
            Matrix xl = Matrix.make(nl, x.getColumnsCount());
            DataBlockIterator lcols = xl.columnsIterator();
            DataBlockIterator cols = x.columnsIterator();
            while (cols.hasNext() && lcols.hasNext()) {
                lp.transform(cols.next(), lcols.next());
            }
            return xl;
        }

        private static boolean isUsed(final int i, final int[] unused) {
            for (int j = 0; j < unused.length; ++j) {
                if (unused[j] == i) {
                    return false;
                }
            }
            return true;
        }
    }

    public static double var(int n, IBaseDiffuseFilteringResults frslts) {
        int m = 0;
        double ssq = 0;
        int nd = frslts.getEndDiffusePosition();
        for (int i = 0; i < nd; ++i) {
            double e = frslts.error(i);
            if (Double.isFinite(e) && frslts.diffuseNorm2(i) == 0) {
                ++m;
                ssq += e * e / frslts.errorVariance(i);
            }
        }
        for (int i = nd; i < n; ++i) {
            double e = frslts.error(i);
            if (Double.isFinite(e)) {
                ++m;
                ssq += e * e / frslts.errorVariance(i);
            }
        }
        return ssq / m;
    }

    public static double logDeterminant(int n, IBaseDiffuseFilteringResults frslts) {
        DeterminantalTerm det = new DeterminantalTerm();
        for (int i = 0; i < frslts.getEndDiffusePosition(); ++i) {
            if (Double.isFinite(frslts.error(i))) {
                double d = frslts.diffuseNorm2(i);
                if (d == 0) {
                    double e = frslts.errorVariance(i);
                    if (e > State.ZERO) {
                        det.add(e);
                    }
                }
            }
        }
        for (int i = frslts.getEndDiffusePosition(); i < n; ++i) {
            if (Double.isFinite(frslts.error(i))) {
                double e = frslts.errorVariance(i);
                if (e > State.ZERO) {
                    det.add(e);
                }
            }
        }
        return det.getLogDeterminant();

    }

    public static double diffuseCorrection(IBaseDiffuseFilteringResults frslts) {
        DeterminantalTerm det = new DeterminantalTerm();
        for (int i = 0; i < frslts.getEndDiffusePosition(); ++i) {
            if (Double.isFinite(frslts.error(i))) {
                double d = frslts.diffuseNorm2(i);
                if (d > 0) {
                    det.add(d);
                }
            }
        }
        return det.getLogDeterminant();
    }

}
