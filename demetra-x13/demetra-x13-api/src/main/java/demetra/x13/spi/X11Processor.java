/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.spi;

import demetra.design.Algorithm;
import demetra.design.ServiceDefinition;
import demetra.modelling.regression.ModellingContext;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.regarima.RegArimaSpec;
import demetra.x11.X11Spec;
import javax.annotation.Nonnull;

/**
 *
 * @author palatej
 */
@Algorithm
@ServiceDefinition
public interface X11Processor {

    ProcResults process(@Nonnull TsData series, @Nonnull X11Spec spec, ModellingContext context);
}
