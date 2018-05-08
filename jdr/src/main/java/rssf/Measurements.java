/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Measurements {
    ISsfMeasurement of(int mpos, double var){
        return Measurement.create(mpos, var);
    }
}
