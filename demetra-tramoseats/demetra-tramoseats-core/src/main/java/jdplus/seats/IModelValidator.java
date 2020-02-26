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


package jdplus.seats;

import demetra.design.Development;
import demetra.information.InformationSet;
import jdplus.sarima.SarimaModel;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IModelValidator {
    /**
     * Gets the new (valid) model. Should only be called after that the "validate"
     * method returned false
     * @return
     */
    SarimaModel getNewModel();

    /**
     *
     * @param model Current model
     * @return True if the model is valid, false otherwise
     * A new model can be retrieved if the current one is invalid 
     */
    boolean validate(SarimaModel model);
}
