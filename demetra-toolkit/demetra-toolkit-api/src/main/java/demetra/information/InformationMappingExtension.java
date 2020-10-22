/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.information;

import nbbrd.design.Development;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
@Development(status = Development.Status.Release)
public interface InformationMappingExtension<S>  {
    
    Class<S> getSourceClass();
    
    boolean updateExtractors(InformationMapping<S> mapping);
    
}
