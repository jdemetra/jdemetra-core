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

import demetra.design.Development;
import java.util.Map;

/**
 * A processing factory is an object that is able to generate a 
 * processing for a given specification S. 
 * The processing itself will accept input of type I for generating output of type R
 * @author Jean Palate
 * @param <S> Specification class
 * @param <I> Input class
 * @param <R> Results (output) class
 */
@Development(status = Development.Status.Alpha)
public interface ProcessorFactory<S extends ProcSpecification, I, R extends ProcResults>  {
    /**
     * Called when the processor is no longer used. Most implementation will be
     * empty.
     */
    void dispose();

    /**
     * Gets the description of the algorithm
     * @return 
     */
    AlgorithmDescriptor getInformation();
    
    boolean canHandle(ProcSpecification spec);

    /**
     * Generates the processing related to given specifications
     * @param specification The specifications (=input) of the processing 
     * @return The results (=output) of the processing. May be null.
     */
    Processor<I,R> generateProcessing(S specification);
    
    /**
     * Returns the output dictionary.
     * @param compact True if the dictionary contains wild cards, false otherwise
     * @return The output dictionary. It might contain id with wildcards ('*' or '?')
     * when compact is set to true.
     * In such case, the user should use the "searchAll" method of the IProcResults
     * for retrieving the right information. Otherwise, the "getData" method should be
     * preferred.
     */
    Map<String, Class> getOutputDictionary(boolean compact);
}
