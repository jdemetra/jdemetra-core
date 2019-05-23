/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.akf;

import jdplus.data.DataBlock;
import demetra.ssf.State;
import demetra.ssf.UpdateInformation;

/**
 *
 * @author Jean Palate
 */
public class AugmentedUpdateInformation extends UpdateInformation {

    /**
     * E is the "prediction error" on the diffuse constraints (=(0-Z(t)A(t))
     */
    private final DataBlock E;

     /**
     *
     * @param ndiffuse
     * @param dim
     */
    public AugmentedUpdateInformation(final int dim, final int ndiffuse) {
        super(dim);
        E = DataBlock.make(ndiffuse);
    }

    public DataBlock E() {
        return E;
    }

    public boolean isDiffuse() {
         return !E.isZero(State.ZERO);
    }


}
