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
package demetra.maths.highprecision;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class DoubleComplex {

    private double reHigh, reLow, imHigh, imLow;

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
                im=new DoubleDoubleComputer(reHigh, reLow);
        return re.square().add(im.square()).result();
    }

    public DoubleDouble abs(){
        DoubleDoubleComputer re=new DoubleDoubleComputer(reHigh, reLow),
                im=new DoubleDoubleComputer(reHigh, reLow);
        return re.square().add(im.square()).sqrt().result();
    }
}
