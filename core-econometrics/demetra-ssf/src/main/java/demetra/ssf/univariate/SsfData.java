/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.data.CellReader;
import demetra.data.Doubles;
import demetra.data.DoublesUtility;
import demetra.data.Sequence;


/**
 *
 * @author Jean Palate
 */
public class SsfData implements ISsfData  {

    private final Doubles data;

    public SsfData(Doubles x) {
        data = x;
    }

    public SsfData(double[] x) {
        data = Doubles.ofFunction(x.length, i->x[i]);
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

    public Doubles extract(int start, int length) {
        return data.extract(start, length);
    }
    
    @Override
    public String toString(){
        return Doubles.toString(data);
    }
}
