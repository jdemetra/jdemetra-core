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
import ec.satoolkit.special.GeneralizedAirlineResults;
import ec.satoolkit.special.GeneralizedAirlineSpecification;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.GeneralizedAirlineProcessor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;

/**
 *
 * @author Jean Palate
 */
public class GeneralizedAirlineDocument extends SaDocument<GeneralizedAirlineSpecification> implements Cloneable {

    public GeneralizedAirlineDocument() {
        super(SaManager.instance.getProcessor(GeneralizedAirlineProcessor.DESCRIPTOR));
        setSpecification(new GeneralizedAirlineSpecification());
    }

    public GeneralizedAirlineDocument(ProcessingContext context) {
        super(SaManager.instance.getProcessor(GeneralizedAirlineProcessor.DESCRIPTOR), context);
        setSpecification(new GeneralizedAirlineSpecification());
    }
    
    @Override
    public GeneralizedAirlineDocument clone() {
        return (GeneralizedAirlineDocument) super.clone();
    }

    @Override
    public GeneralizedAirlineResults getDecompositionPart() {
        CompositeResults rslts = getResults();
        return GenericSaResults.getDecomposition(rslts, GeneralizedAirlineResults.class);
    }
}