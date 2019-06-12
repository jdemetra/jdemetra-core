/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.univariate;

import jdplus.ssf.ISsfLoading;
import jdplus.ssf.implementations.MeasurementError;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author palatej
 */
public class Measurement implements ISsfMeasurement {

    private final ISsfLoading loading;
    private final ISsfError error;

    public Measurement(@Nonnull final ISsfLoading loading, @Nullable final ISsfError error) {
        this.loading = loading;
        this.error = error;
    }

    public Measurement(@Nonnull final ISsfLoading loading, final double var) {
        this.loading = loading;
        this.error = MeasurementError.of(var);
    }

    @Override
    public ISsfLoading loading() {
        return loading;
    }

    @Override
    public ISsfError error() {
        return error;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

}
