/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Ssf {
    demetra.ssf.univariate.ISsf of(final ISsfInitialization initializer, final ISsfDynamics dynamics, ISsfMeasurement measurement){
        return new demetra.ssf.univariate.Ssf(initializer, dynamics, measurement);
    }
    
}
