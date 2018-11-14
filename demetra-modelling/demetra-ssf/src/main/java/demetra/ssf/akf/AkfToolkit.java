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
package demetra.ssf.akf;

import demetra.ssf.likelihood.ProfileLikelihood;
import demetra.ssf.likelihood.MarginalLikelihood;
import demetra.likelihood.ILikelihood;
import demetra.ssf.likelihood.DiffuseLikelihood;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ILikelihoodComputer;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.OrdinaryFilter;

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
        return (ssf, data)->QRFilter.ml(ssf, data, concentrated);

    }

    public static ILikelihoodComputer<ProfileLikelihood> profileLikelihoodComputer() {
        return new PLLComputer();
    }

    public static ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean collapsing) {
        return collapsing ? new LLComputer2() : new LLComputer1();
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
            if (all) {
                sresults.rescaleVariances(var(data.length(), smoother.getFilteringResults()));
            }
            return sresults;
        } else {
            return null;
        }
    }

    private static class LLComputer1 implements ILikelihoodComputer<DiffuseLikelihood> {

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {
            AugmentedFilter akf = new AugmentedFilter();
            AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(false);
            pe.prepare(ssf, data.length());
            if (!akf.process(ssf, data, pe)) {
                return null;
            }
            return pe.likelihood();
        }

    }

    private static class LLComputer2 implements ILikelihoodComputer<DiffuseLikelihood> {

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {
            AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(false);
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
    public static double var(int n, IAugmentedFilteringResults frslts) {
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
