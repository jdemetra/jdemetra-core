/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.spi;

import demetra.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.regarima.RegArimaSpec;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.x11.X11Spec;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author palatej
 */
@Algorithm
@ServiceDefinition
public interface X11Processor {

    ProcResults process(@NonNull TsData series, @NonNull X11Spec spec, ModellingContext context);
}
