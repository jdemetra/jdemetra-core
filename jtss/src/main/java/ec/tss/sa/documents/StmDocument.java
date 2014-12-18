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
import ec.satoolkit.special.StmResults;
import ec.satoolkit.special.StmSpecification;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.StmProcessor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;

/**
 *
 * @author pcuser
 */
public class StmDocument extends SaDocument<StmSpecification> implements Cloneable {

    public StmDocument() {
        super(SaManager.instance.getProcessor(StmProcessor.DESCRIPTOR));
        setSpecification(new StmSpecification());
    }

    public StmDocument(ProcessingContext context) {
        super(SaManager.instance.getProcessor(StmProcessor.DESCRIPTOR), context);
        setSpecification(new StmSpecification());
    }
    
    @Override
    public StmResults getDecompositionPart() {
        CompositeResults rslts = getResults();
        return GenericSaResults.getDecomposition(rslts, StmResults.class);
    }

    @Override
    public StmDocument clone() {
        return (StmDocument) super.clone();
    }
}
