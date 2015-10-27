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
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x13.X13Specification;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.X13Processor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;

/**
 *
 * @author Jean Palate
 */
public class X13Document extends SaDocument<X13Specification> implements Cloneable {

    public X13Document() {
        super(SaManager.instance.getProcessor(X13Processor.DESCRIPTOR));
        setSpecification(new X13Specification());
    }

    public X13Document(ProcessingContext context) {
        super(SaManager.instance.getProcessor(X13Processor.DESCRIPTOR), context);
        setSpecification(new X13Specification());
    }
    
    @Override
    public X13Document clone() {
        return (X13Document) super.clone();
    }

    @Override
    public X11Results getDecompositionPart() {
        CompositeResults rslts = getResults();
        if (rslts == null) {
            return null;
        } else {
            return GenericSaResults.getDecomposition(rslts, X11Results.class);
        }
    }

    public Mstatistics getMStatistics() {
        CompositeResults rslts = getResults();
        if (rslts == null) {
            return null;
        } else {
            return rslts.get(X13ProcessingFactory.MSTATISTICS, Mstatistics.class);
        }
    }
}
