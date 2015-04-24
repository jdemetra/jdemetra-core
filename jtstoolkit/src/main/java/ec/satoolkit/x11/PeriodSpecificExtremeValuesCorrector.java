/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.satoolkit.x11;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
//import ec.tstoolkit.timeseries.simplets.YearIterator;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;

/**
 * This extremvalueCorrector uses period specific standarddeviatio 
 * for the detection of outliers, used for Calendarsigma.All of 
 * Calendarsigma.Signif if Cochran false
 * @author Christiane Hofer
 */
    @Development(status = Development.Status.Exploratory)
public class PeriodSpecificExtremeValuesCorrector extends DefaultExtremeValuesCorrector {

        public PeriodSpecificExtremeValuesCorrector() {
            super();
        }
       

    /**
     *calculates the Standarddeviation for each period, 
     */
    @Override
        protected void calcStdev() {
//      one value for each period
            Integer i = 0;
            Integer np;
            PeriodIterator iteri = new PeriodIterator(scur);
            np = scur.getFrequency().intValue();
            stdev = new double[np];
            while (iteri.hasMoreElements()) {
                DataBlock dbi = iteri.nextElement().data;
                stdev[i] = calcStdev(dbi);
                i = i + 1;
            }

        }

        
        @Override
        protected int outliersDetection() {
            int nval = 0;
            sweights = new TsData(scur.getDomain());
            PeriodIterator iteri = new PeriodIterator(scur);
            PeriodIterator itero = new PeriodIterator(sweights);
            sweights.getValues().set(1);

            double xbar = getMean();
            int y = 0;
            while (iteri.hasMoreElements()) {
                double lv = stdev[y] * lsigma;
                double uv = stdev[y] * usigma;
                DataBlock dbi = iteri.nextElement().data;
                DataBlock dbo = itero.nextElement().data;

                for (int i = 0; i < dbi.getLength(); i++) {
                    double tt = Math.abs(dbi.get(i) - xbar);
                    if (tt > uv) {
                        dbo.set(i, 0.0);
                        ++nval;
                    } else if (tt > lv) {
                        dbo.set(i, (uv - tt) / (uv - lv));
                    }
                }
                y++;
            }
            return nval;
         }
        

        /**
         * 
         * @return periodwise standard deviations startingperiod 0
         */
        @Override
        public double[] getStandardDeviations() {
            return super.stdev;
    }
        
    }
