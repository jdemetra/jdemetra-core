/*
 * Copyright 2022 National Bank of Belgium
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
@lombok.Value
@lombok.Builder
public class Initialization implements ISsfInitialization {
    
    private int dim, diffuseDim;
    private double[] a0;
    private FastMatrix Pf, Pi, B;
    
    @Override
    public int getStateDim() {
        return dim;
    }
    
    @Override
    public boolean isDiffuse() {
        return diffuseDim > 0;
    }
    
    @Override
    public int getDiffuseDim() {
        return diffuseDim;
    }
    
    @Override
    public void diffuseConstraints(FastMatrix b) {
        if (B != null)
            b.copy(B);
    }
    
    @Override
    public void a0(DataBlock a) {
        if (a0 != null) {
            a.copyFrom(a0, 0);
        }
    }
    
    @Override
    public void Pf0(FastMatrix pf) {
        if (Pf != null) {
            pf.copy(Pf);
        }
    }
    
    @Override
    public void Pi0(FastMatrix pi) {
        if (Pi != null) {
            pi.copy(Pi);
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
            DataBlock a = DataBlock.of(a0);
            builder.append(a.toString(ISsfState.FMT));
        }
        builder.append(System.lineSeparator());
        if (Pf == null) {
            builder.append("Pf0:").append(System.lineSeparator()).append("0");
        } else {
            builder.append(Pf.toString(ISsfState.FMT));
        }
        builder.append(System.lineSeparator());
        if (B == null) {
            builder.append("B:").append(System.lineSeparator()).append("0");
        } else {
            builder.append(B.toString(ISsfState.FMT));
        }
        return builder.toString();
    }
    
}
