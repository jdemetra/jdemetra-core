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

package ec.tss.sa.composite;

import ec.tss.TsCollection;
import ec.tss.documents.ActiveDocument;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;

/**
 *
 * @author Jean Palate
 */
public class MultiSaDocument extends ActiveDocument<MultiSaSpecification, TsCollection, CompositeResults> {

    private final MultiSaProcessingFactory factory;
    
    public MultiSaDocument() {
        super(MultiSaProcessingFactory.DESCRIPTOR.name);
        factory=new MultiSaProcessingFactory();
    }

    public MultiSaDocument(ProcessingContext context) {
        super(MultiSaProcessingFactory.DESCRIPTOR.name, context);
        factory=new MultiSaProcessingFactory();
    }
    
    public void setTsCollection(TsCollection coll) {
        super.setInput(coll);
    }

    @Override
    public InformationSet write(boolean verbose) {
        return null;
    }

    @Override
    public boolean read(InformationSet info) {
        return false;
    }

    @Override
    protected CompositeResults recalc(MultiSaSpecification spec, TsCollection input) {
        return factory.generateProcessing(spec, null).process(input);
    }

    @Override
    public String getDescription() {
        return MultiSaProcessingFactory.DESCRIPTOR.name; //To change body of generated methods, choose Tools | Templates.
    }
}
