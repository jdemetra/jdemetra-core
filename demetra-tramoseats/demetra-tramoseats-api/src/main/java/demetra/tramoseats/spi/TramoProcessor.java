/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.spi;

import demetra.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramo.TramoSpec;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author palatej
 */
@Algorithm
@ServiceDefinition
public interface TramoProcessor {

    ProcResults process(@NonNull TsData series, @NonNull TramoSpec spec, ModellingContext context);
}
