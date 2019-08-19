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
package jdplus.ssf.akf;

import jdplus.ssf.likelihood.ProfileLikelihood;
import jdplus.ssf.likelihood.MarginalLikelihood;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ILikelihoodComputer;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinaryFilter;
import demetra.likelihood.Likelihood;
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
public class AkfToolkit {

    private AkfToolkit() {
    }

    public static ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer() {
        return likelihoodComputer(true);
    }

    public static ILikelihoodComputer<MarginalLikelihood> marginalLikelihoodComputer(final boolean concentrated) {
        return (ssf, data) -> QRFilter.ml(ssf, data, concentrated);

    }

    public static ILikelihoodComputer<ProfileLikelihood> profileLikelihoodComputer() {
        return new PLLComputer();
    }
    
    public static ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean collapsing) {
        return likelihoodComputer(collapsing, false);
    }

    public static ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean collapsing, boolean res) {
        return collapsing ? new LLComputer2(res) : new LLComputer1(res);
    }
    
        public static ILikelihoodComputer<DiffuseLikelihood> fastLikelihoodComputer(boolean res) {
        return (ISsf ssf, ISsfData data) -> {
            AugmentedPredictionErrorDecomposition decomp = new AugmentedPredictionErrorDecomposition(res);
            CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(new AugmentedFilterInitializer(decomp));
            CkmsFilter ffilter = new CkmsFilter(ff);
            ffilter.process(ssf, data, decomp);
            return decomp.likelihood();
        };
    }


    public static DefaultAugmentedFilteringResults filter(ISsf ssf, ISsfData data, boolean all) {
        DefaultAugmentedFilteringResults frslts = all
                ? DefaultAugmentedFilteringResults.full() : DefaultAugmentedFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public static DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all) {
        AugmentedSmoother smoother = new AugmentedSmoother();
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

    public static StateStorage smooth(IMultivariateSsf ssf, IMultivariateSsfData data, boolean all, boolean rescaleVariance) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        DefaultSmoothingResults sr = smooth(ussf, udata, all);
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
        
        private final boolean res;
        
        private LLComputer1(boolean res){
            this.res=res;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {
            AugmentedFilter akf = new AugmentedFilter();
            AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(res);
            pe.prepare(ssf, data.length());
            if (!akf.process(ssf, data, pe)) {
                return null;
            }
            return pe.likelihood();
        }

    }

    private static class LLComputer2 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean res;
        
        private LLComputer2(boolean res){
            this.res=res;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {
            AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(res);
            pe.prepare(ssf, data.length());
            AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood();
        }
    }

    private static class PLLComputer implements ILikelihoodComputer<ProfileLikelihood> {

        @Override
        public ProfileLikelihood compute(ISsf ssf, ISsfData data) {
            QRFilter qr = new QRFilter();
            if (!qr.process(ssf, data)) {
                return null;
            }
            return qr.getProfileLikelihood();
        }
    }

    public static double var(int n, DefaultAugmentedFilteringResults frslts) {
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
