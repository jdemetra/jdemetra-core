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


package ec.tstoolkit.utilities;

/**
 *
 * @author Jean Palate
 */
public interface InformationExtractor<S, T> {
    /**
     * Retrieves information from a given object
     * @param source The source of information
     * @return The expected information
     */
    T retrieve(S source);
    
    /**
     * Request the extractor to flush any cached information related to a given 
     * source. This method is called when the internal state of the source
     * has changed.
     * @param source The source that has changed 
     */
    void flush(S source);
}
