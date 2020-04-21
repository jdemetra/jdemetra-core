/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.extractors;

import demetra.information.InformationMapping;
import demetra.sa.SeriesDecomposition;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TsDataDecompositionExtractor {
    private final InformationMapping<SeriesDecomposition> MAPPING = new InformationMapping<>(SeriesDecomposition.class);
    
    static{
        
    }
    
    public InformationMapping<SeriesDecomposition> getMapping() {
        return MAPPING;
    }
}
