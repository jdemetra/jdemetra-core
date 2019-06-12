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
package demetra.var;

import jdplus.data.DataBlock;
import jdplus.ssf.ISsfInitialization;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class VarInitialization implements ISsfInitialization {

    private final int dim;
    private final FastMatrix V0;

    public VarInitialization(int dim, FastMatrix V0) {
        this.dim = dim;
        this.V0 = V0;
    }

    @Override
    public boolean isDiffuse() {
        return false;
    }

    @Override
    public int getDiffuseDim() {
        return 0;
    }

    @Override
    public void diffuseConstraints(FastMatrix b) {
    }

    @Override
    public void a0(DataBlock a0) {
    }

    @Override
    public void Pf0(FastMatrix pf0) {
        if (V0 != null) {
            int nvars = V0.getRowsCount();
            pf0.topLeft(nvars, nvars).copy(V0);
        }
    }

    @Override
    public int getStateDim() {
        return dim;
    }

}
