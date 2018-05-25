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
package demetra.x12;

import demetra.arima.IResidualsComputer;
import demetra.arima.internal.AnsleyFilter;
import demetra.design.BuilderPattern;
import demetra.modelling.Variable;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.TransitoryChange;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.outlier.ExactSingleOutlierDetector;
import demetra.regarima.outlier.IRobustStandardDeviationComputer;
import demetra.regarima.outlier.SingleOutlierDetector;
import demetra.regarima.regular.ModelDescription;
import demetra.sarima.SarimaModel;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.x12.OutliersDetectionModuleImpl;
import demetra.regarima.regular.IOutliersDetectionModule;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;

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
        private TimeSelector span=TimeSelector.all();

        private Builder() {
        }
        
        public Builder span(TimeSelector span){
            this.span=span;
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
            this.ao=ao;
            return this;
        }

        public Builder ls(boolean ls) {
            this.ls=ls;
            return this;
        }

        public Builder tc(boolean tc) {
            this.tc=tc;
            return this;
        }

        public Builder so(boolean so) {
            this.so=so;
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

        public OutliersDetectionModule build() {
            return new OutliersDetectionModule(this);
        }
    }

    private final double eps;
    private final int maxOutliers;
    private final int maxRound;
    private final boolean ao, ls, tc, so;
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
    }


    private SingleOutlierDetector<SarimaModel> factories(int freq) {
        SingleOutlierDetector sod = new ExactSingleOutlierDetector(IRobustStandardDeviationComputer.mad(false),
                IResidualsComputer.mlComputer(),
                new AnsleyFilter());
        if (ao) {
            sod.addOutlierFactory(AdditiveOutlier.FACTORY);
        }
        if (ls) {
            sod.addOutlierFactory(LevelShift.FACTORY_ZEROENDED);
        }
        if (tc) {
            double c = tcrate;
            int r = 12 / freq;
            if (r > 1) {
                c = Math.pow(c, r);
            }
            sod.addOutlierFactory(new TransitoryChange.Factory(c));
        }
        if (freq > 1 && so) {
            sod.addOutlierFactory(new PeriodicOutlier.Factory(freq, true));
        }
        return sod;
    }

    private OutliersDetectionModuleImpl make(ModelDescription desc, double cv) {
        TsDomain domain=desc.getDomain();

        OutliersDetectionModuleImpl impl = OutliersDetectionModuleImpl.builder()
                .singleOutlierDetector(factories(domain.getAnnualFrequency()))
                .criticalValue(cv)
                .maxOutliers(maxOutliers)
                .maxRound(maxRound)
                .processor(RegArimaUtility.processor(desc.getArimaComponent().defaultMapping(), true, EPS))
                .build();
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

    @Override
    public ProcessingResult process(RegArimaModelling context, double criticalValue) {
        ModelDescription model=context.getDescription();
        TsDomain domain = model.getDomain();
        OutliersDetectionModuleImpl impl=make(model, criticalValue);
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
            model.addVariable(new Variable(impl.getFactory(cur[1]).make(pos.start()), false));
        }
        context.setEstimation(null);
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

}
