/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IFiltering {
    /**
     * Applies a filter on an input to produce an output.
     * The inut and the output must have the same length
     * @param in
     * @return 
     */
    DoubleSequence process(DoubleSequence in);
}
