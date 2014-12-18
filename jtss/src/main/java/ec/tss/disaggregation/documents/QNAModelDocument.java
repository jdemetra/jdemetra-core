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
package ec.tss.disaggregation.documents;

import ec.tss.disaggregation.processors.DisaggregationProcessor;
import ec.tss.disaggregation.processors.QNAProcessor;
import ec.tss.documents.ActiveDocument;
import ec.tss.documents.MultiTsDocument;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;

/**
 *
 * @author Jean Palate
 */
public class QNAModelDocument extends ActiveDocument<QNAModelSpecification, QNAModel, CompositeResults> {
    
    public static final String DISAGGREGATION="disaggregation", QNA="QNA";

    public QNAModelDocument() {
        super(QNA);
        setSpecification(new QNAModelSpecification(), true);
    }

    public QNAModelDocument(ProcessingContext context) {
        super(QNA, context);
        setSpecification(new QNAModelSpecification(), true);
    }
    
    public DisaggregationResults disaggregationResults(){
        CompositeResults results = getResults();
        if (results == null)
            return null;
        else
            return results.get(DISAGGREGATION, DisaggregationResults.class);
    } 

    @Override
    protected CompositeResults recalc(QNAModelSpecification spec, QNAModel input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
