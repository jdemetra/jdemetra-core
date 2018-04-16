/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.information;

import ec.tstoolkit.design.ServiceDefinition;


/**
 * New implementation for JD3
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceDefinition
public interface InformationMappingExtension<S>  {
    
    Class<S> getSourceClass();
    
    boolean updateExtractors(InformationMapping<S> mapping);
    
}
