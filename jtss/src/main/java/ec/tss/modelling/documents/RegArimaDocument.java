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

package ec.tss.modelling.documents;

import ec.tss.documents.TsDocument;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.implementation.RegArimaProcessingFactory;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;

/**
 *
 * @author pcuser
 */
public class RegArimaDocument extends TsDocument<RegArimaSpecification, PreprocessingModel> implements Cloneable{
    public RegArimaDocument(){
        super(RegArimaProcessingFactory.instance);
        setSpecification(RegArimaSpecification.RG4.clone(), true);
    }
   
    public RegArimaDocument(ProcessingContext context){
        super(RegArimaProcessingFactory.instance, context);
        setSpecification(RegArimaSpecification.RG4.clone(), true);
    }
    
    @Override
    public RegArimaDocument clone(){
        return (RegArimaDocument) super.clone();
    }
    
}
