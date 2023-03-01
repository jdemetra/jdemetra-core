/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
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
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ILikelihoodComputer;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.StateInfo;
import jdplus.ssf.StateStorage;
import jdplus.ssf.ckms.CkmsDiffuseInitializer;
import jdplus.ssf.ckms.CkmsFilter;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.IMultivariateSsfData;
import jdplus.ssf.multivariate.M2uAdapter;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AkfToolkit {

    public ILikelihoodComputer<MarginalLikelihood> marginalLikelihoodComputer(final boolean scalingfactor, boolean res) {
        return (ssf, data)
                -> {
            QRFilter qr = new QRFilter();
            qr.process(ssf, data);
            return qr.marginalLikelihood(scalingfactor, res);
        };
    }

    public ILikelihoodComputer<ProfileLikelihood> profileLikelihoodComputer() {
        return (ssf, data) -> {
            QRFilter qr = new QRFilter();
            if (!qr.process(ssf, data)) {
                return null;
            }
            return qr.profileLikelihood();
        };
    }

    public ILikelihoodComputer<DiffuseLikelihood> robustLikelihoodComputer(final boolean scalingfactor, boolean res) {
        return (ssf, data) -> {
            QRFilter qr = new QRFilter();
            if (!qr.process(ssf, data)) {
                return null;
            }
            return qr.diffuseLikelihood(scalingfactor, res);
        };
    }

    public ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean collapsing, boolean scalingfactor, boolean res) {
        return collapsing ? new LLComputer2(scalingfactor, res) : new LLComputer1(scalingfactor, res);
    }

    public ILikelihoodComputer<DiffuseLikelihood> fastLikelihoodComputer(boolean scalingfactor, boolean res) {
        return (ISsf ssf, ISsfData data) -> {
            QPredictionErrorDecomposition decomp = new QPredictionErrorDecomposition(res);
            CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(new AugmentedFilterInitializer(decomp));
            CkmsFilter ffilter = new CkmsFilter(ff);
            ffilter.process(ssf, data, decomp);
            return decomp.likelihood(scalingfactor);
        };
    }

    public DefaultQFilteringResults filter(ISsf ssf, ISsfData data, boolean all, boolean collapsing) {
        DefaultQFilteringResults frslts = all
                ? DefaultQFilteringResults.full() : DefaultQFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        if (collapsing) {
            AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(frslts);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, frslts);
        } else {
            AugmentedFilter filter = new AugmentedFilter();
            filter.process(ssf, data, frslts);
        }
        return frslts;
    }

    public DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance, boolean collapsing) {
        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(all);
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        DefaultQFilteringResults fresults = filter(ssf, data, true, collapsing);
        if (smoother.process(ssf, data.length(), fresults, sresults)) {
            if (rescaleVariance) {
                sresults.rescaleVariances(var(data.length(), fresults));
            }
            return sresults;
        } else {
            return null;
        }
    }

    public SmoothingOutput robustSmooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance) {
       return QRSmoother.process(ssf, data, all, rescaleVariance);
    }

    public StateStorage smooth(IMultivariateSsf ssf, IMultivariateSsfData data, boolean all, boolean rescaleVariance, boolean collapsing) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        DefaultSmoothingResults sr = smooth(ussf, udata, all, false, collapsing);
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

    public StateStorage robustSmooth(IMultivariateSsf ssf, IMultivariateSsfData data, boolean all, boolean rescaleVariance) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        StateStorage sr = robustSmooth(ussf, udata, all, rescaleVariance).getSmoothing();
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

    private static class LLComputer1 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean scalingfactor, res;

        private LLComputer1(boolean scalingfactor, boolean res) {
            this.scalingfactor = scalingfactor;
            this.res = res;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {
            AugmentedFilter akf = new AugmentedFilter();
            QPredictionErrorDecomposition pe = new QPredictionErrorDecomposition(res);
            pe.prepare(ssf, data.length());
            if (!akf.process(ssf, data, pe)) {
                return null;
            }
            return pe.likelihood(scalingfactor);
        }

    }

    private static class LLComputer2 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean scalingfactor, res;

        private LLComputer2(boolean scalingfactor, boolean res) {
            this.scalingfactor = scalingfactor;
            this.res = res;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {
            QPredictionErrorDecomposition pe = new QPredictionErrorDecomposition(res);
            pe.prepare(ssf, data.length());
            AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood(scalingfactor);
        }
    }

    public double var(int n, DefaultQFilteringResults frslts) {
        double c = frslts.getAugmentation().c();
        double ssq = c * c;
        int nd = frslts.getCollapsingPosition();
        int m = frslts.getAugmentation().getDegreesofFreedom();
        for (int i = nd; i < n; ++i) {
            double e = frslts.error(i);
            if (Double.isFinite(e)) {
                ++m;
                ssq += e * e / frslts.errorVariance(i);
            }
        }
        return ssq / m;
    }

}
