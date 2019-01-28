/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.information;

import demetra.design.Development;
import demetra.design.ServiceDefinition;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceDefinition
@Development(status = Development.Status.Release)
public interface InformationMappingExtension<S>  {
    
    Class<S> getSourceClass();
    
    boolean updateExtractors(InformationMapping<S> mapping);
    
}
