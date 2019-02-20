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
package demetra.stats.tests;

import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.dstats.DStatException;
import demetra.stats.AutoCovariances;
import demetra.stats.DescriptiveStatistics;
import demetra.stats.StatException;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NiidTests {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(NiidTests.class)
    public static class Builder {

        /**
         *
         * @param period
         * @return
         */
        public static int acTestsLength(int period) {
            int n;
            switch (period) {
                case 12:
                    n = 24;
                    break;
                case 1:
                    n = 8;
                    break;
                default:
                    n = 4 * period;
                    break;
            }
            return n;
        }

        // Data
        private DoubleSequence data;
        private int period, hyperParameters = 0;
        private int k = 24, ks = 2;
        private boolean seasonal = true, defaultTestsLength = true;

        public Builder data(DoubleSequence data) {
            this.data = data;
            return this;
        }

        public Builder period(final int period) {
            this.period = period;
            return this;
        }

        public Builder hyperParametersCount(final int n) {
            this.hyperParameters = n;
            return this;
        }

        public Builder seasonal(final boolean seasonal) {
            this.seasonal = seasonal;
            return this;
        }

        public Builder defaultTestsLength() {
            this.defaultTestsLength = true;
            return this;
        }

        public Builder k(final int k) {
            this.k = k;
            return this;
        }

        public Builder ks(final int ks) {
            this.ks = ks;
            return this;
        }

        public NiidTests build() {
            if (period == 0) {
                throw new IllegalArgumentException();
            }
            int nk = k;
            if (defaultTestsLength) {
                nk = acTestsLength(period);
            }
            return new NiidTests(data, period, hyperParameters, nk, ks, seasonal);
        }
    }

    private double[] ac, ac2;
    private StatisticalTest ljungBox, ljungBoxOnSquares, seasonalLjungBox;
    private StatisticalTest boxPierce, boxPierceOnSquares, seasonalBoxPierce;
    private StatisticalTest mean;
    private StatisticalTest doornikHansen;
    private StatisticalTest skewness;
    private StatisticalTest kurtosis;
    private StatisticalTest runsNumber, runsLength;
    private StatisticalTest upAndDownRunsNumber, upAndDownRunsLength;

    // Data
    private final DoubleSequence data, data2;
    private final DescriptiveStatistics stat, stat2;
    private final int period, hyperParameters;
    private final int k, ks;
    private final boolean seasonal;

    /**
     *
     * @param data
     * @param period
     * @param nhp
     * @param seas
     */
    private NiidTests(final DoubleSequence data, final int period, final int nhp,
            final int k, final int ks, final boolean seas) {
        this.data = data;
        this.stat = DescriptiveStatistics.of(data);
        this.period = period;
        this.seasonal = seas;
        this.hyperParameters = nhp;
        this.k = k;
        this.ks = ks;

        double[] d2 = data.toArray();
        for (int i = 0; i < d2.length; ++i) {
            double cur = d2[i];
            if (Double.isFinite(cur)) {
                d2[i] = cur * cur;
            } else {
                d2[i] = Double.NaN;
            }
        }
        this.data2 = DoubleSequence.ofInternal(d2);
        this.stat2 = DescriptiveStatistics.of(data2);
    }

    /**
     *
     * @return
     */
    public IntToDoubleFunction autoCorrelations() {
        if (ac == null) {
            int kmax = k;
            if (seasonal) {
                kmax = Math.max(k, ks * period);
            }
            ac = new double[kmax];
            {
                IntToDoubleFunction acf = AutoCovariances.autoCorrelationFunction(data, 0);
                for (int i = 0; i < ac.length; ++i) {
                    ac[i] = acf.applyAsDouble(i + 1);
                }
            }
        }
        return k -> ac[k - 1];
    }

    /**
     *
     * @return
     */
    public IntToDoubleFunction autoCorrelationsOnSquares() {
        if (ac2 == null) {
            ac2 = new double[k];
            {
                double mu = Doubles.averageWithMissing(data2);
                IntToDoubleFunction acf = AutoCovariances.autoCorrelationFunction(data2, mu);
                for (int i = 0; i < ac2.length; ++i) {
                    ac2[i] = acf.applyAsDouble(i + 1);
                }
            }
        }
        return k -> ac2[k - 1];
    }

    /**
     *
     * @return
     */
    public StatisticalTest boxPierce() {

        if (boxPierce == null) {
            try {
                boxPierce = new BoxPierce(autoCorrelations(), stat.getObservationsCount())
                        .autoCorrelationsCount(k)
                        .lag(1)
                        .hyperParametersCount(hyperParameters)
                        .build();
            } catch (StatException | DStatException ex) {
                boxPierce = null;
                return null;
            }
        }
        return boxPierce;
    }

    /**
     *
     * @return
     */
    public StatisticalTest boxPierceOnSquare() {
        if (boxPierceOnSquares == null) {
            try {
                boxPierceOnSquares = new BoxPierce(autoCorrelationsOnSquares(), stat.getObservationsCount())
                        .autoCorrelationsCount(k)
                        .lag(1)
                        .hyperParametersCount(hyperParameters)
                        .build();
            } catch (StatException | DStatException ex) {
                boxPierceOnSquares = null;
                return null;
            }
        }
        return boxPierceOnSquares;
    }

    /**
     *
     * @return
     */
    public StatisticalTest ljungBox() {
        if (ljungBox == null) {
            try {
                ljungBox = new LjungBox(autoCorrelations(), stat.getObservationsCount())
                        .autoCorrelationsCount(k)
                        .lag(1)
                        .hyperParametersCount(hyperParameters)
                        .build();
            } catch (StatException | DStatException ex) {
                ljungBox = null;
                return null;
            }
        }
        return ljungBox;
    }

    /**
     *
     * @return
     */
    public StatisticalTest ljungBoxOnSquare() {
        if (ljungBoxOnSquares == null) {
            try {
                ljungBoxOnSquares = new LjungBox(autoCorrelationsOnSquares(), stat.getObservationsCount())
                        .autoCorrelationsCount(k)
                        .lag(1)
                        .hyperParametersCount(hyperParameters)
                        .build();
            } catch (StatException | DStatException ex) {
                ljungBoxOnSquares = null;
                return null;
            }
        }
        return ljungBoxOnSquares;
    }

    /**
     *
     * @return
     *
     */
    public StatisticalTest meanTest() {
        if (mean == null) {
            try {
                mean = Mean.zeroMean(data)
                        .build();
            } catch (StatException | DStatException ex) {
                mean = null;
                return null;
            }
        }
        return mean;
    }

    /**
     *
     * @return
     */
    public StatisticalTest normalityTest() {
        if (doornikHansen == null) {
            try {
                doornikHansen = new DoornikHansen(stat)
                        .build();
            } catch (StatException | DStatException ex) {
                doornikHansen = null;
                return null;
            }
        }
        return doornikHansen;
    }

    /**
     *
     * @return
     */
    public StatisticalTest skewness() {
        if (skewness == null) {
            try {
                skewness = new Skewness(stat)
                        .build();
            } catch (StatException | DStatException ex) {
                skewness = null;
                return null;
            }
        }
        return skewness;
    }

    /**
     *
     * @return
     */
    public StatisticalTest kurtosis() {
        if (kurtosis == null) {
            try {
                kurtosis = new Kurtosis(stat)
                        .build();
            } catch (StatException | DStatException ex) {
                kurtosis = null;
                return null;
            }
        }
        return kurtosis;
    }

    /**
     *
     * @return
     */
    public StatisticalTest runsNumber() {
        if (runsNumber == null) {
            try {
                runsNumber = new TestOfRuns(stat)
                        .testNumber();
            } catch (StatException | DStatException ex) {
                runsNumber = null;
                return null;
            }
        }
        return runsNumber;
    }

    public StatisticalTest runsLength() {
        if (runsLength == null) {
            try {
                runsLength = new TestOfRuns(stat)
                        .testLength();
            } catch (StatException | DStatException ex) {
                runsLength = null;
                return null;
            }
        }
        return runsLength;
    }

    /**
     *
     * @return
     */
    public StatisticalTest upAndDownRunsNumbber() {
        if (upAndDownRunsNumber == null) {
            try {
                upAndDownRunsNumber = new TestOfUpDownRuns(data)
                        .testNumber();
            } catch (StatException | DStatException ex) {
                upAndDownRunsNumber = null;
                return null;
            }
        }
        return upAndDownRunsNumber;
    }

    public StatisticalTest upAndDownRunsLength() {
        if (upAndDownRunsLength == null) {
            try {
                upAndDownRunsLength = new TestOfUpDownRuns(data)
                        .testNumber();
            } catch (StatException | DStatException ex) {
                upAndDownRunsLength = null;
                return null;
            }
        }
        return upAndDownRunsLength;
    }

    /**
     *
     * @return
     */
    public StatisticalTest seasonalBoxPierce() {
        if (!seasonal) {
            return null;
        }
        if (seasonalBoxPierce == null) {
            try {
                seasonalBoxPierce = new BoxPierce(autoCorrelations(), stat.getObservationsCount())
                        .hyperParametersCount(hyperParameters)
                        .lag(period)
                        .autoCorrelationsCount(ks)
                        .usePositiveAutoCorrelations()
                        .build();
            } catch (StatException | DStatException ex) {
                seasonalBoxPierce = null;
                return null;
            }
        }
        return seasonalBoxPierce;
    }

    /**
     *
     * @return
     */
    public StatisticalTest seasonalLjungBox() {
        if (!seasonal) {
            return null;
        }
        if (seasonalLjungBox == null) {
            try {
                seasonalLjungBox = new LjungBox(autoCorrelations(), stat.getObservationsCount())
                        .hyperParametersCount(hyperParameters)
                        .lag(period)
                        .autoCorrelationsCount(ks)
                        .usePositiveAutoCorrelations()
                        .build();
            } catch (StatException | DStatException ex) {
                seasonalLjungBox = null;
                return null;
            }
        }
        return seasonalLjungBox;
    }

}
