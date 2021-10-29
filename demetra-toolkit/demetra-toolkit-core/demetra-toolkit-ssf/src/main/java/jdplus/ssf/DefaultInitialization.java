/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf;

import jdplus.data.DataBlock;
import java.util.function.Consumer;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultInitialization implements ISsfInitialization {
    
    private final int dim, ndiffuse;
    private Consumer<DataBlock> a0;
    private Consumer<FastMatrix> Pf, Pi, B;
    
    public DefaultInitialization(int dim, int ndiffuse) {
        this.dim = dim;
        this.ndiffuse = ndiffuse;
    }
    
    public DefaultInitialization a0(Consumer<DataBlock> a0) {
        this.a0 = a0;
        return this;
    }
    
    public DefaultInitialization Pf(Consumer<FastMatrix> Pf) {
        this.Pf = Pf;
        return this;
    }
    
    public DefaultInitialization Pi(Consumer<FastMatrix> Pi) {
        if (ndiffuse == 0) {
            throw new SsfException(SsfException.INITIALIZATION);
        }
        this.Pi = Pi;
        return this;
    }
    
    public DefaultInitialization B(Consumer<FastMatrix> B) {
        if (ndiffuse == 0) {
            throw new SsfException(SsfException.INITIALIZATION);
        }
        this.B = B;
        return this;
    }
    
    @Override
    public int getStateDim() {
        return dim;
    }
    
    @Override
    public boolean isDiffuse() {
        return ndiffuse > 0;
    }
    
    @Override
    public int getDiffuseDim() {
        return ndiffuse;
    }
    
    @Override
    public void diffuseConstraints(FastMatrix b) {
        if (B != null) {
            B.accept(b);
        }
    }
    
    @Override
    public void a0(DataBlock a) {
        if (a0 != null) {
            a0.accept(a);
        }
    }
    
    @Override
    public void Pf0(FastMatrix pf) {
        if (Pf != null) {
            Pf.accept(pf);
        }
    }
    
    @Override
    public void Pi0(FastMatrix pi) {
        if (Pi != null) {
            Pi.accept(pi);
        } else {
            ISsfInitialization.super.Pi0(pi);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (a0 == null) {
            builder.append("a0:").append(System.lineSeparator()).append("0");
        } else {
            DataBlock a = DataBlock.make(dim);
            a0.accept(a);
            builder.append(a.toString(ISsfState.FMT));
        }
        builder.append(System.lineSeparator());
        if (Pf == null) {
            builder.append("Pf0:").append(System.lineSeparator()).append("0");
        } else {
            FastMatrix M = FastMatrix.square(dim);
            Pf.accept(M);
            builder.append(M.toString(ISsfState.FMT));
        }
        builder.append(System.lineSeparator());
        if (B == null) {
            builder.append("B:").append(System.lineSeparator()).append("0");
        } else {
            FastMatrix M = FastMatrix.make(dim, ndiffuse);
            B.accept(M);
            builder.append(M.toString(ISsfState.FMT));
        }
        return builder.toString();
    }
    
}
