/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.data;

import demetra.design.BuilderPattern;
import demetra.maths.Constants;
import demetra.stats.AutoCovariances;

/**
 *
 * @author Jean Palate
 */
public class SmoothedPeriodogram {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(SmoothedPeriodogram.class)
    public static class Builder {

        private DiscreteWindowFunction win = DiscreteWindowFunction.Tukey;
        private int winLen = 44;
        private double relativeResolution = .5;
        private ITaper taper = null;
        private DoubleSequence data;

        private Builder() {
        }

        public Builder data(DoubleSequence data) {
            this.data = data;
            return this;
        }

        public Builder taper(ITaper taper) {
            this.taper = taper;
            return this;
        }

        public Builder windowFunction(DiscreteWindowFunction win) {
            this.win = win;
            return this;
        }

        public Builder windowLength(int windowLength) {
            this.winLen = windowLength;
            return this;
        }

        public Builder relativeResolution(double resolution) {
            this.relativeResolution = resolution;
            return this;
        }

        public SmoothedPeriodogram build() {
            if (data == null) {
                throw new RuntimeException("Uninitialized data");
            }
            if (winLen >= data.length()) {
                throw new RuntimeException("Not enough data");
            }

            double[] x = data.toArray();
            // correct for mean
            double mean = Doubles.averageWithMissing(data);
            if (mean != 0) {
                for (int i = 0; i < x.length; ++i) {
                    x[i] -= mean;
                }
            }

            if (taper != null) {
                taper.process(x);
            }
            DoubleSequence datac = DoubleSequence.ofInternal(x);
            double[] ac = AutoCovariances.autoCovariancesWithZeroMean(datac, winLen - 1);
            int ns = 1 + (int) (winLen * relativeResolution);
            int len = 2 * ns - 1;
            double[] p = dft(ac, ns);
            return new SmoothedPeriodogram(p, len);
        }

        private double[] dft(double[] ac, int ns) {
            int len = 2 * ns - 1;
            double[] cwnd = win.discreteWindow(2 * winLen + 1);
            for (int i = 1; i < winLen; i++) {
                ac[i] *= cwnd[i] / ac[0];
            }
            // current resolution
            double[] s = new double[ns];
            for (int i = 0; i < ns; i++) {
                double p = 1;
                for (int j = 1; j < winLen; j++) {
                    p += 2 * ac[j] * Math.cos(Constants.TWOPI * i * j / len);
                }
                if (p < 0) {
                    p = 0;
                }
                s[i] = p;
            }
            return s;
        }

    }

    private final double[] p;
    private final int resolution;

    private SmoothedPeriodogram(double[] p, int resolution) {
        this.p = p;
        this.resolution = resolution;
    }

    public double getSpectrumValue(double freq) {
        int ipos = (int) Math.round(freq * resolution / Constants.TWOPI);
        if (ipos == p.length) {
            ipos = p.length - 1;
        }
        if (ipos < 0 || ipos >= p.length) {
            return Double.NaN;
        } else {
            return p[ipos];
        }
    }

}
