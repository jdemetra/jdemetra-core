/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

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
