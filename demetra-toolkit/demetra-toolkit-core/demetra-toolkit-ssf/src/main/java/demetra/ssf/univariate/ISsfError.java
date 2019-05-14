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

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.ssf.ISsfRoot;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsfError extends ISsfRoot {
//<editor-fold defaultstate="collapsed" desc="description">


    /**
     * Gets the variance copyOf the measurement error at a given position
     *
     * @param pos
     * @return The variance copyOf the measurement error at the given position. May
 be 0
     */
    double at(int pos);
//</editor-fold>
    
}
