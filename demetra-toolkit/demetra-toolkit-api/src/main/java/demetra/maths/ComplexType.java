/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
import demetra.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public interface ComplexType {

    double getRe();

    double getIm();

    /**
     * abs(z) = sqrt(re*re + im*im)
     *
     * @param re Real part
     * @param im Imaginary part
     * @return Absolute value of re + i * im
     */
    public static double abs(final double re, final double im) {
        if (re == 0 && im == 0) {
            return 0;
        }
        final double absX = Math.abs(re);
        final double absY = Math.abs(im);

        double w = Math.max(absX, absY);
        double z = Math.min(absX, absY);
        if (z == 0) {
            return w;
        } else {
            double zw = z / w;
            return w * Math.sqrt(1 + zw * zw);
        }
    }
    default double abs() {
        return abs(getRe(), getIm());
    }

    public static double arg(final double re, final double im) {
        return Math.atan2(im, re);
    }

    default double arg(){
        return arg(getRe(), getIm());
    }
}
