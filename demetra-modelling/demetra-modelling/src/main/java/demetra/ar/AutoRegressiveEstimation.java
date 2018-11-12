/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ar;

import demetra.ar.internal.BurgAlgorithm;
import demetra.ar.internal.LevinsonAlgorithm;
import demetra.ar.internal.OlsAlgorithm;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AutoRegressiveEstimation {
    public IAutoRegressiveEstimation levinson(){
        return new LevinsonAlgorithm();
    }

    public IAutoRegressiveEstimation ols(){
        return new OlsAlgorithm();
    }
    
    public IAutoRegressiveEstimation burg(){
        return new BurgAlgorithm();
    }
    
}
