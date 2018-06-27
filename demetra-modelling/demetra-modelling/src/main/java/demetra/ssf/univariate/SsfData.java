/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.data.Doubles;
import demetra.data.Sequence;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;


/**
 *
 * @author Jean Palate
 */
public class SsfData implements ISsfData  {

    private final DoubleSequence data;

    public SsfData(DoubleSequence x) {
        data = x;
    }

    public SsfData(double[] x) {
        data = DoubleSequence.onMapping(x.length, i->x[i]);
    }

    @Override
    public double get(int pos) {
        return pos < data.length() ? data.get(pos) : Double.NaN;
    }

    @Override
    public boolean isMissing(int pos) {
        if (pos >= data.length()) {
            return true;
        }
        double y = data.get(pos);
        return !Double.isFinite(y);
    }

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public int length() {
        return data.length();
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        data.copyTo(buffer, start);
    }

    public DoubleSequence extract(int start, int length) {
        return data.extract(start, length);
    }
    
    @Override
    public String toString(){
        return DoubleSequence.toString(data);
    }
}
