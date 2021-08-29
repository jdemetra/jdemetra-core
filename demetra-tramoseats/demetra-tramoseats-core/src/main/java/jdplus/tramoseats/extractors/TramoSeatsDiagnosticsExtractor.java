/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.sa.DefaultSaDiagnostics;
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
        delegate("diagnostics", DefaultSaDiagnostics.class, source -> source.getSaDiagnostics());
        
    }

    @Override
    public Class<TramoSeatsDiagnostics> getSourceClass() {
        return TramoSeatsDiagnostics.class;
    }
    
}
