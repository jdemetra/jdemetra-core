/*
 * Copyright 2017 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
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
package jdplus.ssf.implementations;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class TimeInvariantDynamics implements ISsfDynamics {

    public static class Innovations {

        private static Innovations of(int stateDim, ISsfDynamics sd) {
            int ne = sd.getInnovationsDim();
            Matrix V = Matrix.square(stateDim);
            sd.V(0, V);
            Matrix S = Matrix.make(stateDim, ne);
            sd.S(0, S);
            return new Innovations(V, S);
        }

        public Innovations(final Matrix V) {
            this.V = V;
            S = null;
        }

        public Innovations(final Matrix V, final Matrix S) {
            this.S = S;
            if (V == null && S != null) {
                this.V = SymmetricMatrix.XXt(S);
            } else {
                this.V = V;
            }
        }

        public final Matrix S, V;
    }

    private final Matrix T;
    private final Matrix V;
    private transient Matrix S;

    public TimeInvariantDynamics(Matrix T, Innovations E) {
        this.T = T;
        this.S = E.S;
        this.V = E.V;

    }

    public static TimeInvariantDynamics of(int stateDim, ISsfDynamics sd) {
        if (!sd.isTimeInvariant()) {
            return null;
        }
        Matrix t = Matrix.square(stateDim);
        sd.T(0, t);
        Innovations e = Innovations.of(stateDim, sd);
        if (e == null) {
            return null;
        }
        return new TimeInvariantDynamics(t, e);
    }

    private synchronized void checkS() {
        if (S == null) {
            S = V.deepClone();
            SymmetricMatrix.lcholesky(S);
        }
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public boolean areInnovationsTimeInvariant() {
        return true;
    }

    @Override
    public int getInnovationsDim() {
        return S == null ? T.getColumnsCount() : S.getColumnsCount();
    }

    @Override
    public void V(int pos, Matrix qm) {
        qm.copy(V);
    }

    @Override
    public boolean hasInnovations(int pos) {
        return V != null;
    }

    @Override
    public void S(int pos, Matrix sm) {
        checkS();
        sm.copy(S);
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        checkS();
        x.addProduct(S.rowsIterator(), u);
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        checkS();
        xs.product(x, S.columnsIterator());
    }

    @Override
    public void T(int pos, Matrix tr) {
        tr.copy(T);
    }

    @Override
    public void TM(int pos, Matrix tm) {
        DataBlock tx = DataBlock.make(T.getColumnsCount());
        DataBlockIterator cols = tm.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            tx.product(T.rowsIterator(), col);
            col.copy(tx);
        } ;
    }

    @Override
    public void TVT(int pos, Matrix tvt) {
        Matrix V = tvt.deepClone();
        SymmetricMatrix.XSXt(V, T, tvt);
    }

    @Override
    public void TX(int pos, DataBlock x) {
        DataBlock tx = DataBlock.make(x.length());
        tx.product(T.rowsIterator(), x);
        x.copy(tx);
    }

    @Override
    public void XT(int pos, DataBlock x) {
        DataBlock tx = DataBlock.make(x.length());
        tx.product(x, T.columnsIterator());
        x.copy(tx);
    }

    @Override
    public void addV(int pos, Matrix p) {
        p.add(V);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("T:\r\n").append(T.toString(FMT)).append(System.lineSeparator());
        builder.append("V:\r\n").append(V.toString(FMT)).append(System.lineSeparator());
        return builder.toString();
    }
    
    private static final String FMT="0.#####";

}
