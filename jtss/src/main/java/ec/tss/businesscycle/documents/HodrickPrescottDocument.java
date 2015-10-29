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

package ec.tss.businesscycle.documents;

import ec.tss.businesscycle.processors.HodrickPrescottProcessingFactory;
import ec.satoolkit.ISeriesDecomposition;
import ec.tss.documents.TsDocument;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;

/**
 *
 * @author Jean Palate
 */
public class HodrickPrescottDocument extends TsDocument<HodrickPrescottSpecification, CompositeResults> {

    public HodrickPrescottDocument() {
        super(new HodrickPrescottProcessingFactory());
        setSpecification(new HodrickPrescottSpecification());
    }

    public HodrickPrescottDocument(ProcessingContext context) {
        super(new HodrickPrescottProcessingFactory(), context);
        setSpecification(new HodrickPrescottSpecification());
    }

    @Override
    public HodrickPrescottDocument clone() {
        return (HodrickPrescottDocument) super.clone();
    }

    public BusinessCycleDecomposition getDecompositionPart() {
        CompositeResults rslts = getResults();
        return rslts.get(HodrickPrescottProcessingFactory.BC, BusinessCycleDecomposition.class);
    }

    public ISeriesDecomposition getSaPart() {
        CompositeResults rslts = getResults();
        return rslts.get(HodrickPrescottProcessingFactory.SA, ISeriesDecomposition.class);
    }
}
