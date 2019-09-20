/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.spi;

import demetra.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import demetra.processing.ProcResults;
import demetra.modelling.regarima.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.seats.SeatsSpec;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author palatej
 */
@Algorithm
@ServiceDefinition
public interface SeatsProcessor {

    ProcResults process(@NonNull TsData series, @NonNull SarimaSpec arima, @NonNull SeatsSpec spec);
}
