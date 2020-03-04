/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package demetra.arima;

import demetra.design.Development;

/**
 * Unobserved components Arima model
 * @author Jean Palate
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class UcarimaModel {

    /**
     * Reduced form of the model
     */
    private ArimaModel sum;
    /**
     * Unobserved components
     */
    @lombok.NonNull 
    private ArimaModel[] components;
    
    /**
     * Number of components
     * @return 
     */
    public int size(){
        return components.length;
    }
    
    /**
     * Gets the ith component
     * @param i 0-based position of the component
     * @return 
     */
    public ArimaModel getComponent(int i){
        return components[i];
    }

}
