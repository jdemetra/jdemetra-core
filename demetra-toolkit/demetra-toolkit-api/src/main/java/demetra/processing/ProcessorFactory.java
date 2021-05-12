/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.processing;

import java.util.Collections;
import nbbrd.design.Development;
import java.util.Map;

/**
 * A processor factory is an object that is able to generate a
 * processing for a given specification S.
 * The processing itself will accept input of type I for generating output of
 * type R
 *
 * @author Jean Palate
 * @param <S> Specification class
 * @param <I> Input class
 * @param <R> Results (output) class
 */
@Development(status = Development.Status.Exploratory)
public interface ProcessorFactory<S extends ProcSpecification, I, R> {

    /**
     * Called when the processor is no longer used. Most implementation will be
     * empty.
     */
    default void dispose() {
    }

    boolean canHandle(ProcSpecification spec);

    /**
     * Generates the processor related to given specifications
     *
     * @param specification The specifications (=input) of the processor
     * @return The results (=output) of the processing. May be null.
     */
    Processor<I, R> generateProcessor(S specification);

    /**
     * Returns the output dictionary.
     *
     * @param compact True if the dictionary contains wild cards, false
     * otherwise
     * @return The output dictionary. It might contain id with wildcards ('*' or
     * '?')
     * when compact is set to true.
     * In such case, the user should use the "searchAll" method of the
     * IProcResults
     * for retrieving the right information. Otherwise, the "getData" method
     * should be
     * preferred.
     */
    default Map<String, Class> getOutputDictionary(boolean compact) {
        return Collections.emptyMap();
    }
}
