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
public class ThousandNormalizer implements IDataNormalizer, InPlaceNormalizer {

    private static final double D_MAX = 1e3, D_MIN = 1e-3;

    private final double dmax_, dmin_;
    private int k;
    private double factor;
    private double[] y;

    private void clear() {
        factor = 1;
        k = 0;
        y = null;

    }

    public ThousandNormalizer() {
        dmin_ = D_MIN;
        dmax_ = D_MAX;
    }

    /**
     * Scaling of data, except if all data (in abs) are in the range[dmin,
     * dmax];
     *
     * @param dmin
     * @param dmax
     */
    public ThousandNormalizer(final double dmin, final double dmax) {
        this.dmin_ = dmin;
        this.dmax_ = dmax;
    }

    @Override
    public boolean process(IReadDataBlock data) {
        clear();
        int i = 0;
        y = new double[data.getLength()];
        data.copyTo(y, 0);
        while (i < y.length && !Double.isFinite(y[i])) {
            ++i;
        }
        if (i == y.length) {
            return false;
        }
        double ymax = Math.abs(y[i++]), ymin = ymax;
        for (; i < y.length; ++i) {
            if (Double.isFinite(y[i])) {
                double ycur = Math.abs(y[i]);
                if (ycur < ymin) {
                    ymin = ycur;
                } else if (ycur > ymax) {
                    ymax = ycur;
                }
            }
        }
        k = 0;
        if (ymax < dmax_ && ymin > dmin_) {
            return false;
        }
        while (ymin > 1e3) {
            --k;
            ymin /= 1000;
        }
        while (ymax < 1e-1) {
            ++k;
            ymax *= 1000;
        }
        if (k != 0) {
            factor = 1;
            for (i = 0; i < k; ++i) {
                factor *= 1000;
            }
            for (i = k; i < 0; ++i) {
                factor /= 1000;
            }

            for (i = 0; i < y.length; ++i) {
                double ycur = Math.abs(y[i]);
                if (!Double.isNaN(ycur)) {
                    y[i] = factor * ycur;
                }
            }
        }
        return true;
    }

    @Override
    public double getFactor() {
        return factor;
    }

    @Override
    public double[] getNormalizedData() {
        return y;
    }

    public int getUnits() {
        return k;
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

    @Override
    public double normalize(IDataBlock data) {
        int n = data.getLength();
        int i = data.first((x) -> Double.isFinite(x));
        if (i == n) {
            return 1;
        }
        double ymax = data.get(i++), ymin = ymax;
        for (; i < n; ++i) {
            double ycur = data.get(i);
            if (Double.isFinite(ycur)) {
                ycur = Math.abs(ycur);
                if (ycur < ymin) {
                    ymin = ycur;
                } else if (ycur > ymax) {
                    ymax = ycur;
                }
            }
        }
        int q = 0;
        if (ymax < dmax_ && ymin > dmin_) {
            return 1;
        }
        while (ymin < 1e3) {
            ++q;
            ymin *= 1000;
        }
        while (ymax > 1e3) {
            --q;
            ymax /= 1000;
        }
        if (q != 0) {
            double f = 1;
            for (i = 0; i < q; ++i) {
                f *= 1000;
            }
            for (i = q; i < 0; ++i) {
                f /= 1000;
            }
            final double c = f;
            data.apply((x) -> x * c);
            return c;

        } else {
            return 1;
        }
    }

}
