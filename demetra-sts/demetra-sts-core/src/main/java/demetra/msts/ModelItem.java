/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DoubleSeqCursor;
import java.util.List;

/**
 *
 * @author palatej
 */
public interface ModelItem {
    String getName();
    
    void addTo(MstsMapping model);
    /**
     * Gets the parameters involved in the component
     * @return 
     */
    List<ParameterInterpreter> parameters();
    
    /**
     * 
     * @param variance
     * @return 
     */
    default boolean isScaleSensitive(boolean variance ){
        List<ParameterInterpreter> parameters = parameters();
        for (ParameterInterpreter p: parameters){
            if (p.isScaleSensitive(variance))
                return true;
        }
        return false;
    }
  
    default int rescaleVariances(double factor, double[] buffer, int pos){
        List<ParameterInterpreter> parameters = parameters();
        int npos=pos;   
        for (ParameterInterpreter p: parameters){
            p.rescaleVariances(factor, buffer, npos);
            npos+=p.getDomain().getDim();
        }
        return npos;
    }
    
}
