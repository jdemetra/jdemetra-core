/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import demetra.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public interface ComplexType {

    double getRe();

    double getIm();

    default double arg() {
        return Math.atan2(getIm(), getRe());
    }

    default double abs() {
        double xa = Math.abs(getRe()), xb = Math.abs(getIm());
        double w, z;
        if (xa > xb) {
            w = xa;
            z = xb;
        } else {
            w = xb;
            z = xa;
        }
        if (z == 0.0) {
            return w;
        } else {
            double zw = z / w;
            return w * Math.sqrt(1.0 + zw * zw);
        }
    }

}
