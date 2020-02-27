/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.descriptors.sa;

import demetra.design.Development;
import demetra.information.InformationMapping;
import jdplus.sa.RegArimaDecomposer;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class RegArimaDescriptor {
    private final InformationMapping<RegArimaDecomposer> MAPPING = new InformationMapping<>(RegArimaDecomposer.class);

    static {
    }

    public InformationMapping<RegArimaDecomposer> getMapping() {
        return MAPPING;
    }
   
    
}
