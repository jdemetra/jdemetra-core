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


package jdplus.regsarima.regular;

import nbbrd.design.Development;
import jdplus.arima.IArimaModel;
import jdplus.regarima.RegArimaEstimation;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface IModelComparator {

    /**
     * 
     * @param <M>
     * @param reference Reference model. Could be null
     * @param models Alternative models
     * @return -1 if the preferred model is the reference, 
     * the index of the alternative model in the array otherwise
     */
    <M extends IArimaModel> int compare(RegArimaEstimation<M> reference, @NonNull RegArimaEstimation<M>[] models);

    /**
     * 
     * @param reference Reference model
     * @param alternative Alternative model
     * @return -1 if the preferred model is the reference, 0 otherwise
     */
    <M extends IArimaModel> int compare(@NonNull RegArimaEstimation<M> reference, @NonNull RegArimaEstimation<M> alternative);
}
