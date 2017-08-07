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
package demetra.ssf.implementations;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author Jean Palate
 */
public class ConstantInitialization implements ISsfInitialization{
    
    private final int dim, ndiffuse;
    
    public ConstantInitialization(int dim, int ndiffuse){
        this.dim=dim;
        this.ndiffuse=ndiffuse;
    }
    
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getStateDim() {
        return dim;
    }

    @Override
    public boolean isDiffuse() {
        return true;
    }

    @Override
    public int getDiffuseDim() {
        return ndiffuse;
    }

    @Override
    public void diffuseConstraints(Matrix b) {
        b.diagonal().set(1);
    }

    @Override
    public boolean a0(DataBlock a0) {
        return true;
    }

    @Override
    public boolean Pf0(Matrix pf0) {
        return true;
    }

    @Override
    public void Pi0(Matrix p) {
        p.diagonal().set(1);
    }


}
