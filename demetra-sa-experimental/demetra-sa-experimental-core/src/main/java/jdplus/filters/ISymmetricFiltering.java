/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.DoubleSeq;
import jdplus.math.linearfilters.IFiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ISymmetricFiltering extends IFiltering{
    /**
     * Applies a filter on an input to produce an output.
     * The input and the output must have the same length
     * @param in
     * @return 
     */
    @Override
    DoubleSeq process(DoubleSeq in);
    
    SymmetricFilter symmetricFilter();
    
    @Override
    default IFiniteFilter centralFilter(){
        return symmetricFilter();
    }
    
    IFiniteFilter[] endPointsFilters();
    
    @Override
    default IFiniteFilter[] leftEndPointsFilters(){
        IFiniteFilter[] lf=endPointsFilters().clone();
        for (int i=0; i<lf.length; ++i){
            lf[i]=lf[i].mirror();
        }
        return lf;
    }

    @Override
    default IFiniteFilter[] rightEndPointsFilters(){
        return endPointsFilters();
    }
}
