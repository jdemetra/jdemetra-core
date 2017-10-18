/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import java.util.Arrays;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
class LightPolynomial implements PolynomialType {

    private double[] c;

    LightPolynomial(double[] c) {
        this.c = c;
    }

    @Override
    public int getDegree() {
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
    public String toString() {
        return PolynomialType.toString(this, "%6g", 'X');
    }
}
