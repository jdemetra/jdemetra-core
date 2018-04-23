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


package demetra.regarima.ami;

import demetra.design.Development;

/**
 * The Initializer will make some preliminary transformation of the initial model
 * return by the model builder. More especially, it should give a first rapid estimation of the
 * missing values and - possibly - detect rapidly the main outliers
 * the preprocessing model.
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface IRegArimaInitializer {

     boolean initialize(RegArimaContext context);
}
