/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.DoubleSeq;
import jdplus.maths.linearfilters.IFiniteFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IFiltering {
    /**
     * Applies a filter on an input to produce an output.
     * The input and the output must have the same length
     * @param in
     * @return 
     */
    DoubleSeq process(DoubleSeq in);
    
    IFiniteFilter centralFilter();
    
    IFiniteFilter[] leftEndPointsFilters();
    IFiniteFilter[] rightEndPointsFilters();
}
