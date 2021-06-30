/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.sa.SeriesDecomposition;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class SeriesDecompositionExtractor extends InformationMapping<SeriesDecomposition>{

    @Override
    public Class getSourceClass() {
        return SeriesDecomposition.class;
    }

    
}
