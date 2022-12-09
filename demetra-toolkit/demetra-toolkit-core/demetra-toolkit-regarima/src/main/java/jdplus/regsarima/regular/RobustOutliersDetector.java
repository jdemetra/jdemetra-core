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
package jdplus.regsarima.regular;

import jdplus.regsarima.ami.FastOutliersDetector;
import nbbrd.design.BuilderPattern;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.outlier.FastOutlierDetector;
import jdplus.regarima.outlier.SingleOutlierDetector;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.FastMatrix;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.estimation.SarimaMapping;

/**
 *
 * @author Jean Palate
 */
public class RobustOutliersDetector {

    public static int DEF_MAXROUND = 20;
    public static int DEF_MAXOUTLIERS = 15;
    public static final double EPS = 1e-5;
    public static final double CV = 6;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(RobustOutliersDetector.class)
    public static class Builder {

        private double eps = EPS;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private double cv = 6;

        private Builder() {
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder criticalValue(double cv) {
            this.cv = cv;
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

        public RobustOutliersDetector build() {
            return new RobustOutliersDetector(this);
        }
    }

    private final double eps;
    private final int maxOutliers;
    private final int maxRound;
    private final double cv;

    private int[][] outliers;

    private RobustOutliersDetector(Builder builder) {
        this.eps = builder.eps;
        this.maxOutliers = builder.maxOutliers;
        this.maxRound = builder.maxRound;
        this.cv = builder.cv;
    }

    private FastOutliersDetector make(DoubleSeq s) {
        
        FastOutliersDetector.Builder builder = FastOutliersDetector.builder()
                .singleOutlierDetector(factories())
                .criticalValue(cv)
                .maximumLikelihood(false)
                .maxOutliers(maxOutliers)
                .maxRound(maxRound)
                .processor(RegArimaUtility.processor(true, eps));
        FastOutliersDetector impl = builder.build();
        int n = s.length();
        impl.prepare(n);
        impl.setBounds(0, n);
        return impl;
    }

    private SingleOutlierDetector<SarimaModel> factories() {
        FastOutlierDetector detector = new FastOutlierDetector(null);
        detector.setOutlierFactories(AdditiveOutlierFactory.FACTORY, LevelShiftFactory.FACTORY_ZEROENDED);
        return detector;
    }

    public DoubleSeq process(DoubleSeq s, int period, FastMatrix regs) {
        outliers=null;
        FastOutliersDetector impl = make(s);
        // airline
        SarimaOrders spec = SarimaOrders.airline(period);
        SarimaModel airline = SarimaModel.builder(spec).setDefault().build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .arima(airline)
                .y(s)
                .addX(regs)
                .build();
        boolean ok = impl.process(regarima, SarimaMapping.of(spec));
        if (!ok) {
            return s;
        }
        // add new outliers
        outliers = impl.getOutliers();
        
        RegArimaModel<SarimaModel> regArima = impl.getRegArima();
        RegArimaEstimation<SarimaModel> estimation = RegArimaUtility.processor(true, eps).process(regArima, SarimaMapping.of(spec));
        return estimation.linearizedSeries();
    }
    
    public int[][] getOutliers(){
        return outliers;
    }

}
