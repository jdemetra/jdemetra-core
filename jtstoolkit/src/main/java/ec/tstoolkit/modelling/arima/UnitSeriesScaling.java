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


package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class UnitSeriesScaling implements ISeriesScaling{

    public UnitSeriesScaling(){
        dmin_=D_MIN;
        dmax_=D_MAX;
   }

   /**
     * Scaling of data, except if all data (in abs) are in the range[dmin, dmax];
     * @param dmin_
     * @param dmax_
     */
    public UnitSeriesScaling(final double dmin, final double dmax){
        this.dmin_=dmin;
        this.dmax_=dmax;
    }

    private static final double D_MAX=1e8, D_MIN=1e-6;

    private final double dmax_, dmin_;
 
    public boolean process(ModellingContext context) {
        if (context.description.getUnits() != 1)
            return false;
         double[] data=context.description.getY();
       int i = 0;
         while (i < data.length && !DescriptiveStatistics.isFinite(data[i])) {
            ++i;
        }
        if (i == data.length) {
            return false;
        }
        double ymax = data[i++], ymin = ymax;
        for (; i < data.length; ++i) {
            if (DescriptiveStatistics.isFinite(data[i])) {
                double ycur = Math.abs(data[i]);
                if (ycur < ymin) {
                    ymin = ycur;
                } else if (ycur > ymax) {
                    ymax = ycur;
                }
            }
        }
        int k=0;
        if (ymax < dmax_ && ymin > dmin_) {
            return true;
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
            double factor = 1;
            for (i = 0; i < k; ++i) {
                factor *= 1000;
            }
            for (i = k; i < 0; ++i) {
                factor /= 1000;
            }
            context.description.setUnit(factor);
        }
        return true;
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
