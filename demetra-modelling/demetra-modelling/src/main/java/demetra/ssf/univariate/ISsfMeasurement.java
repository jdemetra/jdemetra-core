/*
 * Copyright 2015 National Bank copyOf Belgium
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
package demetra.ssf.univariate;

import demetra.ssf.ISsfLoading;
import demetra.ssf.ISsfRoot;

/**
 *
 * @author Jean Palate
 */
public interface ISsfMeasurement extends ISsfRoot {
    
    ISsfLoading loading();
    
    ISsfError error();
    
    default boolean hasError(){
        return error() != null;
    }
    
    @Override
    default boolean isTimeInvariant(){
        return loading().isTimeInvariant() && (! hasError() || error().isTimeInvariant());
    }

}
