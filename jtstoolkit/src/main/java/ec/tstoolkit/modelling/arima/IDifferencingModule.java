/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.data.IReadDataBlock;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IDifferencingModule {
    
    /**
     * Sets the upper bounds for differencing
     * @param maxd Maximum of the regular differencing. Should be strictly greater than 0
     * @param maxbd Maximum of the seasona differencing. Could be 0 (non seasonal models)
     */
    void setLimits(int maxd, int maxbd);
    
    /**
     * Analyses a given set of data.
     * @param data The considered data. They should not contain missing values
     * @param freq The annual frequency (used for seasonal differencing)
     * @param d0 The initial regular differencing order. The final differencing order will 
     * be greater or equal to d0.
     * @param bd0 The initial seasonal differencing order. The final differencing order will 
     * be greater or equal to bd0.
     */
    void process(IReadDataBlock data, int freq, int d0, int bd0);
    
    /**
     * Analyses a given set of data.
     * @param data The considered data. They should not contain missing values
     * @param freq The annual frequency (used for seasonal differencing)
     */
    default void process(IReadDataBlock data, int freq){
        process(data, freq, 0, 0);
    }

    /**
     * Gets the regular differencing order
     * @return The regular differencing order;
     */
    int getD();
    
    /**
     * Gets the seasonal differencing order
     * @return The seasonal differencing order;
     */
    int getBD();
    
    /**
     * Checks that the model needs a mean correction 
     * @return True if a mean correction is needed, false otherwise.
     */
    boolean isMeanCorrection();
}
