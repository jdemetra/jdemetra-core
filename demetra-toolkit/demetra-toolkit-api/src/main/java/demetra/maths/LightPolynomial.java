/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import demetra.design.Development;
import java.util.Arrays;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
class LightPolynomial implements PolynomialType {

    private final double[] c;

    LightPolynomial(double[] c) {
        this.c = c;
    }

    @Override
    public int degree() {
        return c.length - 1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (other instanceof LightPolynomial) {
            return Arrays.equals(c, ((LightPolynomial) other).c);
        }
        if (other instanceof PolynomialType) {
            return PolynomialType.equals(this, ((PolynomialType) other));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Arrays.hashCode(this.c);
        return hash;
    }

    @Override
    public double get(int i) {
        return c[i];
    }

    @Override
    public double[] toArray() {
        return c.clone();
    }
    
    @Override
    public void copyTo(double[] buffer, int startpos){
        System.arraycopy(c, 0, buffer, startpos, c.length);
    }

    @Override
    public DoubleSeq coefficients() {
        return DoubleSeq.of(c);
    }

    @Override
    public String toString() {
        return PolynomialType.toString(this, "%6g", 'X');
    }

}
