/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.fractionalairline;

import jdplus.arima.ArimaModel;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import demetra.data.DoubleSeq;
import jdplus.arima.IArimaModel;
import jdplus.math.matrices.Matrix;
import jdplus.ucarima.AllSelector;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class PeriodicAirlineEngine {

    public RegArimaEstimation<ArimaModel> process(DoubleSeq y, Matrix x, double[] periods, double precision) {
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(periods, true, false);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(precision)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.<ArimaModel>builder()
                .y(y)
                .addX(x)
                .arima(mapping.getDefault())
                .build();
        return processor.process(regarima, mapping);
    }

    public RegArimaEstimation<ArimaModel> process(DoubleSeq y, Matrix x, double period, double precision) {
        final PeriodicAirlineMapping mapping = new PeriodicAirlineMapping(period, true, false);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(precision)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.<ArimaModel>builder()
                .y(y)
                .addX(x)
                .arima(mapping.getDefault())
                .build();
        return processor.process(regarima, mapping);
    }

    public static UcarimaModel ucm(IArimaModel arima, boolean sn) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        if (sn) {
            ucm = ucm.setVarianceMax(0, false);
        } else {
            ucm = ucm.setVarianceMax(-1, false);
        }
        return ucm;
    }
}
