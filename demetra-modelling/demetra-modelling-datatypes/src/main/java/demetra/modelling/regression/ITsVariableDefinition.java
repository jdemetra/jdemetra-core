/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import javax.annotation.Nonnull;

/**
 * Root of all regression variable definition.
 * All definitions must contain enough information for generating the actual regression variables
 * in a given context (corresponding to a ModellingContext).
 * @author palatej
 */
public interface ITsVariableDefinition {
    
    default int dim(){
        return 1;
    }
    
    public static int dim(@Nonnull ITsVariableDefinition... vars){
        int nvars = 0;
        for (int i=0; i<vars.length; ++i)
            nvars+=vars[i].dim();
        return nvars;
    }
}
