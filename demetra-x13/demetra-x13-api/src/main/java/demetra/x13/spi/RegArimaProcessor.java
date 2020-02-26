/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.spi;

import demetra.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.regarima.RegArimaSpec;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author palatej
 */
@Algorithm
@ServiceDefinition
public interface RegArimaProcessor {

    ProcResults process(@NonNull TsData series, @NonNull RegArimaSpec spec, ModellingContext context);
}
