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


package demetra.processing;

import demetra.algorithms.AlgorithmDescriptor;
import demetra.design.Development;
import demetra.timeseries.TsData;
import java.util.Map;

/**
 * A processing factory is an object that is able to generate a 
 * processing for a given specification S. 
 * The processing itself will accept input of type I for generating output of type R
 * @author Jean Palate
 * @param <S> Specification class
 * @param <R> Results (output) class
 */
@Development(status = Development.Status.Alpha)
public interface ITsDataProcessorFactory<S extends IProcSpecification, R extends IProcResults>  extends
       IProcessorFactory<S, TsData, R>  {

    /**
     * Generates the processing related to given specifications
     * @param specification The specifications (=input) of the processing 
     * @return The results (=output) of the processing. May be null.
     */
    @Override
    ITsDataProcessor<R> generateProcessing(S specification);
    
}
