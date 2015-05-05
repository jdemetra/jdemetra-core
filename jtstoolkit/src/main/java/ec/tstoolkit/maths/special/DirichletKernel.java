/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.maths.special;

/**
 *
 * @author Jean Palate
 */
public class DirichletKernel implements IRealFunction {

    private static final double TWOPI = 2 * Math.PI, EPS = 1e-9;

    private final int n_, m_;
    private final double mc_;

    private boolean mpi(double x) {
        double y = x / Math.PI;
        return Math.abs(y - Math.round(y)) < EPS;
    }

    public DirichletKernel(int n) {
        n_ = n;
        m_ = (2 * n + 1);
        mc_ = m_ / TWOPI;
    }

    public int getN() {
        return n_;
    }

    @Override
    public double fn(final double x) {
        double x2 = x * .5;
        if (mpi(x2)) {
            return mc_;
        } else {
            return (Math.sin(m_ * x2) / Math.sin(x2)) / TWOPI;
        }
    }
}
