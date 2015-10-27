/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tss.sa.documents;

import ec.satoolkit.GenericSaResults;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;


/**
 *
 * @author Jean Palate
 */
public class TramoSeatsDocument extends SaDocument<TramoSeatsSpecification> implements Cloneable{
    public TramoSeatsDocument(){
        super(SaManager.instance.getProcessor(TramoSeatsProcessor.DESCRIPTOR));
        setSpecification(new TramoSeatsSpecification());
    }
   
    public TramoSeatsDocument(ProcessingContext context){
        super(SaManager.instance.getProcessor(TramoSeatsProcessor.DESCRIPTOR), context);
        setSpecification(new TramoSeatsSpecification());
    }
    
    @Override
    public TramoSeatsDocument clone(){
        return (TramoSeatsDocument) super.clone();
    }
    
   
    @Override
    public SeatsResults getDecompositionPart(){
        CompositeResults rslts=getResults();
        if (rslts == null)
            return null;
        else
            return GenericSaResults.getDecomposition(rslts, SeatsResults.class);
    }
    
    
}
