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

package ec.tstoolkit.data;

/**
 * This class implements the Durbin's algorithm, which fits recursively
 * auto-regressive models
 *
 * @author Jean Palate
 */
public class DurbinAlgorithm {

    private double[] cxx_;//, cn_;
    private double sd, aic;
    private double[] x_;
    private double[] a_;
    private static final double SMALL = 1e-9;
    private boolean mean_ = true;
    private ITaper taper_;

    public double getAutocovariance(int lag) {
        return cxx_[lag];
    }

    public int getMaxLag() {
        return cxx_ == null ? 0 : cxx_.length - 1;
    }

    public void setTaper(ITaper taper) {
        taper_ = taper;
    }

    public ITaper getTaper() {
        return taper_;
    }

    public boolean solve(IReadDataBlock x, int l) {
        x_ = new double[x.getLength()];
        x.copyTo(x_, 0);
        return calc(l);
    }
    
    public double[] getCoefficients(){
        return a_;
    }
    
    public double getAIC(){
        return aic;
    }
    
    public double getInnovationVariance(){
        return sd;
    }
    
    public boolean isMeanCorrection(){
        return mean_;
    }
    
    public void setMeanCorrection(boolean mean){
        mean_=mean;
    }

    private boolean calc(int l) {
        if (!checkMean()) {
            return false;
        }
        if (taper_ != null) {
            taper_.process(x_);
        }
        if (!calcCov(l)) {
            return false;
        }
        iterate(l);
        //a_ = Toeplitz.solveDurbinSystem(cxx_);
        return true;
    }

    private boolean checkMean() {
        double sum = 0;
        int nm = 0;
        for (int i = 0; i < x_.length; ++i) {
            if (!Double.isFinite(x_[i])) {
                ++nm;
            } else if (mean_) {
                sum += x_[i];
            }
        }
        if (nm == x_.length) {
            return false;
        }
        if (sum != 0) {
            sum /= (x_.length - nm);
            for (int i = 0; i < x_.length; ++i) {
                if (Double.isFinite(x_[i])) {
                    x_[i] -= sum;
                }
            }
        }
        return true;
    }

    private boolean calcCov(int l) {
        cxx_ = new double[l + 1];
        for (int i = 0; i <= l; ++i) {
            cxx_[i] = DescriptiveStatistics.cov(i, x_);
        }
        double v = cxx_[0];
        if (DescriptiveStatistics.isSmall(v)) {
            return false;
        }
//        cn_ = new double[cxx_.length];
//        cn_[0] = 1;
//        for (int i = 1; i <= l; ++i) {
//            cn_[i] = cxx_[i] / v;
//        }
        return true;
    }

    private void iterate(int l) {
        a_ = new double[l];
        int n = x_.length;
        double csd = cxx_[0];
        double caic = n * Math.log(csd);
        aic = caic;
        sd = csd;
        double se = cxx_[1];
        double[] b = new double[l];
        for (int m = 0; m < l; m++) {
            double sdr = csd / cxx_[0];
            if (sdr < SMALL) {
                break;
            }
            double d = se / csd;
            a_[m] = d;
            csd = (1 - d * d) * csd;
            caic = n * Math.log(csd) + 2 * (m + 1);
            if (m != 0) {
                for (int i = 0; i <= m - 1; i++) {
                    a_[i] -= d * b[i];
                }
            }
            for (int i = 0; i <= m; i++) {
                b[i] = a_[m - i];
            }
            if (aic > caic) {
                aic = caic;
                sd = csd;
            }
            if (m != l - 1) {
                se = cxx_[m + 2];
                for (int i = 0; i <= m; i++) {
                    se -= b[i] * cxx_[i + 1];
                }
            }
        }
        
        for (int i = 0; i < l; i++) {
            a_[i] = -a_[i];
        }
    }
}
