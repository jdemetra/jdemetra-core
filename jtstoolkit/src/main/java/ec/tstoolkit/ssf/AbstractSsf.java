/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tstoolkit.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author palatej
 */
public abstract class AbstractSsf implements ISsf {

    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        T(pos, lm);
        DataBlockIterator rows = lm.rows();
        DataBlock row = rows.getData();
        int r = 0;
        do {
            double c=k.get(r++);
            if (c != 0)
                XpZd(pos, row, -c);
        } while (rows.next());
    }

    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        DataBlockIterator columns = m.columns();
        DataBlock column = columns.getData();
        int c = 0;
        do {
            x.set(c++, ZX(pos, column));
        } while (columns.next());
    }

    @Override
    public int getTransitionResCount() {
        return getStateDim();
    }

    @Override
    public int getTransitionResDim() {
        return getStateDim();
    }

    @Override
    public boolean hasR() {
        return false;
    }

    @Override
    public boolean hasTransitionRes(int pos) {
        return true;
    }

    @Override
    public boolean hasW() {
        return false;
    }

    @Override
    public void Q(int pos, SubMatrix qm) {
        fullQ(pos, qm);
    }

    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    @Override
    public void TVT(int pos, SubMatrix vm) {
        DataBlockIterator cols = vm.columns();
        DataBlock col = cols.getData();
        do {
            TX(pos, col);
        } while (cols.next());

        DataBlockIterator rows = vm.rows();
        DataBlock row = rows.getData();
        do {
            TX(pos, row);
        } while (rows.next());
    }

    @Override
    public void W(int pos, SubMatrix wv) {
    }
}
