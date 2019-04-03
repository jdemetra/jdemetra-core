/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.fractionalairline;

import demetra.arima.ArimaModel;
import demetra.maths.matrices.Matrix;
import demetra.regarima.GlsArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class PeriodicAirlineProcessor {

    public RegArimaEstimation<ArimaModel> process(DoubleSeq y, Matrix x, double[] periods, double precision) {
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

    public RegArimaEstimation<ArimaModel> process(DoubleSeq y, Matrix x, double period, double precision) {
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
