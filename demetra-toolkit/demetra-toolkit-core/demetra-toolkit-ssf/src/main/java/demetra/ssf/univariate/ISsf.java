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
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.ssf.ISsfDynamics;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.SsfComponent;
import demetra.ssf.ISsfState;

/**
 *
 * @author Jean Palate
 */
public interface ISsf extends ISsfState {

    ISsfMeasurement measurement();

    @Override
    default boolean isTimeInvariant() {
        return dynamics().isTimeInvariant() && measurement().isTimeInvariant();
    }
    
    default ISsfLoading loading(){
        return measurement().loading();
    }
    
    default ISsfError measurementError(){
        return measurement().error();
    }
    
    default SsfComponent asComponent(){
        return new SsfComponent(initialization(), dynamics(), loading());
    }
    

//<editor-fold defaultstate="collapsed" desc="auxiliary operations">
    /**
     * Computes X*L, where L = T - Tm/f * z
     *
     * @param pos
     * @param x The row array being modified
     * @param m A Colunm array (usually P*Z')
     * @param f The divisor of m (usually ZPZ'+ H)
     */
    default void xL(int pos, DataBlock x, DataBlock m, double f) {
        // XT - [(XT)*m]/f * z 
        dynamics().XT(pos, x);
        loading().XpZd(pos, x, -x.dot(m) / f);
    }

    default void XL(int pos, FastMatrix M, DataBlock m, double f) {
        // MT - [(MT)*m]/f * z
        ISsfDynamics dynamics = dynamics();
        ISsfLoading loading = loading();
        // Apply XL on each row copyOf M
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()){
            DataBlock row=rows.next();
            dynamics.XT(pos, row);
            loading.XpZd(pos, row, -row.dot(m) / f);
        }
       
    }

    /**
     *
     * @param pos
     * @param x The column array being modified
     * @param m A Colunm array (usually P*Z')
     * @param f The divisor copyOf m (usually ZPZ'+ H)
     */
    default void LX(int pos, DataBlock x, DataBlock m, double f) {
        // TX - T*m/f * z * X
        // TX - T * m * (zX)/f)
        // T (X - m*(zX/f))
        x.addAY(-loading().ZX(pos, x) / f, m);
        dynamics().XT(pos, x);
    }

    default void LM(int pos, FastMatrix M, DataBlock m, double f) {
        // TX - T*m/f * z * X
        // TX - T * m * (zX)/f)
        // T (X - m*(zX/f))
        ISsfDynamics dynamics = dynamics();
        ISsfLoading loading = loading();
        // Apply LX on each column copyOf M
        M.columns().forEach(col -> {
            col.addAY(-loading.ZX(pos, col) / f, m);
            dynamics.XT(pos, col);
        });
    }

    default boolean diffuseEffects(FastMatrix effects) {
        ISsfDynamics dynamics = dynamics();
        ISsfLoading loading = loading();
        ISsfInitialization initializer = initialization();
        int n = initializer.getStateDim();
        int d = initializer.getDiffuseDim();
        if (d == 0 || d != effects.getColumnsCount()) {
            return false;
        }
        FastMatrix matrix = FastMatrix.make(n, d);
        // initialization
        initializer.diffuseConstraints(matrix);
        DataBlockIterator rows = effects.rowsIterator();
        int pos = 0;
        loading.ZM(pos, matrix, rows.next());
        while (rows.hasNext()) {
            // Apply T on matrix and Copy Z*matrix in the current row
            dynamics.TM(pos++, matrix);
            loading.ZM(pos, matrix, rows.next());
        }
        return true;
    }
//</editor-fold>
}
