/*
 * Copyright 2016 National Bank copyOf Belgium
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
package jdplus.ssf.implementations;

import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class ConstantDynamics implements ISsfDynamics {

    public ConstantDynamics() {
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
        return 0;
    }

    @Override
    public void V(int pos, FastMatrix qm) {
    }

    @Override
    public boolean hasInnovations(int pos) {
        return false;
    }

    @Override
    public void S(int pos, FastMatrix sm) {
    }

//    @Override
//    public void addSX(int pos, DataBlock x, DataBlock y) {
//        y.add(x);
//    }
//
    @Override
    public void T(int pos, FastMatrix tr) {
        tr.diagonal().set(1);
    }

    @Override
    public void TX(int pos, DataBlock x) {
    }

    @Override
    public void TM(int pos, FastMatrix m) {
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock sx) {
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock sx) {
    }

    @Override
    public void XT(int pos, DataBlock x) {
    }

    @Override
    public void TVT(int pos, FastMatrix v) {
    }

    @Override
    public void addV(int pos, FastMatrix p) {
    }

    @Override
    public void MT(int pos, FastMatrix x) {
    }
}
