/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.spi;

import demetra.design.Algorithm;
import demetra.design.ServiceDefinition;
import demetra.modelling.regression.ModellingContext;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.tramoseats.TramoSeatsSpec;
import javax.annotation.Nonnull;

/**
 *
 * @author palatej
 */
@Algorithm
@ServiceDefinition
public interface TramoSeats {

    ProcResults process(@Nonnull TsData series, @Nonnull TramoSeatsSpec spec, ModellingContext context);
}
