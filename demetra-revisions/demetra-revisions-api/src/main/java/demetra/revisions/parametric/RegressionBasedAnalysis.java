/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.revisions.parametric;

import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class RegressionBasedAnalysis {
   
    /**
     * bias[k] contains bias for vintage[k+1]-vintage[k]
     */
    @lombok.Singular("revisionBias")
    List<Bias> revisionBiases;
    
    /**
     * current bias  contains bias for last vintage - preliminary vintage
     */
    Bias currentBias;
    
    Efficiency preminaryEfficiency;
    
}
