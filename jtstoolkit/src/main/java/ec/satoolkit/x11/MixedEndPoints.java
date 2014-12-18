/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;


/**
 *
 * @author s4504ch
 */
@Development(status = Development.Status.Exploratory)
public class MixedEndPoints implements IEndPointsProcessor {

    private SeasonalFilterOption[] options;
     private int npoints;
  private int firstperiod; 
  
    
   /**
     * Creates a new end points processor
     * @param options filteroptions to know which are the stabel ones, the the last value of thie perio has to be used
     * @param npoints The number of points that will be copied at the beginning
     * an at the end of the series, if the filter for this period is not stable
     * @param firstperiod FirstPeriod from the dataarray in
     * 
     */
  public MixedEndPoints(SeasonalFilterOption[] options, int npoints, int firstperiod){
        this.options = options;
        this.npoints = npoints;
        this.firstperiod = firstperiod;
    }
  
    @Override
    public void process(DataBlock in, DataBlock out) {
     
        int n = out.getLength();
        int noptions = options.length;
        int nperiodef;
        int nperiodel;
      
       // insert missing values at beginning and at the end
        for (int i = 0; i < npoints; i++) {
           nperiodef= (i+this.firstperiod)% noptions;
            if (options[nperiodef] == SeasonalFilterOption.Stable) {
                out.set(i,out.get(i + noptions ));
 
            } else {
                out.set(i,out.get(npoints));
            }
            nperiodel=(n-npoints+this.firstperiod+i)%noptions;
            if (options[nperiodel] == SeasonalFilterOption.Stable) {
                      out.set(n-npoints+i, out.get(n-npoints+i-noptions ));
            } else {
                out.set(n-npoints+i,out.get(n-npoints-1));
            
            }
            
        }
         
        
          
    
    }
    
}

