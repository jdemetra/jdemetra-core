/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.data;

import ec.tstoolkit.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PeriodicDummies {

    private final int period;
    private final int start;

    public PeriodicDummies(int period) {
        this.period = period;
        start=0;
    }

    public PeriodicDummies(int period, int start) {
        this.period = period;
        this.start=start;
    }
    
    public Matrix matrix(int len) {
        Matrix m = new Matrix(len, period);
        DataBlockIterator columns = m.columns();
        DataBlock col = columns.getData();
        int pos = 0;
        do {
            int beg=(pos++)-start;
            if (beg<0)
                beg+=period;
            col.extract(beg, -1, period).set(1);
        } while (columns.next());
        return m;
    }

}
