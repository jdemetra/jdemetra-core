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

package ec.tstoolkit.algorithm;

import ec.tstoolkit.IDocumented;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSetSerializable;

/**
 * IProcDocument describes an object that contains information related to a
 * processing. 
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IProcDocument<S extends IProcSpecification, I, R extends IProcResults> extends InformationSetSerializable, IDocumented {

    public static final String INPUT = "input", SPEC = "specification", ALGORITHM = "algorithm", RESULTS = "results", METADATA="metadata";

    I getInput();
    
    S getSpecification();
 
    R getResults();

    long getKey();

    String getDescription();
    
}
