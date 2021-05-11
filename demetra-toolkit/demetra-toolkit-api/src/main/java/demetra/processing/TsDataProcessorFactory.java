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

import nbbrd.design.Development;
import demetra.timeseries.TsData;

/**
 * A processing factory is an object that is able to generate a 
 * processing for a given specification S. 
 * The processing itself will accept input of type I for generating output of type R
 * @author Jean Palate
 * @param <S> Specification class
 * @param <R> Results (output) class
 */
@Development(status = Development.Status.Exploratory)
public interface TsDataProcessorFactory<S extends ProcSpecification, R>  extends
       ProcessorFactory<S, TsData, R>  {

    /**
     * Generates the processing related to given specifications
     * @param specification The specifications (=input) of the processing 
     * @return The results (=output) of the processing. May be null.
     */
    @Override
    TsDataProcessor<R> generateProcessor(S specification);
    
}
