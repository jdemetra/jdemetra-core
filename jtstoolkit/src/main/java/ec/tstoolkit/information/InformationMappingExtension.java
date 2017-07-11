/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.information;

import ec.tstoolkit.design.ServiceDefinition;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceDefinition
public interface InformationMappingExtension<S>  {
    
    Class<S> getSourceClass();
    
    boolean updateExtractors(InformationMapping<S> mapping);
    
}
