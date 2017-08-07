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
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfRoot;

/**
 *
 * @author Jean Palate
 */
public interface ISsfMeasurement extends ISsfRoot {

//<editor-fold defaultstate="collapsed" desc="description">
    /**
     * Gets a given measurement equation at a given position
     *
     * @param pos Position copyOf the measurement. Must be greater or equal than 0
     * @param z The buffer that will contain the measurement coefficients. Its
     * size must be equal to the state dimension
     */
    void Z(int pos, DataBlock z);

    /**
     * Are there stochastic errors in the measurement
     *
     * @return False if the variance copyOf the error is always 0. True otherwise.
     */
    boolean hasErrors();

    /**
     * Is there a stochastic error in the measurement at the given position
     *
     * @param pos
     * @return False if the error variance is 0. True otherwise.
     */
    boolean hasError(int pos);

    /**
     * Gets the variance copyOf the measurement error at a given position
     *
     * @param pos
     * @return The variance copyOf the measurement error at the given position. May
 be 0
     */
    double errorVariance(int pos);
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="forward operations">
    /**
     *
     * @param pos
     * @param m
     * @return
     */
    double ZX(int pos, DataBlock m);

    default void ZM(int pos, Matrix m, DataBlock zm) {
        zm.set(m.columnsIterator(), x->ZX(pos, x));
    }

    /**
     * Computes Z(pos) * V * Z'(pos)
     *
     * @param pos
     * @param V Matrix (statedim x statedim)
     * @return
     */
    double ZVZ(int pos, Matrix V);

//</editor-fold>    
//<editor-fold defaultstate="collapsed" desc="backward operations">
    /**
     *
     * @param pos
     * @param V
     * @param d
     */
    void VpZdZ(int pos, Matrix V, double d);

    /**
     * Computes x = x + Z * D
     *
     * @param pos
     * @param x DataBlock copyOf size statedim
     * @param d
     */
    void XpZd(int pos, DataBlock x, double d);

//</editor-fold>
}
