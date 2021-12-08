/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.sa.SaDictionary;
import demetra.sa.StationaryVarianceDecomposition;
import jdplus.sa.diagnostics.GenericSaTests;
import jdplus.seats.SeatsTests;
import jdplus.tramoseats.TramoSeatsDiagnostics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class TramoSeatsDiagnosticsExtractor extends InformationMapping<TramoSeatsDiagnostics> {
    
 
 public TramoSeatsDiagnosticsExtractor(){
        delegate("diagnostics", GenericSaTests.class, source -> source.getGenericDiagnostics());
        
        delegate(SaDictionary.VARIANCE, StationaryVarianceDecomposition.class, source -> source.getVarianceDecomposition());
        
        delegate("seats", SeatsTests.class, source -> source.getSpecificDiagnostics());
    }

    @Override
    public Class<TramoSeatsDiagnostics> getSourceClass() {
        return TramoSeatsDiagnostics.class;
    }
    
}
