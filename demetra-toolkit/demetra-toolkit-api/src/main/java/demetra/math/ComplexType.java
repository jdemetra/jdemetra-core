/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.math;

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
