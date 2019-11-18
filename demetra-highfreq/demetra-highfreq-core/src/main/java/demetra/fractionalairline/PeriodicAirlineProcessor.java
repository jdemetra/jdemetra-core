/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.fractionalairline;

import jdplus.arima.ArimaModel;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.lapack.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class PeriodicAirlineProcessor {

    public RegArimaEstimation<ArimaModel> process(DoubleSeq y, FastMatrix x, double[] periods, double precision) {
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(periods, true, false);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .mapping(mapping)
                .precision(precision)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.builder(ArimaModel.class)
                .y(y)
                .addX(x)
                .arima(mapping.getDefault())
                .build();
        return processor.process(regarima);
    }

    public RegArimaEstimation<ArimaModel> process(DoubleSeq y, FastMatrix x, double period, double precision) {
        final PeriodicAirlineMapping mapping = new PeriodicAirlineMapping(period, true, false);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .mapping(mapping)
                .precision(precision)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.builder(ArimaModel.class)
                .y(y)
                .addX(x)
                .arima(mapping.getDefault())
                .build();
        return processor.process(regarima);

    }

}
