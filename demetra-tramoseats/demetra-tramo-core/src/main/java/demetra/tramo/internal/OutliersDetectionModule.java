/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.tramo.internal;

import demetra.design.BuilderPattern;
import demetra.modelling.regression.Variable;
import demetra.modelling.regression.AdditiveOutlierFactory;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.LevelShiftFactory;
import demetra.modelling.regression.TransitoryChangeFactory;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.outlier.FastOutlierDetector;
import demetra.regarima.outlier.SingleOutlierDetector;
import demetra.regarima.regular.ModelDescription;
import demetra.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.regarima.regular.IOutliersDetectionModule;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
public class OutliersDetectionModule implements IOutliersDetectionModule {

    public static int DEF_MAXROUND = 100;
    public static int DEF_MAXOUTLIERS = 50;
    public static final double EPS = 1e-7;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(OutliersDetectionModule.class)
    public static class Builder {

        private double eps = EPS;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private boolean ao, ls, tc, so;
        private double tcrate;
        private boolean ml;
        private TimeSelector span = TimeSelector.all();

        private Builder() {
        }

        public Builder span(TimeSelector span) {
            this.span = span;
            return this;
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder tcrate(double tcrate) {
            this.tcrate = tcrate;
            return this;
        }

        public Builder ao(boolean ao) {
            this.ao = ao;
            return this;
        }

        public Builder ls(boolean ls) {
            this.ls = ls;
            return this;
        }

        public Builder tc(boolean tc) {
            this.tc = tc;
            return this;
        }

        public Builder so(boolean so) {
            this.so = so;
            return this;
        }

        public Builder maxOutliers(int max) {
            this.maxOutliers = max;
            return this;
        }

        public Builder maxRound(int max) {
            this.maxRound = max;
            return this;
        }

        public Builder maximumLikelihood(boolean ml) {
            this.ml = ml;
            return this;
        }

        public OutliersDetectionModule build() {
            return new OutliersDetectionModule(this);
        }
    }

    private final double eps;
    private final int maxOutliers;
    private final int maxRound;
    private final boolean ao, ls, tc, so;
    private final boolean ml;
    private final double tcrate;
    private final TimeSelector span;

    private OutliersDetectionModule(Builder builder) {
        this.eps = builder.eps;
        this.maxOutliers = builder.maxOutliers;
        this.maxRound = builder.maxRound;
        this.ao = builder.ao;
        this.ls = builder.ls;
        this.tc = builder.tc;
        this.so = builder.so;
        this.tcrate = builder.tcrate;
        this.span = builder.span;
        this.ml = builder.ml;
    }

    private OutliersDetectionModuleImpl make(ModelDescription desc, double cv) {
        TsDomain domain = desc.getDomain();
        int test = comatip(desc);
        boolean cmvx;
        if (test < 0) {
            return null;
        }
        if (test > 0) {
            cmvx = true;
        } else {
            cmvx = false;
        }

        OutliersDetectionModuleImpl.Builder builder = OutliersDetectionModuleImpl.builder()
                .singleOutlierDetector(factories())
                .criticalValue(cv)
                .maximumLikelihood(cmvx)
                .maxOutliers(maxOutliers)
                .maxRound(maxRound)
                .processor(RegArimaUtility.processor(desc.getArimaComponent().defaultMapping(), true, eps));
        OutliersDetectionModuleImpl impl = builder.build();
        TsDomain odom = domain.select(span);
        int start = domain.indexOf(odom.getStartPeriod()), end = start + odom.getLength();
        impl.prepare(domain.getLength());
        impl.setBounds(start, end);
        String[] types = impl.outlierTypes();
        // remove missing values
        int[] missing = desc.transformation().getMissing();
        if (missing != null) {
            for (int i = 0; i < missing.length; ++i) {
                for (int j = 0; j < types.length; ++j) {
                    impl.exclude(missing[i], j);
                }
            }
        }
        // current outliers ([fixed], pre-specified, identified)
        desc.variables()
                .filter(var -> var.getVariable() instanceof IOutlier)
                .map(var -> (IOutlier) var.getVariable()).forEach(
                o -> impl.exclude(domain.indexOf(o.getPosition()), outlierType(types, o.getCode())));
        return impl;
    }

    private SingleOutlierDetector<SarimaModel> factories() {
        FastOutlierDetector detector = new FastOutlierDetector(null);
        if (ao) {
            detector.addOutlierFactory(AdditiveOutlierFactory.FACTORY);
        }
        if (ls) {
            detector.addOutlierFactory(LevelShiftFactory.FACTORY_ZEROENDED);
        }
        if (tc) {
            detector.addOutlierFactory(new TransitoryChangeFactory(tcrate));
        }
        return detector;
    }

    @Override
    public ProcessingResult process(RegArimaModelling context, double criticalValue) {
        ModelDescription model = context.getDescription();
        TsDomain domain = model.getDomain();
        OutliersDetectionModuleImpl impl = make(model, criticalValue);
        if (impl == null) {
            return ProcessingResult.Failed;
        }
        boolean ok = impl.process(model.regarima());
        if (!ok) {
            return ProcessingResult.Failed;
        }
        // add new outliers
        int[][] outliers = impl.getOutliers();
        if (outliers.length == 0) {
            return ProcessingResult.Unchanged;
        }
        for (int i = 0; i < outliers.length; ++i) {
            int[] cur = outliers[i];
            TsPeriod pos = domain.get(cur[0]);
            IOutlier o = impl.getFactory(cur[1]).make(pos.start());
            model.addVariable(new Variable(o, IOutlier.defaultName(o.getCode(), pos), false));
        }
        return ProcessingResult.Changed;
    }

    private static int outlierType(String[] all, String cur) {
        for (int i = 0; i < all.length; ++i) {
            if (cur.equals(all[i])) {
                return i;
            }
        }
        return -1;
    }

    private int comatip(ModelDescription desc) {
        // int rslt = ml ? 1 : 0;
        int n = desc.getSeries().getValues().count(x -> Double.isFinite(x));
        // first, check if od is possible
        SarimaSpecification spec = desc.getSpecification();
        int nparm = Math.max(spec.getD() + spec.getP() + spec.getPeriod()
                * (spec.getBd() + spec.getBp()), spec.getQ()
                + spec.getPeriod() * spec.getBq())
                + (desc.isEstimatedMean() ? 1 : 0)
                + (15 * n) / 100 + spec.getPeriod();
        if (n - nparm <= 0) {
            return -1;
        }
        if (ml) {
            return 1;
        }
        int ndf1 = TramoUtility.autlar(n, spec);
        int ndf2 = 0;
        if (spec.getP() + spec.getBp() > 0 && spec.getQ() + spec.getBq() > 0) {
            n -= spec.getP() + spec.getPeriod() * spec.getBp();
            spec.setP(0);
            spec.setBp(0);
            ndf2 = TramoUtility.autlar(n, spec);
        }
        if (ndf1 < 0 || ndf2 < 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
