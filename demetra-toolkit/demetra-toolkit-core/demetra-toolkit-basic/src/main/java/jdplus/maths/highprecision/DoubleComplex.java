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

import demetra.maths.Complex;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class DoubleComplex {
    
    public static final DoubleComplex ZERO=new DoubleComplex(0,0,0,0);
    public static final DoubleComplex ONE=new DoubleComplex(1,0,0,0);
    public static final DoubleComplex I=new DoubleComplex(0,0,1,0);

    private double reHigh, reLow, imHigh, imLow;

    public static DoubleComplex of(Complex c) {
        return new DoubleComplex(c.getRe(), 0, c.getIm(), 0);
    }

    public static DoubleComplex cart(double re, double im) {
        return new DoubleComplex(re, 0, im, 0);
    }

    public static DoubleComplex cart(DoubleDoubleType re, DoubleDoubleType im) {
        return new DoubleComplex(re.getHigh(), re.getLow(), im.getHigh(), im.getLow());
    }
    
    public DoubleDouble getRe(){
        return new DoubleDouble(reHigh, reLow);
    }
    
    public DoubleDouble getIm(){
        return new DoubleDouble(imHigh, imLow);
    }
    
    public DoubleDouble absSquare(){
        DoubleDoubleComputer re=new DoubleDoubleComputer(reHigh, reLow),
                im=new DoubleDoubleComputer(imHigh, imLow);
        return re.square().add(im.square()).result();
    }

    public DoubleDouble abs(){
        DoubleDoubleComputer re=new DoubleDoubleComputer(reHigh, reLow),
                im=new DoubleDoubleComputer(imHigh, imLow);
        return re.square().add(im.square()).sqrt().result();
    }
    
    public DoubleComplex plus(DoubleComplex c){
        return new DoubleComplexComputer(this).add(c).result();
    }
    
    public DoubleComplex minus(DoubleComplex c){
        return new DoubleComplexComputer(this).sub(c).result();
    }

    public DoubleComplex times(DoubleComplex c){
        return new DoubleComplexComputer(this).mul(c).result();
    }

    public DoubleComplex plus(Complex c){
        return new DoubleComplexComputer(this).add(DoubleComplex.of(c)).result();
    }
    
    public DoubleComplex minus(Complex c){
        return new DoubleComplexComputer(this).sub(DoubleComplex.of(c)).result();
    }

    public DoubleComplex times(Complex c){
        return new DoubleComplexComputer(this).mul(DoubleComplex.of(c)).result();
    }

    public DoubleComplex plus(DoubleDouble r){
        return new DoubleComplexComputer(this).add(r).result();
    }
    
    public DoubleComplex minus(DoubleDouble r){
        return new DoubleComplexComputer(this).sub(r).result();
    }

    public DoubleComplex times(DoubleDouble r){
        return new DoubleComplexComputer(this).mul(r).result();
    }
    
    public DoubleComplex plus(double r){
        return new DoubleComplexComputer(this).add(r).result();
    }
    
    public DoubleComplex minus(double r){
        return new DoubleComplexComputer(this).sub(r).result();
    }

    public DoubleComplex times(double r){
        return new DoubleComplexComputer(this).mul(r).result();
    }
    
    
//    public DoubleComplex divide(DoubleComplex c){
//        return new DoubleComplexComputer(this).div(c).result();
//    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(getRe());

        DoubleDouble im = getIm(); 
        if (im.isNegative()) {
            result.append(" - ").append(im.negate());
        } else if (im.isZero()) {
            result.append(" - ").append(0.0);
        } else {
            result.append(" + ").append(im);
        }

        result.append("i)");
        return result.toString();
    }
}
