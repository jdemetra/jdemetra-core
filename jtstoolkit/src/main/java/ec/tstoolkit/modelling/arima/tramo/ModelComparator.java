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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.modelling.arima.ModelStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.util.Comparator;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ModelComparator implements Comparator<PreprocessingModel> {

    public static enum Preference {

        BIC, First, Second
    }
    private final Preference preference_;
    private double significance_ = .01;
    private double kbic_ = .03;
    private double kq_ = 1.1;
    private double kqs_ = 1.1;
    private int mout_ = 2;
    private int kout_ = 4;
    private double knz_ = .05;
    private double ksk_ = 1.25;

    private boolean acceptableQ_, acceptableOut_, acceptableSk_, acceptableQS_, acceptableStab_;

    public ModelComparator() {
        preference_ = Preference.BIC;
    }

    public ModelComparator(Preference pref) {
        preference_ = pref;
    }

    /**
     * @return the significance
     */
    public double getSignificance() {
        return significance_;
    }

    /**
     * @param significance the significance to set
     */
    public void setSignificance(double significance) {
        this.significance_ = significance;
    }

    @Override
    public int compare(PreprocessingModel m1, PreprocessingModel m2) {

        if (m1 == m2) {
            return 0;
        }

        ModelStatistics s1 = new ModelStatistics(m1);
        ModelStatistics s2 = new ModelStatistics(m2);

        if ((preference_ == Preference.BIC && s2.bic < s1.bic) || preference_ == Preference.Second) {
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
        if (s1.ljungBoxPvalue >= significance_) {
            return false;
        }
        if (s1.ljungBox <= s2.ljungBox * kq_) {
            return false;
        }
        return true;
    }

    /**
     * Better outliers
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test2(ModelStatistics s1, ModelStatistics s2) {
        if (s1.outliers <= s1.nz * (knz_)) {
            return false;
        }
        if (s1.outliers <= s2.outliers + mout_) {
            return false;
        }
        return true;
    }

    /**
     * Better QS
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test3(ModelStatistics s1, ModelStatistics s2) {
        if (s1.seasLjungBoxPvalue >= significance_) {
            return false;
        }
        if (s1.seasLjungBox <= s2.seasLjungBox * kqs_) {
            return false;
        }
        return true;
    }

    /**
     * Better Skewness
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test4(ModelStatistics s1, ModelStatistics s2) {
        if (s1.skewnessPvalue >= significance_) {
            return false;
        }
        if (s1.skewnessAbsvalue <= s2.skewnessAbsvalue * ksk_) {
            return false;
        }
        return true;
    }

    /**
     * Better Stability
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean test5(ModelStatistics s1, ModelStatistics s2) {
        return s1.getStabilityScore()>s2.getStabilityScore();
    }
    
    private boolean checkAcceptable(ModelStatistics s1, ModelStatistics s2) {
        if ((s2.bic - s1.bic) >= Math.abs(s1.bic) * kbic_) {
            // deterioration in bic is too high... reject the model
            return false;
        }
        acceptableQ_ = (s2.ljungBox < s1.ljungBox * kq_) || (s2.ljungBoxPvalue > significance_);
        acceptableOut_ = (s2.outliers < s1.outliers + 3) || (s2.outliers < knz_ * s2.nz);
        acceptableSk_ = (s2.skewnessAbsvalue < s1.skewnessAbsvalue * ksk_)
                || (s2.skewnessPvalue > significance_);
        acceptableQS_ = (s2.seasLjungBox < s1.seasLjungBox * kqs_)
                || (s2.seasLjungBoxPvalue > significance_);
        acceptableStab_ = s2.stableMean;
        return acceptableQ_ && acceptableOut_ && acceptableSk_ && acceptableQS_;// && acceptableStab_;
    }

}
