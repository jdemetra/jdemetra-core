/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import demetra.data.DoubleSequence;
import java.util.Formatter;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface PolynomialType {
    
    public static final PolynomialType ONE=new LightPolynomial(new double[]{1});
    public static final PolynomialType ZERO=new LightPolynomial(new double[]{0});
    
    public static PolynomialType of(@Nonnull double... coefficients){
        return new LightPolynomial(coefficients.clone());
    }

    int getDegree();

    double get(int i);

    static class LightPolynomial implements PolynomialType {

        private double[] c;

        private LightPolynomial(double[] c) {
            this.c = c;
        }

        @Override
        public int getDegree() {
            return c.length - 1;
        }

        @Override
        public double get(int i) {
            return c[i];
        }
        
        @Override
        public String toString(){
            return PolynomialType.toString(this, "%6g", 'X');
        }

    }
    
    public static String toString(PolynomialType p, final String fmt, final char var){
        StringBuilder sb = new StringBuilder(512);
        boolean sign = false;
        int n = p.getDegree();
        if (n == 0) {
            sb.append(new Formatter().format(fmt, p.get(0)));
        } else {
            for (int i = 0; i <= n; ++i) {
                double v = Math.abs(p.get(i));
                if (v >= 1e-6) {
                    if (v > p.get(i)) {
                        sb.append(" - ");
                    } else if (sign) {
                        sb.append(" + ");
                    }
                    if ((v != 1) || (i == 0)) {
                        sb.append(new Formatter().format(fmt, v).toString());
                    }
                    sign = true;
                    if (i > 0) {
                        sb.append(' ').append(var);
                    }
                    if (i > 1) {
                        sb.append('^').append(i);
                    }
                }
            }
        }
        return sb.toString();
    }
}
