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


package ec.satoolkit.seats;

import ec.tstoolkit.design.Development;

/**
 * This interface describes the different modules that form the algorithm
 * "Seats", for computing the canonical decomposition of an SarimaModel
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface ISeatsToolkit {

    /**
     * "Seats" model builder (designed for generating the model that will be
     * decomposed)
     * @return 
     */
    IModelBuilder getModelBuilder();

    /**
     * Gets the bias corrector, which corrects biases that can appear in the
     * case of log-transformations.
     * @return
     */
    IBiasCorrector getBiasCorrector();

    /**
     * Gets the module that will compute the estimates
     * @return
     */
    IComponentsEstimator getComponentsEstimator();

    /**
     * Gets the context of the computation
     * @return
     */
    SeatsContext getContext();

    /**
     * Gets the module that can search for a similar model, when the current
     * one is not decomposable
     * @return
     */
    IModelApproximator getModelApproximator();

    /**
     * Gets the module that will process the canonical decomposition of the model 
     * @return
     */
    IArimaDecomposer getModelDecomposer();

    /**
     * Gets the module that will validate the model being decomposed. Typically,
     * such a module will take care of quasi-unit roots in the MA part of the model
     * @return
     */
    IModelValidator getModelValidator();
}
