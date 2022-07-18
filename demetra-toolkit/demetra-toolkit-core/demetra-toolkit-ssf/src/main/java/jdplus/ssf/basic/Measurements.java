/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.basic;

import jdplus.ssf.ISsfLoading;
import jdplus.ssf.multivariate.ISsfErrors;
import jdplus.ssf.multivariate.ISsfMeasurements;

/**
 *
 * @author palatej
 */
public class Measurements implements ISsfMeasurements {

    public static Measurements of(final ISsfLoading[] loadings, final ISsfErrors measurementsError) {
        return new Measurements(loadings, measurementsError);
    }

    private final ISsfLoading[] loadings;
    private final ISsfErrors measurementsError;

    Measurements(final ISsfLoading[] loadings, final ISsfErrors measurementsError) {
        this.loadings = loadings;
        this.measurementsError = measurementsError;
    }

    @Override
    public int getCount() {
        return loadings.length;
    }


    @Override
    public ISsfLoading loading(int equation) {
        return loadings[equation];
    }

    @Override
    public ISsfErrors errors() {
        return this.measurementsError;
    }

    @Override
    public boolean isTimeInvariant() {
        if (this.measurementsError != null && !this.measurementsError.isTimeInvariant()) {
            return false;
        }
        for (int i = 0; i < loadings.length; ++i) {
            if (!loadings[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }
}
