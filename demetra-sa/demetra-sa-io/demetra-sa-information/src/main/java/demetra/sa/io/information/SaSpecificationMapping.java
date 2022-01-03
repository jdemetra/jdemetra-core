/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.information;

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.sa.SaSpecification;
import java.util.List;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author PALATEJ
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE, mutability = Mutability.NONE, singleton = true)
public interface SaSpecificationMapping{
    SaSpecification read(InformationSet info);
    InformationSet write(SaSpecification spec, boolean verbose, DemetraVersion version);
    
    public static SaSpecification of(InformationSet info){
        List<SaSpecificationMapping> all = SaSpecificationMappingLoader.get();
        for (SaSpecificationMapping mapping : all){
            SaSpecification spec=mapping.read(info);
            if (spec != null)
                return spec;
        }
        return null;
    }
    
    public static InformationSet toInformationSet(SaSpecification spec, boolean verbose, DemetraVersion version){
        List<SaSpecificationMapping> all = SaSpecificationMappingLoader.get();
        for (SaSpecificationMapping mapping : all){
            InformationSet info=mapping.write(spec, verbose, version);
            if (info != null)
                return info;
        }
        return null;
    }

}
