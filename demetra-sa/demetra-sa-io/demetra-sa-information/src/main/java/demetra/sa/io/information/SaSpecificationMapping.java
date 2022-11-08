/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.information;

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.sa.SaSpecification;
import demetra.timeseries.TsDomain;
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
    SaSpecification read(InformationSet info, TsDomain context);
    InformationSet write(SaSpecification spec, TsDomain context, boolean verbose, DemetraVersion version);
    
    public static SaSpecification of(InformationSet info, TsDomain context){
        List<SaSpecificationMapping> all = SaSpecificationMappingLoader.get();
        for (SaSpecificationMapping mapping : all){
            SaSpecification spec=mapping.read(info, context);
            if (spec != null)
                return spec;
        }
        return null;
    }
    
    public static InformationSet toInformationSet(SaSpecification spec, TsDomain context, boolean verbose, DemetraVersion version){
        List<SaSpecificationMapping> all = SaSpecificationMappingLoader.get();
        for (SaSpecificationMapping mapping : all){
            InformationSet info=mapping.write(spec, context, verbose, version);
            if (info != null)
                return info;
        }
        return null;
    }

}
