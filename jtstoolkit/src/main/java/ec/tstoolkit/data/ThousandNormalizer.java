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
 *
 * @author Jean Palate
 */
public class ThousandNormalizer implements IDataNormalizer {

    private static final double D_MAX=1e8, D_MIN=1e-6;

    private final double dmax_, dmin_;
    private int k_;
    private double factor_;
    private double[] y_;

    private void clear(){
        factor_=1;
        k_=0;
        y_=null;

    }

    public ThousandNormalizer(){
        dmin_=D_MIN;
        dmax_=D_MAX;
    }

    /**
     * Scaling of data, except if all data (in abs) are in the range[dmin, dmax];
     * @param dmin_
     * @param dmax_
     */
    public ThousandNormalizer(final double dmin, final double dmax){
        this.dmin_=dmin;
        this.dmax_=dmax;
    }

    public boolean process(IReadDataBlock data) {
        clear();
        int i = 0;
        y_=new double[data.getLength()];
        data.copyTo(y_, 0);
        while (i < y_.length && !DescriptiveStatistics.isFinite(y_[i])) {
            ++i;
        }
        if (i == y_.length) {
            return false;
        }
        double ymax = y_[i++], ymin = ymax;
        for (; i < y_.length; ++i) {
            if (DescriptiveStatistics.isFinite(y_[i])) {
                double ycur = Math.abs(y_[i]);
                if (ycur < ymin) {
                    ymin = ycur;
                } else if (ycur > ymax) {
                    ymax = ycur;
                }
            }
        }
        k_ = 0;
        if (ymax < dmax_ && ymin > dmin_) {
            return false;
        }
        while (ymin > 1e3) {
            --k_;
            ymin /= 1000;
        }
        while (ymax < 1e-1) {
            ++k_;
            ymax *= 1000;
        }
        if (k_ != 0) {
            factor_ = 1;
            for (i = 0; i < k_; ++i) {
                factor_ *= 1000;
            }
            for (i = k_; i < 0; ++i) {
                factor_ /= 1000;
            }

            for (i = 0; i < y_.length; ++i) {
                double ycur = Math.abs(y_[i]);
                if (!Double.isNaN(ycur)) {
                    y_[i] = factor_ * ycur;
                }
            }
        }
        return true;
    }

    public double getFactor() {
        return factor_;
    }

    public double[] getNormalizedData() {
        return y_;
    }

    public int getUnits(){
        return k_;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return dmax_;
    }

    /**
     * @return the min
     */
    public double getMin() {
        return dmin_;
    }

}
