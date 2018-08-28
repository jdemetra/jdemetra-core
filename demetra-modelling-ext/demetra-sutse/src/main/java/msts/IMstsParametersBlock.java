/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msts;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametersDomain;

/**
 *
 * @author palatej
 */
public interface IMstsParametersBlock {
    
    boolean isFixed();
    
    /**
     * Reads the parameters and transforms them into a suitable input for the builders.
     * @param reader The current parameters
     * @param buffer The buffer with the transformed + fixed parameters
     * @param pos The current position in the buffer
     * @return The new position in the buffer
     */
    int decode(DoubleReader reader, double[] buffer, int pos);

    /**
     * Transforms true parameters into function parameters (skipping fixed parameters)
     * @param reader
     * @param buffer
     * @param pos
     * @return 
     */
    int encode(DoubleReader reader, double[] buffer, int pos);

    IParametersDomain getDomain();
    
    /**
     * Fill the default parameters (without fixed parameters)
     * @param buffer
     * @param pos
     * @return 
     */
    int fillDefault(double[] buffer, int pos);
    
}
