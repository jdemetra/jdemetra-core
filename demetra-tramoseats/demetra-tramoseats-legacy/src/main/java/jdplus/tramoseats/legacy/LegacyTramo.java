/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.legacy;

import demetra.arima.SarimaModel;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.tramo.TramoProcessor;
import demetra.tramo.TramoSpec;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import java.util.List;

/**
 *
 * @author palatej
 */
public class LegacyTramo implements TramoProcessor.Computer{

    @Override
    public LinearModelEstimation<SarimaModel> compute(TsData series, TramoSpec spec, ModellingContext context, List<String> addtionalItems) {

        TramoSpecification lspec = LegacyUtility.toLegacy(spec);
        IPreprocessor ltramo = lspec.build();
        PreprocessingModel process = ltramo.process(LegacyUtility.toLegacy(series), null);
        return LegacyUtility.toApi(process);
    }
    
}
