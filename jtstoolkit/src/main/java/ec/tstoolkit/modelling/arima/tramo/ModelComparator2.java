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
@Deprecated
public class ModelComparator2 implements Comparator<PreprocessingModel> {

    private double significance_ = .01;
    private double kbic_ = .03;
    private double kq_ = 2;
    private double kqs_ = 1;
    private int mout_ = 2;
    private int kout_ = 4;
    private double knz_ = .05;
    private double ksk = 1;

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

        if (s2.bic < s1.bic) {
            if (isBetter(s2, s1)) {
                return 1;
            } else {
                return -1;
            }
        } else if (isBetter(s1, s2)) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 
     * @param s1
     * @param s2
     * @return True if s1 is better than s2
     */
    private boolean isBetter(ModelStatistics s1, ModelStatistics s2) {
        if (test1(s1, s2)) {
            return false;
        }
        if (test2(s1, s2)) {
            return false;
        }
        if (test3(s1, s2)) {
            return false;
        }
        return true;
    }

    /**
     * 
     * @param s1
     * @param s2
     * @return True if s2 is preferred to s1
     */
    private boolean test1(ModelStatistics s1, ModelStatistics s2) {
        if (s2.bic - s1.bic >= Math.abs(s1.bic) * kbic_) {
            return false;
        }
        if (s1.ljungBoxPvalue >= significance_) {
            return false;
        }
        if (s2.ljungBoxPvalue <= significance_) {
            return false;
        }
        if (s1.ljungBox - s2.ljungBox <= kq_) {
            return false;
        }
        if (s2.outliers - s1.outliers >= kout_) {
            return false;
        }
        if (s2.outliers >= Math.round(s2.nz * (knz_))) {
            return false;
        }
        return true;
    }

    private boolean test2(ModelStatistics s1, ModelStatistics s2) {
        if (s1.bic - s2.bic >= Math.abs(s1.bic) * kbic_) {
            return false;
        }
        if (s2.ljungBoxPvalue <= significance_ && (s1.ljungBoxPvalue <= significance_
                || s2.ljungBox - s1.ljungBox >= kq_)){
            return false;
        }
        if (s1.outliers <= Math.round(s1.nz * (knz_))) {
            return false;
        }
        if (s2.outliers >= Math.round(s2.nz * (knz_))) {
            return false;
        }
        if (s1.outliers - s2.outliers <= mout_) {
            return false;
        }
        return true;
    }

    private boolean test3(ModelStatistics s1, ModelStatistics s2) {
        if (s1.bic - s2.bic >= Math.abs(s1.bic) * kbic_) {
            return false;
        }
        if (s2.ljungBoxPvalue <= significance_ && (s1.ljungBoxPvalue <= significance_
                || s2.ljungBox - s1.ljungBox >= kq_)){
            return false;
        }
        if (s2.outliers - s1.outliers >= kout_) {
            return false;
        }
        if (s2.outliers >= Math.round(s2.nz * (knz_))) {
            return false;
        }
        if (s1.seasLjungBoxPvalue >= significance_)
            return false;
        if (s2.seasLjungBoxPvalue <= significance_)
            return false;
        if (s1.seasLjungBox - s2.seasLjungBox <= 1)
            return false;
       return true;
    }
}
