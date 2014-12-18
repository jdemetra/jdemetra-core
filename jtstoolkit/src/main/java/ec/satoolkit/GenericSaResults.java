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

package ec.satoolkit;

import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcDocument;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.tstoolkit.modelling.arima.PreprocessingModel;

/**
 *
 * @author Jean Palate
 */
public class GenericSaResults {

    public static SingleTsData getInput(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(IProcDocument.INPUT, SingleTsData.class);
    }

    public static ISeriesDecomposition getFinalDecomposition(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.FINAL, ISeriesDecomposition.class);
    }

    public static PreprocessingModel getPreprocessingModel(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.PREPROCESSING, PreprocessingModel.class);
    }

    public static <S extends ISaResults> S getDecomposition(CompositeResults rslts, Class<S> sclass) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.DECOMPOSITION, sclass);
    }

    public static SaBenchmarkingResults getBenchmarking(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.BENCHMARKING, SaBenchmarkingResults.class);
    }
}
