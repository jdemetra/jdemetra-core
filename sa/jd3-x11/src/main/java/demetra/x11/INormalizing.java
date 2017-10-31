/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package demetra.x11;

import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.simplets.TsData;


/**
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Exploratory)
public interface INormalizing {
    
      /**
     *
     * @return
     */ 
    String getDescription();
    
    /* In Case enter a return value for the used normalizer    */
      
    /**
     *
     * @param s
     * @param domain
     * @param freq
     * @return
     */
    TsData process(TsData s, TsDomain domain, int freq);
    
}
