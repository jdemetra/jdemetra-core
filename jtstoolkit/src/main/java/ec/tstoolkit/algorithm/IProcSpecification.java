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

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;

/**
 *
 * @author pcuser
 */
public interface IProcSpecification extends InformationSetSerializable{

    public static final String ALGORITHM = "algorithm";
    public static final IProcSpecification EMPTY = new EmptySpecification();

    public IProcSpecification clone();
}

class EmptySpecification implements IProcSpecification {

    @Override
    public IProcSpecification clone() {
        return this;
    }

    @Override
    public InformationSet write(boolean verbose) {
        return null;
    }

    @Override
    public boolean read(InformationSet info) {
        return true;
    }
}
