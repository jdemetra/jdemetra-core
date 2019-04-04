/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.data.Doubles;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleSeq;
import demetra.data.Seq;


/**
 *
 * @author Jean Palate
 */
public class SsfData implements ISsfData  {

    private final DoubleSeq data;

    public SsfData(DoubleSeq x) {
        data = x;
    }

    public SsfData(double[] x) {
        data = DoubleSeq.onMapping(x.length, i->x[i]);
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

    public DoubleSeq extract(int start, int length) {
        return data.extract(start, length);
    }
    
    @Override
    public String toString(){
        return DoubleSeq.format(data);
    }
}
