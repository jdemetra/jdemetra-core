/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.maths.highprecision;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DoubleComplexComputer {

    private final DoubleDoubleComputer re, im;

    public DoubleComplexComputer(DoubleComplex c) {
        re = new DoubleDoubleComputer(c.getReHigh(), c.getReLow());
        im = new DoubleDoubleComputer(c.getImHigh(), c.getImLow());
    }

    public DoubleComplexComputer(double re, double im) {
        this.re = new DoubleDoubleComputer(re);
        this.im = new DoubleDoubleComputer(im);
    }

    public DoubleComplexComputer add(DoubleComplex c) {
        re.add(c.getReHigh(), c.getReLow());
        im.add(c.getImHigh(), c.getImLow());
        return this;
    }

    public DoubleComplexComputer add(DoubleDoubleType x) {
        re.add(x);
        return this;
    }

    public DoubleComplexComputer add(double xhigh, double xlow) {
        re.add(xhigh, xlow);
        return this;
    }

    public DoubleComplexComputer add(double x) {
        re.add(x);
        return this;
    }

    public DoubleComplexComputer sub(DoubleComplex c) {
        re.sub(c.getReHigh(), c.getReLow());
        im.sub(c.getImHigh(), c.getImLow());
        return this;
    }

    public DoubleComplexComputer sub(DoubleDoubleType x) {
        re.sub(x);
        return this;
    }

    public DoubleComplexComputer sub(double xhigh, double xlow) {
        re.sub(xhigh, xlow);
        return this;
    }

    public DoubleComplexComputer sub(double x) {
        re.sub(x);
        return this;
    }

    /**
     * Divides this object by a complex number (= x + i*y)
     *
     * @param x The real part
     * @param y The imaginary part
     * @return This object is returned
     */
    public DoubleComplexComputer mul(final DoubleComplex c) {
        DoubleDoubleComputer tmp1 = new DoubleDoubleComputer(re);
        // tmp1 = Re*c.Re
        tmp1.mul(c.getReHigh(), c.getReLow());
        DoubleDoubleComputer tmp2 = new DoubleDoubleComputer(im);
        // tmp2 = Im*c.Im
        tmp2.mul(c.getImHigh(), c.getImLow());
        // tmp1 = Re*c.Re - Im*c.Im
        tmp1.sub(tmp2);
        // tmp2 = Re
        tmp2.set(re);
        re.set(tmp1);
        // tmp1 = Im
        tmp1.set(im);
        // tmp2 = Re*c.Im
        tmp2.mul(c.getImHigh(), c.getImLow());
        // tmp1 = Im*c.Re
        tmp1.mul(c.getReHigh(), c.getReLow());
        tmp2.add(tmp1);
        im.set(tmp2);
        return this;
    }

    public DoubleComplexComputer mul(final DoubleDoubleType c) {
        re.mul(c);
        im.mul(c);
        return this;
    }

    public DoubleComplexComputer mul(final double xhigh, final double xlow) {
        re.mul(xhigh, xlow);
        im.mul(xhigh, xlow);
        return this;
    }

    public DoubleComplexComputer mul(final double x) {
        re.mul(x);
        im.mul(x);
        return this;
    }

    public DoubleComplexComputer div(final DoubleComplex c) {
 
//        if (Math.abs(x) >= Math.abs(y)) {
//            scalar = 1.0 / (x + y * (y / x));
//
//            dRe = scalar * (re + im * (y / x));
//            dIm = scalar * (im - re * (y / x));
//
//        } else {
//            scalar = 1.0 / (x * (x / y) + y);
//
//            dRe = scalar * (re * (x / y) + im);
//            dIm = scalar * (im * (x / y) - re);
//        }// endif
//        re = dRe;
//        im = dIm;
        DoubleDouble x=c.getRe(), y=c.getIm();
        DoubleDouble scalar, dRe, dIm;
        if (x.abs().gt(y.abs())) {
            DoubleDoubleComputer cpt=new DoubleDoubleComputer(y);
            scalar =cpt.div(x).mul(y).add(x).inv().result();
            dRe=cpt.set(y).div(x).mul(im).add(re).mul(scalar).result();
            dIm=cpt.set(y).div(x).mul(re).chs().add(im).mul(scalar).result();
        } else {
            DoubleDoubleComputer cpt=new DoubleDoubleComputer(x);
            scalar =cpt.div(y).mul(x).add(y).inv().result();
            dRe=cpt.set(x).div(y).mul(re).add(im).mul(scalar).result();
            dIm=cpt.set(x).div(y).mul(im).sub(re).mul(scalar).result();
        }
        re.set(dRe);
        im.set(dIm);
        return this;
    }

    public DoubleComplexComputer div(final DoubleDoubleType c) {
        re.div(c);
        im.div(c);
        return this;
    }

    public DoubleComplexComputer div(final double xhigh, final double xlow) {
        re.div(xhigh, xlow);
        im.div(xhigh, xlow);
        return this;
    }

    public DoubleComplexComputer div(final double x) {
        re.div(x);
        im.div(x);
        return this;
    }

    public DoubleComplex result() {
        return new DoubleComplex(re.getHigh(), re.getLow(), im.getHigh(), im.getLow());
    }
}
