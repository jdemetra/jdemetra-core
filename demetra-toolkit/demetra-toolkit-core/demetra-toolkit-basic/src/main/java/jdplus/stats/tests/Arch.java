/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stats.tests;

import demetra.data.DoubleSeq;
import demetra.design.BuilderPattern;
import demetra.stats.StatException;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Arch {

    public Lm lm(DoubleSeq res) {
        return new Lm(res.fn(z -> z * z));
    }

    public PorteManteau porteManteau(DoubleSeq res) {
        return new PorteManteau(res.fn(z -> z * z));
    }

    @BuilderPattern(StatisticalTest.class)
    public static class Lm {

        private DoubleSeq e2;
        private int nlags = 1;
        private LeastSquaresResults lsr;

        private Lm(DoubleSeq e2) {
            this.e2 = e2;
        }

        public Lm autoCorrelationsCount(int nlags) {
            this.nlags = nlags;
            return this;
        }

        public StatisticalTest build() {
            // build the regression model
            int n = e2.length() - nlags;
            if (n <= nlags + 1) {
                throw new StatException(StatException.NOT_ENOUGH_DATA);
            }
            LinearModel.Builder builder = LinearModel.builder()
                    .y(e2.extract(nlags, n))
                    .meanCorrection(true);
            for (int i = 0; i < nlags; ++i) {
                builder.addX(e2.extract(i, n));
            }
            lsr = Ols.compute(builder.build());
            return lsr==null ? null : lsr.Khi2Test();
        }
        
        public LeastSquaresResults getLeastSquaresResults(){
            return lsr;
        }
    }

    @BuilderPattern(StatisticalTest.class)
    public static class PorteManteau {

        private final DoubleSeq e2;
        private boolean ljungbox = true;

        private int nlags = 4;

        private PorteManteau(DoubleSeq e2) {
            this.e2 = e2;
        }

        public PorteManteau useLjungBox(boolean lb) {
            ljungbox = lb;
            return this;
        }

        public PorteManteau autoCorrelationsCount(int nlags) {
            this.nlags = nlags;
            return this;
        }

        public StatisticalTest build() {
            return ljungbox ? new LjungBox(e2, true)
                    .autoCorrelationsCount(nlags)
                    //.usePositiveAutoCorrelations()
                    .build()
                    : new BoxPierce(e2, true)
                            .autoCorrelationsCount(nlags)
                            //.usePositiveAutoCorrelations()
                            .build();
        }
    }
}
