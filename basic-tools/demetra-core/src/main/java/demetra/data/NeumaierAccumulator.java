/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

/**
 * Kahan and Babuska summation, Neumaier variant
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public strictfp final class NeumaierAccumulator implements DoubleAccumulator {

    private double del, sum;

    @Override
    public void reset() {
        del = 0;
        sum = 0;
    }

    @Override
    public void add(double x) {
        double t = sum + x;
        if (Math.abs(sum) >= Math.abs(x)) {
            del += (sum - t) + x;
        } else {
            del += (x - t) + sum;
        }
        sum = t;
    }

    @Override
    public double sum() {
        return sum + del;
    }

    @Override
    public void set(double value) {
        del = 0;
        sum = value;
    }

}
