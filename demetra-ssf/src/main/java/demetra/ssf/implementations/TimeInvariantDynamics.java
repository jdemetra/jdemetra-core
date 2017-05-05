/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.implementations;

import java.util.Iterator;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;

/**
 *
 * @author Jean Palate
 */
public class TimeInvariantDynamics implements ISsfDynamics {

    public static class Innovations {

        private static Innovations of(ISsfDynamics sd) {
            int n = sd.getStateDim();
            int ne = sd.getInnovationsDim();
            Matrix V = Matrix.square(n);
            sd.V(0, V);
            Matrix S = Matrix.make(n, ne);
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

    public static class Initialization {

        public static Initialization of(ISsfDynamics sd) {
            int n = sd.getStateDim();
            Matrix P0 = Matrix.square(n);
            DataBlock a0 = DataBlock.make(n);
            sd.Pf0(P0);
            sd.a0(a0);
            if (!sd.isDiffuse()) {
                return new Initialization(P0, a0);
            }
            int nd = sd.getNonStationaryDim();
            Matrix B0 = Matrix.make(n, nd);
            Matrix Pi0 = Matrix.square(n);
            sd.diffuseConstraints(B0);
            sd.Pi0(Pi0);
            return new Initialization(P0, B0, Pi0, a0);
        }

        public Initialization(final Matrix P0) {
            this.P0 = P0;
            Pi0 = null;
            B0 = null;
            a0 = null;
        }

        public Initialization(final Matrix P0, final Matrix B0) {
            this.P0 = P0;
            this.B0 = B0;
            Pi0 = SymmetricMatrix.XXt(B0);
            a0 = null;
        }

        public Initialization(final Matrix P0, final Matrix Pi0, final Matrix B0) {
            this.P0 = P0;
            this.Pi0 = Pi0;
            this.B0 = B0;
            a0 = null;
        }

        public Initialization(final Matrix P0, final DataBlock a0) {
            this.P0 = P0;
            Pi0 = null;
            B0 = null;
            this.a0 = a0;
        }

        public Initialization(final Matrix P0, final Matrix B0, final DataBlock a0) {
            this.P0 = P0;
            this.B0 = B0;
            Pi0 = SymmetricMatrix.XXt(B0);
            this.a0 = a0;
        }

        public Initialization(final Matrix P0, final Matrix B0, final Matrix Pi0, final DataBlock a0) {
            this.P0 = P0;
            this.B0 = B0;
            this.Pi0 = Pi0;
            this.a0 = a0;
        }

        public final Matrix P0, B0, Pi0;
        public final DataBlock a0;
    }

    private final Matrix T;
    private final Matrix V;
    private transient Matrix S;

    private final Matrix Pf0, B0;
    private final DataBlock a0;

    public TimeInvariantDynamics(Matrix T, Innovations E, Initialization I) {
        this.T = T;
        this.B0 = I.B0;
        this.Pf0 = I.P0;
        this.a0 = I.a0;
        this.S = E.S;
        this.V = E.V;

    }

    public static TimeInvariantDynamics of(ISsfDynamics sd) {
        if (!sd.isTimeInvariant()) {
            return null;
        }
        int n = sd.getStateDim();
        Matrix t = Matrix.square(n);
        sd.T(0, t);
        Innovations e = Innovations.of(sd);
        if (e == null) {
            return null;
        }
        Initialization i = Initialization.of(sd);
        if (i == null) {
            return null;
        }
        return new TimeInvariantDynamics(t, e, i);
    }

    private synchronized void checkS() {
        if (S == null) {
            S = V.deepClone();
            SymmetricMatrix.lcholesky(S);
        }
    }

    @Override
    public int getStateDim() {
        return T.getColumnsCount();
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getInnovationsDim() {
        return S == null ? getStateDim() : S.getColumnsCount();
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
    public boolean isDiffuse() {
        return B0 != null;
    }

    @Override
    public int getNonStationaryDim() {
        return B0 == null ? 0 : B0.getColumnsCount();
    }

    @Override
    public void diffuseConstraints(Matrix b) {
        if (B0 != null) {
            b.copy(B0);
        }
    }

    @Override
    public boolean a0(DataBlock a0) {
        a0.copy(this.a0);
        return true;
    }

    @Override
    public boolean Pf0(Matrix pf0) {
        pf0.copy(this.Pf0);
        return true;
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
        builder.append("a0:\r\n").append(a0 == null ? "0" : a0.toString(FMT)).append(System.lineSeparator());
        builder.append("P0:\r\n").append(Pf0 == null ? "0" : Pf0.toString(FMT)).append(System.lineSeparator());
        builder.append("B0:\r\n").append(B0 == null ? "0" : B0.toString(FMT)).append(System.lineSeparator());
        return builder.toString();
    }
    
    private static final String FMT="0.#####";

}
