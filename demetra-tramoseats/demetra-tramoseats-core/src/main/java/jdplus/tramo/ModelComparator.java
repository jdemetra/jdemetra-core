/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.RegSarimaModelling;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ModelComparator {

    public static enum Preference {

        BIC, First, Second
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Preference preference = Preference.BIC;
        private double significance = .01;
        private double kbic = .03;
        private double kq = 1.1;
        private double kqs = 1.1;
        private int mout = 2;
        private int kout = 4;
        private double knz = .05;
        private double ksk = 1.25;

        public Builder preference(Preference preference) {
            this.preference = preference;
            return this;
        }

        public Builder significance(double significance) {
            this.significance = significance;
            return this;
        }

        /**
         * @param bic Should be greater than 0. .03 by default
         * @return
         */
        public Builder bicTolerance(double bic) {
            if (bic < 0) {
                throw new IllegalArgumentException();
            }
            this.kbic = bic;
            return this;
        }

        /**
         * A model is discarded if its Ljung-Box stat is greater than kq times
         * the Ljung-Box of the second one
         *
         * @param kq Should be greater than 1. 1.1 by default
         * @return
         */
        public Builder ljungBoxTolerance(double kq) {
            if (kq < 1) {
                throw new IllegalArgumentException();
            }
            this.kq = kq;
            return this;
        }

        /**
         * A model is discarded if its seasonal Ljung-Box stat is greater than
         * kqs times the seasonal Ljung-Box of the second one
         *
         * @param kqs Should be greater than 1. 1.1 by default
         * @return
         */
        public Builder seasonalLjungBoxTolerance(double kqs) {
            if (kqs < 1) {
                throw new IllegalArgumentException();
            }
            this.kqs = kqs;
            return this;
        }

        /**
         * A model is discarded if its skewness stat is greater than ksk times
         * the skewness of the second one
         *
         * @param ksk Should be greater than 1. 1.25 by default
         * @return
         */
        public Builder skewnessTolerance(double ksk) {
            if (ksk < 1) {
                throw new IllegalArgumentException();
            }
            this.ksk = ksk;
            return this;
        }

        /**
         * Limit for the relative number of outliers, in comparison with the
         * length of the series
         *
         * @param knz In [0, .5[. 0.05 by default
         * @return
         */
        public Builder relativeOutliersThreshold(double knz) {
            if (knz >= .5 || knz < 0) {
                throw new IllegalArgumentException();
            }
            this.knz = knz;
            return this;
        }

        /**
         * A model is discarded if it contains m outliers more than the second
         * one
         *
         * @param m
         * @return
         */
        public Builder outliersTolerance(int m) {
            this.mout = m;
            return this;
        }

        public ModelComparator build() {
            return new ModelComparator(this);
        }

    }

    private final Preference preference;
    private final double significance;
    private final double kbic;
    private final double kq;
    private final double kqs;
    private final int mout;
    private final int kout;
    private final double knz;
    private final double ksk;

    private boolean acceptableQ, acceptableOut, acceptableSk, acceptableQS, acceptableStab;

    public ModelComparator(Builder builder) {
        this.preference = builder.preference;
        this.significance = builder.significance;
        this.kbic = builder.kbic;
        this.kq = builder.kq;
        this.kqs = builder.kqs;
        this.mout = builder.mout;
        this.kout = builder.kout;
        this.knz = builder.knz;
        this.ksk = builder.ksk;
    }

    public int compare(RegSarimaModelling m1, RegSarimaModelling m2) {

        if (m1 == m2) {
            return 0;
        }

        ModelStatistics s1 = ModelStatistics.of(m1.getDescription(), m1.getEstimation().getConcentratedLikelihood());
        ModelStatistics s2 = ModelStatistics.of(m2.getDescription(), m2.getEstimation().getConcentratedLikelihood());

        if ((preference == Preference.BIC && s2.getBic() < s1.getBic()) || preference == Preference.Second) {
            if (!preferSecondModel(s2, s1)) {
                return 1;
            } else {
                return -1;
            }
        } else if (!preferSecondModel(s1, s2)) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Compare two models, with a preference for the first one.
     *
     * @param s1
     * @param s2
     * @return True if s2 is better than s1
     */
    private boolean preferSecondModel(ModelStatistics s1, ModelStatistics s2) {
        if (!checkAcceptable(s1, s2)) {
            return false;
        }
        return test1(s1, s2) || test2(s1, s2) || test3(s1, s2)
                || test4(s1, s2) || test5(s1, s2);
    }

    /**
     * Better Q (LjungBox)
     *
     * @param s1
     * @param s2
     * @return True if s2 is preferred to s1
     */
    private boolean test1(ModelStatistics s1, ModelStatistics s2) {
        if (s1.getLjungBoxPvalue() >= significance) {
            return false;
        }
        return s1.getLjungBox() > s2.getLjungBox() * kq;
    }

    /**
     * Better outliers
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test2(ModelStatistics s1, ModelStatistics s2) {
        if (s1.getOutliersCount() <= s1.getObservationsCount() * (knz)) {
            return false;
        }
        return s1.getOutliersCount() > s2.getOutliersCount() + mout;
    }

    /**
     * Better QS
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test3(ModelStatistics s1, ModelStatistics s2) {
        if (s1.getSeasonalLjungBoxPvalue() >= significance) {
            return false;
        }
        return s1.getSeasonalLjungBox() > s2.getSeasonalLjungBox() * kqs;
    }

    /**
     * Better Skewness
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test4(ModelStatistics s1, ModelStatistics s2) {
        if (s1.getSkewnessPvalue() >= significance) {
            return false;
        }
        return s1.getSkewnessAbsvalue() > s2.getSkewnessAbsvalue() * ksk;
    }

    // high score fore unstable model
    private int stabilityScore(ModelStatistics s) {
        int c = 0;
        if (s.getStableMeanPvalue() < significance) {
            ++c;
        }
        if (s.getStableVariancePvalue() < significance) {
            ++c;
        }
        return c;
    }

    /**
     * Better Stability
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test5(ModelStatistics s1, ModelStatistics s2) {
        return stabilityScore(s1) > stabilityScore(s2);
    }

    private boolean checkAcceptable(ModelStatistics s1, ModelStatistics s2) {
        if ((s2.getBic() - s1.getBic()) >= Math.abs(s1.getBic()) * kbic) {
            // deterioration in bic is too high... reject the model
            return false;
        }
        acceptableQ = (s2.getLjungBox() < s1.getLjungBox() * kq) || (s2.getLjungBoxPvalue() > significance);
        acceptableOut = (s2.getOutliersCount() < s1.getOutliersCount() + 3) || (s2.getOutliersCount() < knz * s2.getObservationsCount());
        acceptableSk = (s2.getSkewnessAbsvalue() < s1.getSkewnessAbsvalue() * ksk)
                || (s2.getSkewnessPvalue() > significance);
        acceptableQS = (s2.getSeasonalLjungBox() < s1.getSeasonalLjungBox() * kqs)
                || (s2.getSeasonalLjungBoxPvalue() > significance);
        acceptableStab = s2.getStableMeanPvalue() > significance;
        return acceptableQ && acceptableOut && acceptableSk && acceptableQS;// && acceptableStab_;
    }

}
