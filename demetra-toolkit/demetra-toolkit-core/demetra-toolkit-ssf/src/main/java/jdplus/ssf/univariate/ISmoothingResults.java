/*
 * Copyright 2015 National Bank of Belgium
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
 /*
 */
package jdplus.ssf.univariate;

import jdplus.data.DataBlock;
import jdplus.ssf.IStateResults;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author PCUser
 */
public interface ISmoothingResults extends IStateResults {

    DataBlock a(int pos);

    Matrix P(int pos);

    void rescaleVariances(double factor);

    DoubleSeq getComponent(int pos);

    DoubleSeq getComponentVariance(int pos);

    boolean hasVariances();

    default void saveSmoothation(int pos, double u, double uvar) {
    }

    default void saveR(int pos, DataBlock r, Matrix rvar) {
    }
    
    default DoubleSeq R(int pos) {
        throw new java.lang.UnsupportedOperationException();
    }

    default Matrix RVariance(int pos) {
        throw new java.lang.UnsupportedOperationException();
    }

    default double smoothation(int pos) {
        throw new java.lang.UnsupportedOperationException();
    }

    default DoubleSeq smoothations() {
        throw new java.lang.UnsupportedOperationException();
    }

    default double smoothationVariance(int pos) {
        throw new java.lang.UnsupportedOperationException();
    }
}
