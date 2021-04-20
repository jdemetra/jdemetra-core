/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sa;

import demetra.information.InformationSet;
import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcQuality;
import demetra.processing.ProcResults;
import demetra.processing.ProcessingLog;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder( toBuilder=true)
public class SaEstimation {
    
    /**
     * Results of the estimation
     */
    ProcResults results;
    
    /**
     * ProcessingLog. Could be null
     */
    ProcessingLog log;
   
    @lombok.Singular
    List<ProcDiagnostic>  diagnostics;
    
    /**
     * Quality of the current results
     */
    ProcQuality quality;
   
    /**
     * Specification corresponding to the results of the current estimation (fully identified model)
     */
    SaSpecification pointSpec;
    
    /**
     * Warnings on the current estimation
     * @return 
     */
    public List<String> warnings(){
        List<String> warnings=new ArrayList<>();
        for (ProcDiagnostic diag : diagnostics)
            warnings.addAll(diag.getWarnings());
        return warnings;
    }
}
