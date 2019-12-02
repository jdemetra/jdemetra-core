/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

/**
 *
 * @author palatej
 */
@lombok.Data
public class HouseholderReflector {

    double tau;
    private final double[] px;
    private final int incx; // increment between x
    private int xstart; // start of the vector 
    int n; // length of the vector 

    double x0() {
        return px[xstart];
    }
    
    void beta(double beta){
        px[xstart]=beta;
    }

    DataPointer v() {
        return DataPointer.of(px, xstart + incx, incx);
    }

    public HouseholderReflector(int n, DataPointer x) {
        this.n = n;
        this.px = x.p;
        this.incx = x.inc();
        this.xstart = x.pos + incx;
    }

    public HouseholderReflector(double[] x, int incx) {
        this.px = x;
        this.incx = incx;
    }

    /**
     * Initialize the reflector
     *
     * @param start Starting position of the vector
     * @param n Number of elements in the vector
     */
    public void set(int start, int n) {
        this.xstart = start;
        this.n = n;
        this.tau = 0;
    }

}
