/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;

/**
 * This extremvalueCorrector uses period goupe specific standarddeviations for
 * the detection of outliers, used for Calendarsigma.Select of
 * Calendarsigma.Signif if Cochran false
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Exploratory)
public class GroupSpecificExtremeValuesCorrector extends PeriodSpecificExtremeValuesCorrector {

    public GroupSpecificExtremeValuesCorrector() {
        super();
    }

    private SigmavecOption[] sigmavecOption_;

    public void setSigmavecOption(SigmavecOption[] sigmavecOption) {
        sigmavecOption_ = sigmavecOption;
    }

    @Override
    protected void calcStdev() {
//      one value for each group
        //  Integer i = 0;
        Integer np;
        double stdvGroup1 = 0;
        double stdvGroup2 = 0;

        PeriodIterator iteri = new PeriodIterator(scur);
        np = scur.getFrequency().intValue();
        stdev = new double[np];
        //Berechnung der Standardeviation pro gruppe
        int i = 0;
        int nGroup1 = 0; //Count of numbers in group one
        int nGroup2 = 0;//
        double eGroup1 = 0;
        double eGroup2 = 0;
        
//System.out.println(scur);
        
        while (iteri.hasMoreElements()) {
            DataBlock dbi = iteri.nextElement().data;
            if (sigmavecOption_[i].equals(SigmavecOption.Group1)) {
                for (int j = 0; j < dbi.getLength(); j++) {
                    double x = dbi.get(j);
                    if (Double.isNaN(x)) {
                    } else {
                        nGroup1 = nGroup1 + 1;
                        if (isMultiplicative()) {
                            x -= 1;
                        }
                        eGroup1 += x * x;
                    }
                }

            } else if (sigmavecOption_[i].equals(SigmavecOption.Group2)) {
                for (int j = 0; j < dbi.getLength(); j++) {
                    double x = dbi.get(j);
                    if (Double.isNaN(x)) {
                    } else {
                        nGroup2 = nGroup2 + 1;
                        if (isMultiplicative()) {
                            x -= 1;
                        }
                        eGroup2 += x * x;
                    }
                }
            }
            i = i + 1;
        }
        stdvGroup1 = Math.sqrt(eGroup1 / nGroup1);
        stdvGroup2 = Math.sqrt(eGroup2 / nGroup2);
      //  System.out.println("Group1: " +stdvGroup1);
      //         System.out.println("Group2: " +stdvGroup2);
               
        for (i = 0; i < np; i++) {
            if (sigmavecOption_[i].equals(SigmavecOption.Group1)) {
                stdev[i] = stdvGroup1;
            } else if (sigmavecOption_[i].equals(SigmavecOption.Group2)){
                stdev[i] = stdvGroup2;
            }
         
        }

    }
}
