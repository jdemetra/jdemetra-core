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
import demetra.data.DataBlockIterator;
import demetra.data.DataWindow;
import demetra.ssf.ISsfDynamics;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsf {

    ISsfMeasurement getMeasurement();

    ISsfDynamics getDynamics();

    int getStateDim();

    boolean isTimeInvariant();

//<editor-fold defaultstate="collapsed" desc="auxiliary operations">
    /**
     * Computes X*L, where L = T(I - m/f * z)
     *
     * @param pos
     * @param x The row array being modified
     * @param m A Colunm array (usually P*Z')
     * @param f The divisor copyOf m (usually ZPZ'+ H)
     */
    default void XL(int pos, DataBlock x, DataBlock m, double f) {
        // XT - [(XT)*m]/f * z 
        getDynamics().XT(pos, x);
        getMeasurement().XpZd(pos, x, -x.dot(m) / f);
    }

    default void ML(int pos, Matrix M, DataBlock m, double f) {
        // MT - [(MT)*m]/f * z
        ISsfDynamics dynamics = getDynamics();
        ISsfMeasurement measurement = getMeasurement();
        // Apply XL on each row copyOf M
        M.rows().forEach(row -> {
            dynamics.XT(pos, row);
            measurement.XpZd(pos, row, -row.dot(m) / f);
        });
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
        x.addAY(-getMeasurement().ZX(pos, x) / f, m);
        getDynamics().XT(pos, x);
    }

    default void LM(int pos, Matrix M, DataBlock m, double f) {
        // TX - T*m/f * z * X
        // TX - T * m * (zX)/f)
        // T (X - m*(zX/f))
        ISsfDynamics dynamics = getDynamics();
        ISsfMeasurement measurement = getMeasurement();
        // Apply LX on each column copyOf M
        M.columns().forEach(col -> {
            col.addAY(-measurement.ZX(pos, col) / f, m);
            dynamics.XT(pos, col);
        });
    }

    default boolean diffuseEffects(Matrix effects) {
        ISsfDynamics dynamics = getDynamics();
        ISsfMeasurement measurement = getMeasurement();
        int n = dynamics.getStateDim();
        int d = dynamics.getNonStationaryDim();
        if (d == 0 || d != effects.getColumnsCount()) {
            return false;
        }
        Matrix matrix = Matrix.make(n, d);
        // initialization
        dynamics.diffuseConstraints(matrix);
        DataBlockIterator rows = effects.rowsIterator();
        int pos = 0;
        measurement.ZM(pos, matrix, rows.next());
        while (rows.hasNext()) {
            // Apply T on matrix and Copy Z*matrix in the current row
            dynamics.TM(pos++, matrix);
            measurement.ZM(pos, matrix, rows.next());
        }
        return true;
    }
//</editor-fold>
}
