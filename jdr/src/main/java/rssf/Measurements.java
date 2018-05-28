/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.DataBlock;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.implementations.TimeInvariantMeasurement;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Measurements {
    public ISsfMeasurement of(int mpos, double var){
        return Measurement.create(mpos, var);
    }
    
    public ISsfMeasurement of(double[] Z, double var){
        return new TimeInvariantMeasurement(DataBlock.ofInternal(Z), var);
    }
}
