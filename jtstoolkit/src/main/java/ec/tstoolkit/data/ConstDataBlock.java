/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.data;

/**
 *
 * @author Admin
 */
public class ConstDataBlock implements IReadDataBlock {

    private final int n;
    private final double val;

    public ConstDataBlock(int n) {
        this.n = n;
        this.val = 0;
    }

    public ConstDataBlock(double val) {
        this.n = 1;
        this.val = val;
    }

    public ConstDataBlock(int n, double val) {
        this.n = n;
        this.val = val;
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        for (int i = 0; i < n; ++i) {
            buffer[start + i] = val;
        }
    }

    @Override
    public double get(int idx) {
        return val;
    }

    @Override
    public int getLength() {
        return n;
    }

    @Override
    public IReadDataBlock rextract(int start, int length) {
        if (start+length > n)
            return DataBlock.EMPTY;
        return new ConstDataBlock(length, val);
    }

}
